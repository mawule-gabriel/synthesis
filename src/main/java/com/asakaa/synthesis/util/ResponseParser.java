package com.asakaa.synthesis.util;

import com.asakaa.synthesis.domain.dto.response.DifferentialDto;
import com.asakaa.synthesis.exception.DiagnosticException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class ResponseParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<DifferentialDto> parseDiagnosticResponse(String rawResponse) {
        try {
            // Extract JSON from response if it contains markdown code blocks
            String jsonContent = extractJson(rawResponse);

            JsonNode root = objectMapper.readTree(jsonContent);
            JsonNode differentials = root.get("differentials");

            if (differentials == null || !differentials.isArray()) {
                throw new DiagnosticException("Invalid response format: 'differentials' array not found");
            }

            List<DifferentialDto> result = new ArrayList<>();

            for (JsonNode differential : differentials) {
                DifferentialDto dto = DifferentialDto.builder()
                        .condition(differential.get("condition").asText())
                        .confidence(BigDecimal.valueOf(differential.get("confidence").asDouble()))
                        .reasoning(differential.get("reasoning").asText())
                        .recommendedTests(parseStringArray(differential.get("recommendedTests")))
                        .redFlags(parseStringArray(differential.get("redFlags")))
                        .build();

                result.add(dto);
            }

            return result;

        } catch (Exception e) {
            throw new DiagnosticException("Failed to parse diagnostic response: " + e.getMessage(), e);
        }
    }

    public String extractJson(String rawResponse) {
        // Remove markdown code blocks if present
        String cleaned = rawResponse.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private List<String> parseStringArray(JsonNode arrayNode) {
        List<String> result = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                result.add(item.asText());
            }
        }
        return result;
    }

    public List<com.asakaa.synthesis.domain.dto.response.TreatmentResponse> parseTreatmentResponse(String rawResponse) {
        try {
            // Extract JSON from response if it contains markdown code blocks
            String jsonContent = extractJson(rawResponse);

            JsonNode root = objectMapper.readTree(jsonContent);
            JsonNode treatments = root.get("treatments");

            if (treatments == null || !treatments.isArray()) {
                throw new DiagnosticException("Invalid treatment response format: 'treatments' array not found");
            }

            List<com.asakaa.synthesis.domain.dto.response.TreatmentResponse> result = new ArrayList<>();

            for (JsonNode treatment : treatments) {
                com.asakaa.synthesis.domain.dto.response.TreatmentResponse dto =
                        com.asakaa.synthesis.domain.dto.response.TreatmentResponse.builder()
                                .type(treatment.has("type") ? treatment.get("type").asText() : null)
                                .drugName(treatment.has("drugName") ? treatment.get("drugName").asText() : null)
                                .dosage(treatment.has("dosage") ? treatment.get("dosage").asText() : null)
                                .duration(treatment.has("duration") ? treatment.get("duration").asText() : null)
                                .instructions(treatment.has("instructions") ? treatment.get("instructions").asText() : null)
                                .build();

                result.add(dto);
            }

            return result;

        } catch (Exception e) {
            throw new DiagnosticException("Failed to parse treatment response: " + e.getMessage(), e);
        }
    }
}