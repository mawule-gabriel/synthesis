package com.asakaa.synthesis.service;

import com.asakaa.synthesis.domain.dto.request.AuthRequest;
import com.asakaa.synthesis.domain.dto.request.RegisterRequest;
import com.asakaa.synthesis.domain.dto.response.AuthResponse;
import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.exception.ValidationException;
import com.asakaa.synthesis.repository.ClinicRepository;
import com.asakaa.synthesis.repository.ProviderRepository;
import com.asakaa.synthesis.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private Provider provider;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("Dr. Smith")
                .role("Doctor")
                .clinicRegistrationCode(null)
                .region("North")
                .email("smith@example.com")
                .password("password123")
                .build();

        authRequest = AuthRequest.builder()
                .email("smith@example.com")
                .password("password123")
                .build();

        provider = new Provider();
        provider.setId(1L);
        provider.setName("Dr. Smith");
        provider.setRole("Doctor");
        provider.setEmail("smith@example.com");
        provider.setPasswordHash("hashedPassword");
    }

    @Test
    void register_SavesProviderAndReturnsToken() {
        // Arrange
        when(providerRepository.existsByEmail("smith@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");
        when(providerRepository.save(any(Provider.class))).thenReturn(provider);
        when(jwtUtil.generateToken("smith@example.com")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("smith@example.com", response.getEmail());
        assertEquals("Dr. Smith", response.getName());
        verify(providerRepository).save(any(Provider.class));
    }

    @Test
    void register_ThrowsValidationException_WhenEmailExists() {
        // Arrange
        when(providerRepository.existsByEmail("smith@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ValidationException.class, () -> authService.register(registerRequest));
        verify(providerRepository, never()).save(any(Provider.class));
    }

    @Test
    void login_ReturnsToken_ForValidCredentials() {
        // Arrange
        when(providerRepository.findByEmail("smith@example.com")).thenReturn(Optional.of(provider));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("smith@example.com")).thenReturn("jwt-token");

        // Act
        AuthResponse response = authService.login(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("smith@example.com", response.getEmail());
    }

    @Test
    void login_ThrowsValidationException_ForWrongPassword() {
        // Arrange
        when(providerRepository.findByEmail("smith@example.com")).thenReturn(Optional.of(provider));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(ValidationException.class, () -> authService.login(authRequest));
        verify(jwtUtil, never()).generateToken(anyString());
    }
}
