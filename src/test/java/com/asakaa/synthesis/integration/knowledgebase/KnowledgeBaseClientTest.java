package com.asakaa.synthesis.integration.knowledgebase;

import com.asakaa.synthesis.domain.dto.response.KnowledgeBaseCitation;
import com.asakaa.synthesis.exception.DiagnosticException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;
import software.amazon.awssdk.services.bedrockagentruntime.model.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeBaseClientTest {

    @Mock
    private BedrockAgentRuntimeClient bedrockAgentRuntimeClient;

    @InjectMocks
    private KnowledgeBaseClient knowledgeBaseClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(knowledgeBaseClient, "knowledgeBaseId", "test-kb-id");
    }

    @Test
    void retrieve_WithValidQuery_ReturnsCitations() {
        // Arrange
        String query = "malaria treatment";
        int maxResults = 5;

        RetrievalResultContent content = RetrievalResultContent.builder()
                .text("WHO Guidelines: IV Artesunate is preferred")
                .build();

        RetrievalResultS3Location s3Location = RetrievalResultS3Location.builder()
                .uri("s3://bucket/WHO_Malaria_Guidelines_2023.pdf")
                .build();

        RetrievalResultLocation location = RetrievalResultLocation.builder()
                .s3Location(s3Location)
                .build();

        KnowledgeBaseRetrievalResult result = KnowledgeBaseRetrievalResult.builder()
                .content(content)
                .location(location)
                .score(0.95)
                .build();

        RetrieveResponse mockResponse = RetrieveResponse.builder()
                .retrievalResults(List.of(result))
                .build();

        when(bedrockAgentRuntimeClient.retrieve(any(RetrieveRequest.class)))
                .thenReturn(mockResponse);

        // Act
        List<KnowledgeBaseCitation> citations = knowledgeBaseClient.retrieve(query, maxResults);

        // Assert
        assertNotNull(citations);
        assertEquals(1, citations.size());
        assertEquals("WHO Guidelines: IV Artesunate is preferred", citations.get(0).getText());
        assertEquals("WHO_Malaria_Guidelines_2023.pdf", citations.get(0).getSource());
        assertEquals(0.95, citations.get(0).getRelevanceScore());
        verify(bedrockAgentRuntimeClient).retrieve(any(RetrieveRequest.class));
    }

    @Test
    void retrieve_WhenClientThrows_ThrowsDiagnosticException() {
        // Arrange
        String query = "test query";
        when(bedrockAgentRuntimeClient.retrieve(any(RetrieveRequest.class)))
                .thenThrow(new RuntimeException("AWS error"));

        // Act & Assert
        assertThrows(DiagnosticException.class, 
                () -> knowledgeBaseClient.retrieve(query, 5));
    }

    @Test
    void retrieve_WithEmptyResults_ReturnsEmptyList() {
        // Arrange
        String query = "unknown condition";
        RetrieveResponse mockResponse = RetrieveResponse.builder()
                .retrievalResults(List.of())
                .build();

        when(bedrockAgentRuntimeClient.retrieve(any(RetrieveRequest.class)))
                .thenReturn(mockResponse);

        // Act
        List<KnowledgeBaseCitation> citations = knowledgeBaseClient.retrieve(query, 5);

        // Assert
        assertNotNull(citations);
        assertTrue(citations.isEmpty());
    }
}
