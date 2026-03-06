package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.ConsultationRequest;
import com.asakaa.synthesis.domain.dto.request.ConsultationUpdateRequest;
import com.asakaa.synthesis.domain.dto.response.ConsultationResponse;
import com.asakaa.synthesis.domain.entity.Consultation;
import com.asakaa.synthesis.domain.entity.ConsultationStatus;
import com.asakaa.synthesis.domain.entity.Patient;
import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.repository.ConsultationRepository;
import com.asakaa.synthesis.repository.PatientRepository;
import com.asakaa.synthesis.repository.ProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import com.asakaa.synthesis.security.ClinicAccessGuard;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ClinicAccessGuard clinicAccessGuard;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ConsultationService consultationService;

    private Patient patient;
    private Provider provider;
    private Consultation consultation;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();

        provider = Provider.builder()
                .id(1L)
                .name("Dr. Smith")
                .build();

        consultation = Consultation.builder()
                .id(1L)
                .patient(patient)
                .provider(provider)
                .status(ConsultationStatus.OPEN)
                .chiefComplaint("Fever")
                .vitals("{\"temp\": 38}")
                .openedAt(LocalDateTime.now())
                .diagnoses(new ArrayList<>())
                .build();
    }

    @Test
    void openConsultation_SetsStatusToOpenAndOpenedAtToNow() {
        // Arrange
        ConsultationRequest request = ConsultationRequest.builder()
                .patientId(1L)
                .chiefComplaint("Fever")
                .vitals("{\"temp\": 38}")
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(consultation);

        // Act
        ConsultationResponse response = consultationService.openConsultation(request, 1L, authentication);

        // Assert
        ArgumentCaptor<Consultation> captor = ArgumentCaptor.forClass(Consultation.class);
        verify(consultationRepository).save(captor.capture());
        
        Consultation saved = captor.getValue();
        assertEquals(ConsultationStatus.OPEN, saved.getStatus());
        assertNotNull(saved.getOpenedAt());
        assertNull(saved.getClosedAt());
    }

    @Test
    void closeConsultation_SetsStatusToClosedAndClosedAtToNow() {
        // Arrange
        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(consultation);

        // Act
        ConsultationResponse response = consultationService.closeConsultation(1L, authentication);

        // Assert
        ArgumentCaptor<Consultation> captor = ArgumentCaptor.forClass(Consultation.class);
        verify(consultationRepository).save(captor.capture());
        
        Consultation saved = captor.getValue();
        assertEquals(ConsultationStatus.CLOSED, saved.getStatus());
        assertNotNull(saved.getClosedAt());
    }

    @Test
    void updateConsultation_PersistsUpdatedVitals() {
        // Arrange
        ConsultationUpdateRequest request = ConsultationUpdateRequest.builder()
                .vitals("{\"temp\": 39}")
                .notes("Patient condition worsening")
                .build();

        when(consultationRepository.findById(1L)).thenReturn(Optional.of(consultation));
        when(consultationRepository.save(any(Consultation.class))).thenReturn(consultation);

        // Act
        ConsultationResponse response = consultationService.updateConsultation(1L, request, authentication);

        // Assert
        ArgumentCaptor<Consultation> captor = ArgumentCaptor.forClass(Consultation.class);
        verify(consultationRepository).save(captor.capture());
        
        Consultation saved = captor.getValue();
        assertEquals("{\"temp\": 39}", saved.getVitals());
        assertEquals("Patient condition worsening", saved.getNotes());
    }
}
