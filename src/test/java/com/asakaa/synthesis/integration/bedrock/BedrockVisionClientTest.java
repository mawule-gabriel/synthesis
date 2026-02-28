package com.asakaa.synthesis.integration.bedrock;

import com.asakaa.synthesis.exception.DiagnosticException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BedrockVisionClientTest {

    @Mock
    private BedrockRuntimeClient bedrockRuntimeClient;

    @InjectMocks
    private BedrockClient bedrockClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(bedrockClient, "modelId", "anthropic.claude-3-sonnet-20240229-v1:0");
        ReflectionTestUtils.setField(bedrockClient, "maxTokens", 1024);
        ReflectionTestUtils.setField(bedrockClient, "temperature", 0.2);
    }

    @Test
    void invokeVision_WithValidJpegImage_ReturnsAnalysis() throws Exception {
        // Arrange
        byte[] imageBytes = "fake-image-data".getBytes();
        String mediaType = "image/jpeg";
        String prompt = "Analyze this chest X-ray";
        String responseText = "The chest X-ray shows clear lung fields with no infiltrates";

        ObjectMapper mapper = new ObjectMapper();
        String responseBody = mapper.writeValueAsString(
                mapper.createObjectNode()
                        .set("content", mapper.createArrayNode()
                                .add(mapper.createObjectNode()
                                        .put("type", "text")
                                        .put("text", responseText)))
        );

        InvokeModelResponse mockResponse = InvokeModelResponse.builder()
                .body(SdkBytes.fromUtf8String(responseBody))
                .build();

        when(bedrockRuntimeClient.invokeModel(any(InvokeModelRequest.class))).thenReturn(mockResponse);

        // Act
        String result = bedrockClient.invokeVision(imageBytes, mediaType, prompt);

        // Assert
        assertNotNull(result);
        assertEquals(responseText, result);
        verify(bedrockRuntimeClient).invokeModel(any(InvokeModelRequest.class));
    }

    @Test
    void invokeVision_WithPngImage_ReturnsAnalysis() throws Exception {
        // Arrange
        byte[] imageBytes = "fake-png-data".getBytes();
        String mediaType = "image/png";
        String prompt = "Describe this skin lesion";
        String responseText = "The lesion appears to be a benign nevus";

        ObjectMapper mapper = new ObjectMapper();
        String responseBody = mapper.writeValueAsString(
                mapper.createObjectNode()
                        .set("content", mapper.createArrayNode()
                                .add(mapper.createObjectNode()
                                        .put("type", "text")
                                        .put("text", responseText)))
        );

        InvokeModelResponse mockResponse = InvokeModelResponse.builder()
                .body(SdkBytes.fromUtf8String(responseBody))
                .build();

        when(bedrockRuntimeClient.invokeModel(any(InvokeModelRequest.class))).thenReturn(mockResponse);

        // Act
        String result = bedrockClient.invokeVision(imageBytes, mediaType, prompt);

        // Assert
        assertNotNull(result);
        assertEquals(responseText, result);
    }

    @Test
    void invokeVision_WithUnsupportedMediaType_ThrowsDiagnosticException() {
        // Arrange
        byte[] imageBytes = "fake-data".getBytes();
        String mediaType = "image/gif";
        String prompt = "Analyze this";

        // Act & Assert
        DiagnosticException exception = assertThrows(
                DiagnosticException.class,
                () -> bedrockClient.invokeVision(imageBytes, mediaType, prompt)
        );

        assertTrue(exception.getMessage().contains("Unsupported media type"));
        verify(bedrockRuntimeClient, never()).invokeModel(any(software.amazon.awssdk.services.bedrockruntime.model.InvokeModelRequest.class));
    }

    @Test
    void invokeVision_WhenSdkThrowsException_ThrowsDiagnosticException() {
        // Arrange
        byte[] imageBytes = "fake-data".getBytes();
        String mediaType = "image/jpeg";
        String prompt = "Analyze this";

        when(bedrockRuntimeClient.invokeModel(any(InvokeModelRequest.class)))
                .thenThrow(SdkClientException.create("Image too large"));

        // Act & Assert
        DiagnosticException exception = assertThrows(
                DiagnosticException.class,
                () -> bedrockClient.invokeVision(imageBytes, mediaType, prompt)
        );

        assertTrue(exception.getMessage().contains("too large") || 
                   exception.getMessage().contains("throttled"));
    }
}
