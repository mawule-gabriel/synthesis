package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.PatientRequest;
import com.asakaa.synthesis.domain.dto.response.PatientResponse;
import com.asakaa.synthesis.domain.entity.Patient;
import com.asakaa.synthesis.exception.ResourceNotFoundException;
import com.asakaa.synthesis.exception.ValidationException;
import com.asakaa.synthesis.repository.PatientRepository;
import com.asakaa.synthesis.util.PatientMapper;
import com.asakaa.synthesis.security.ClinicAccessGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @Mock
    private ClinicAccessGuard clinicAccessGuard;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private PatientService patientService;

    private PatientRequest request;
    private Patient patient;
    private PatientResponse response;

    @BeforeEach
    void setUp() {
        request = PatientRequest.builder()
                .nationalId("12345")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("Male")
                .build();

        patient = Patient.builder()
                .id(1L)
                .nationalId("12345")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("Male")
                .build();

        response = PatientResponse.builder()
                .id(1L)
                .nationalId("12345")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender("Male")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createPatient_SavesAndReturnsResponse() {
        // Arrange
        when(patientRepository.existsByNationalId("12345")).thenReturn(false);
        when(patientMapper.toEntity(request)).thenReturn(patient);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(response);

        // Act
        PatientResponse result = patientService.createPatient(request, authentication);

        // Assert
        assertNotNull(result);
        assertEquals("12345", result.getNationalId());
        assertEquals("John", result.getFirstName());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void createPatient_ThrowsValidationException_WhenNationalIdExists() {
        // Arrange
        when(patientRepository.existsByNationalId("12345")).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> patientService.createPatient(request, authentication));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void getPatientById_ThrowsResourceNotFoundException_ForUnknownId() {
        // Arrange
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(999L, authentication));
    }
}
