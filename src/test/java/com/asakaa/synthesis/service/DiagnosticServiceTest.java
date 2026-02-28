package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.DiagnosticRequest;
import com.asakaa.synthesis.domain.dto.response.DiagnosticResponse;
import com.asakaa.synthesis.domain.dto.response.DifferentialDto;
import com.asakaa.synthesis.domain.entity.Consultation;
import com.asakaa.synthesis.domain.entity.ConsultationStatus;
import com.asakaa.synthesis.domain.entity.Patient;
import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.exception.DiagnosticException;
import com.asakaa.synthesis.exception.ResourceNotFoundException;
import com.asakaa.synthesis.integration.bedrock.BedrockClient;
import com.asakaa.synthesis.integration.bedrock.BedrockPromptBuilder;
import com.asakaa.synthesis.integration.bedrock.ClinicalContext;
import com.asakaa.synthesis.repository.ConsultationRepository;
import com.asakaa.synthesis.repository.DiagnosisRepository;
import com.asakaa.synthesis.repository.ImageAnalysisRepository;
import com.asakaa.synthesis.repository.LabResultRepository;
import com.asakaa.synthesis.util.ResponseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiagnosticServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @Mock
    private ImageAnalysisRepository imageAnalysisRepository;

    @Mock
    private LabResultRepository labResultRepository;

    @Mock
    private BedrockPromptBuilder bedrockPromptBuilder;

    @Mock
    private BedrockClient bedrockClient;

    @Mock
    private ResponseParser responseParser;

    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DiagnosticService diagnosticService;

    private DiagnosticRequest request;
    private Consultation consultation;
    private Patient patient;
    private Provider provider;

    @BeforeEach
    void setUp() {
        request = DiagnosticRequest.builder()
                .consultationId(1L)
                .availableEquipment(List.of("Stethoscope", "Thermometer"))
                .localFormulary(List.of("Paracetamol", "Amoxicillin"))
                .build();

        patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setDateOfBirth(LocalDate.of(1990, 1, 1));
        patient.setGender("Male");
        patient.setBloodGroup("O+");

        provider = new Provider();
        provider.setId(1L);
        provider.setName("Dr. Smith");

        consultation = new Consultation();
        consultation.setId(1L);
        consultation.setPatient(patient);
        consultation.setProvider(provider);
        consultation.setStatus(ConsultationStatus.OPEN);
        consultation.setChiefComplaint("Fever and cough");
        consultation.setVitals("{\"temperature\": 38.5}");
        consultation.setOpenedAt(LocalDateTime.now());
        consultation.setDiagnoses(new ArrayList<>());
    }

    @Test
    void analyze_ReturnsCorrectNumberOfDifferentials() {
        // Arrange
        List<DifferentialDto> differentials = List.of(
                DifferentialDto.builder()
                        .condition("Malaria")
                        .confidence(BigDecimal.valueOf(0.85))
                        .reasoning("High fever in endemic area")
                        .build(),
                DifferentialDto.builder()
                        .condition("Pneumonia")
                        .confidence(BigDecimal.valueOf(0.65))
                        .reasoning("Cough and fever")
                        .build()
        );

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(labResultRepository.findByConsultationId(1L)).thenReturn(List.of());
        when(imageAnalysisRepository.findByConsultationId(1L)).thenReturn(List.of());
        when(knowledgeBaseService.queryGuidelines(anyString())).thenReturn(List.of());
        when(knowledgeBaseService.formatCitationsForPrompt(any())).thenReturn("");
        when(knowledgeBaseService.extractCitationReferences(any(), any())).thenReturn(List.of());
        when(bedrockPromptBuilder.buildDiagnosticPrompt(any(ClinicalContext.class))).thenReturn("prompt");
        when(bedrockClient.invoke(anyString())).thenReturn("{\"differentials\": []}");
        when(responseParser.parseDiagnosticResponse(anyString())).thenReturn(
                new ResponseParser.DiagnosticParseResult(differentials, List.of(), List.of(), "MODERATE"));

        // Act
        DiagnosticResponse response = diagnosticService.analyze(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getDifferentials().size());
        assertEquals(1L, response.getConsultationId());
        verify(consultationRepository).save(any(Consultation.class));
    }

    @Test
    void analyze_ThrowsResourceNotFoundException_WhenConsultationNotFound() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> diagnosticService.analyze(request));
        verify(bedrockClient, never()).invoke(anyString());
    }

    @Test
    void analyze_ThrowsDiagnosticException_WhenBedrockClientThrows() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(bedrockPromptBuilder.buildDiagnosticPrompt(any(ClinicalContext.class))).thenReturn("prompt");
        when(bedrockClient.invoke(anyString())).thenThrow(new DiagnosticException("Bedrock error"));

        // Act & Assert
        assertThrows(DiagnosticException.class, () -> diagnosticService.analyze(request));
    }
}
