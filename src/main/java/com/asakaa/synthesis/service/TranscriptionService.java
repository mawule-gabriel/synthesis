package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.response.TranscriptionResponse;
import com.asakaa.synthesis.exception.TranscriptionException;
import com.asakaa.synthesis.exception.TranscriptionTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptionService {

    private final TranscribeClient transcribeClient;
    private final S3Client s3Client;

    @Value("${aws.transcribe.s3-bucket}")
    private String s3Bucket;

    @Value("${aws.transcribe.timeout-seconds}")
    private int timeoutSeconds;

    private static final Set<String> SUPPORTED_CONTENT_TYPES = Set.of(
            "audio/wav",
            "audio/mpeg",
            "audio/mp3",
            "audio/mp4",
            "audio/x-m4a",
            "audio/m4a"
    );

    private static final Map<String, MediaFormat> CONTENT_TYPE_TO_FORMAT = Map.of(
            "audio/wav", MediaFormat.WAV,
            "audio/mpeg", MediaFormat.MP3,
            "audio/mp3", MediaFormat.MP3,
            "audio/mp4", MediaFormat.MP4,
            "audio/x-m4a", MediaFormat.MP4,
            "audio/m4a", MediaFormat.MP4
    );

    /**
     * Transcribes an audio file using AWS Transcribe.
     *
     * @param audioFile the uploaded audio file
     * @return TranscriptionResponse containing the transcript, confidence score, and language code
     */
    public TranscriptionResponse transcribe(MultipartFile audioFile) {
        validateAudioFile(audioFile);

        String s3Key = "transcribe-" + UUID.randomUUID() + getFileExtension(audioFile);
        String jobName = "synthesis-" + UUID.randomUUID();
        MediaFormat mediaFormat = resolveMediaFormat(Objects.requireNonNull(audioFile.getContentType()));

        try {
            uploadToS3(audioFile, s3Key);
            log.info("Uploaded audio file to S3: s3://{}/{}", s3Bucket, s3Key);

            startTranscriptionJob(jobName, s3Key, mediaFormat);
            log.info("Started transcription job: {}", jobName);

            TranscriptionJob completedJob = pollForCompletion(jobName);
            log.info("Transcription job completed: {}", jobName);

            String transcriptUri = completedJob.transcript().transcriptFileUri();
            return fetchTranscript(transcriptUri);

        } catch (TranscriptionException | TranscriptionTimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new TranscriptionException("Transcription failed: " + e.getMessage(), e);
        } finally {
            cleanupS3(s3Key);
        }
    }

    private void validateAudioFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new TranscriptionException("Audio file is required and must not be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new TranscriptionException(
                    "Unsupported audio format: " + contentType +
                            ". Supported formats: WAV, MP3, MP4/M4A.");
        }
    }

    private MediaFormat resolveMediaFormat(String contentType) {
        MediaFormat format = CONTENT_TYPE_TO_FORMAT.get(contentType.toLowerCase());
        if (format == null) {
            throw new TranscriptionException("Cannot determine media format for content type: " + contentType);
        }
        return format;
    }

    private void uploadToS3(MultipartFile file, String s3Key) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new TranscriptionException("Failed to read audio file for upload.", e);
        }
    }

    private void startTranscriptionJob(String jobName, String s3Key, MediaFormat mediaFormat) {
        String s3Uri = "s3://" + s3Bucket + "/" + s3Key;

        StartTranscriptionJobRequest request = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .languageCode(LanguageCode.EN_US)
                .mediaFormat(mediaFormat)
                .media(Media.builder().mediaFileUri(s3Uri).build())
                .build();

        transcribeClient.startTranscriptionJob(request);
    }

    private TranscriptionJob pollForCompletion(String jobName) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeoutSeconds * 1000L;

        while (true) {
            GetTranscriptionJobRequest request = GetTranscriptionJobRequest.builder()
                    .transcriptionJobName(jobName)
                    .build();

            GetTranscriptionJobResponse response = transcribeClient.getTranscriptionJob(request);
            TranscriptionJob job = response.transcriptionJob();
            TranscriptionJobStatus status = job.transcriptionJobStatus();

            if (status == TranscriptionJobStatus.COMPLETED) {
                return job;
            }

            if (status == TranscriptionJobStatus.FAILED) {
                throw new TranscriptionException(
                        "Transcription job failed: " + job.failureReason());
            }

            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                throw new TranscriptionTimeoutException(
                        "Transcription timed out after " + timeoutSeconds + " seconds. " +
                                "Try a shorter audio clip (< 30 seconds recommended).");
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TranscriptionException("Transcription polling interrupted.", e);
            }
        }
    }

    private TranscriptionResponse fetchTranscript(String transcriptUri) {
        try {
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(transcriptUri))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            return parseTranscriptJson(body);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new TranscriptionException("Failed to fetch transcript from result URI.", e);
        }
    }

    /**
     * Parses the AWS Transcribe JSON result.
     * Expected structure:
     * {
     * "results": {
     * "transcripts": [{ "transcript": "..." }],
     * "items": [{ "alternatives": [{ "confidence": "0.98", "content": "..." }] }]
     * }
     * }
     */
    private TranscriptionResponse parseTranscriptJson(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(json);

            com.fasterxml.jackson.databind.JsonNode results = root.path("results");
            com.fasterxml.jackson.databind.JsonNode transcripts = results.path("transcripts");

            String transcript = "";
            if (transcripts.isArray() && !transcripts.isEmpty()) {
                transcript = transcripts.get(0).path("transcript").asText("");
            }

            double totalConfidence = 0.0;
            int confidenceCount = 0;
            com.fasterxml.jackson.databind.JsonNode items = results.path("items");
            if (items.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode item : items) {
                    com.fasterxml.jackson.databind.JsonNode alternatives = item.path("alternatives");
                    if (alternatives.isArray() && !alternatives.isEmpty()) {
                        String confidenceStr = alternatives.get(0).path("confidence").asText("");
                        if (!confidenceStr.isEmpty()) {
                            totalConfidence += Double.parseDouble(confidenceStr);
                            confidenceCount++;
                        }
                    }
                }
            }

            double avgConfidence = confidenceCount > 0 ? totalConfidence / confidenceCount : 0.0;
            avgConfidence = Math.round(avgConfidence * 100.0) / 100.0;

            return TranscriptionResponse.builder()
                    .transcript(transcript)
                    .confidence(avgConfidence)
                    .languageCode("en-US")
                    .build();

        } catch (Exception e) {
            throw new TranscriptionException("Failed to parse transcription result.", e);
        }
    }

    private void cleanupS3(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(s3Key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.debug("Cleaned up S3 object: {}", s3Key);
        } catch (Exception e) {
            log.warn("Failed to clean up S3 object {}: {}", s3Key, e.getMessage());
        }
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String contentType = file.getContentType();
        if (contentType != null) {
            return switch (contentType.toLowerCase()) {
                case "audio/wav" -> ".wav";
                case "audio/mpeg", "audio/mp3" -> ".mp3";
                case "audio/mp4", "audio/x-m4a", "audio/m4a" -> ".m4a";
                default -> ".audio";
            };
        }
        return ".audio";
    }
}
