//package com.asakaa.synthesis.service;
//
//import com.asakaa.synthesis.domain.dto.response.ImageAnalysisResponse;
//import com.asakaa.synthesis.exception.DiagnosticException;
//import com.asakaa.synthesis.exception.ValidationException;
//import com.asakaa.synthesis.integration.bedrock.BedrockClient;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class ImageAnalysisServiceTest {
//
//    @Mock
//    private BedrockClient bedrockClient;
//
//    @InjectMocks
//    private DiagnosticService diagnosticService;
//
//    @Test
//    void analyzeImage_WithValidJpeg_ReturnsAnalysis() {
//        // Arrange
//        byte[] imageBytes = "test-image".getBytes();
//        String mediaType = "image/jpeg";
//        String clinicalContext = "Patient has persistent cough";
//        String mockResponse = "{\"description\": \"Chest X-ray shows infiltrates\", \"findings\": [\"Infiltrate in right lower lobe\", \"No pleural effusion\"]}";
//
//        when(bedrockClient.invokeVision(any(byte[].class), eq(mediaType), anyString()))
//                .thenReturn(mockResponse);
//
//        // Act
//        ImageAnalysisResponse response = diagnosticService.analyzeImage(imageBytes, mediaType, clinicalContext);
//
//        // Assert
//        assertNotNull(response);
//        assertNotNull(response.getDescription());
//        assertNotNull(response.getFindings());
//        assertNotNull(response.getAnalyzedAt());
//        verify(bedrockClient).invokeVision(any(byte[].class), eq(mediaType), anyString());
//    }
//
//    @Test
//    void analyzeImage_WithInvalidMediaType_ThrowsValidationException() {
//        // Arrange
//        byte[] imageBytes = "test-image".getBytes();
//        String mediaType = "image/gif";
//        String clinicalContext = "Test context";
//
//        // Act & Assert
//        assertThrows(ValidationException.class,
//                () -> diagnosticService.analyzeImage(imageBytes, mediaType, clinicalContext));
//        verify(bedrockClient, never()).invokeVision(any(), any(), any());
//    }
//
//    @Test
//    void analyzeImage_WithOversizedImage_ThrowsValidationException() {
//        // Arrange
//        byte[] imageBytes = new byte[6 * 1024 * 1024]; // 6MB
//        String mediaType = "image/jpeg";
//        String clinicalContext = "Test context";
//
//        // Act & Assert
//        assertThrows(ValidationException.class,
//                () -> diagnosticService.analyzeImage(imageBytes, mediaType, clinicalContext));
//        verify(bedrockClient, never()).invokeVision(any(), any(), any());
//    }
//
//    @Test
//    void analyzeImage_WhenBedrockFails_ThrowsDiagnosticException() {
//        // Arrange
//        byte[] imageBytes = "test-image".getBytes();
//        String mediaType = "image/jpeg";
//        String clinicalContext = "Test context";
//
//        when(bedrockClient.invokeVision(any(byte[].class), eq(mediaType), anyString()))
//                .thenThrow(new DiagnosticException("Bedrock error"));
//
//        // Act & Assert
//        assertThrows(DiagnosticException.class,
//                () -> diagnosticService.analyzeImage(imageBytes, mediaType, clinicalContext));
//    }
//}
