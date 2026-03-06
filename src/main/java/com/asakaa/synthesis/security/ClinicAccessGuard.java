package com.asakaa.synthesis.security;

import com.asakaa.synthesis.domain.entity.Consultation;
import com.asakaa.synthesis.domain.entity.Patient;
import com.asakaa.synthesis.domain.entity.Provider;
import com.asakaa.synthesis.exception.ClinicAccessDeniedException;
import com.asakaa.synthesis.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClinicAccessGuard {

    private static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    private final ProviderRepository providerRepository;

    /**
     * Resolves the authenticated provider from the security context.
     */
    public Provider getCurrentProvider(Authentication authentication) {
        String email = authentication.getName();
        return providerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated provider not found: " + email));
    }

    /**
     * Returns true if the authenticated user has the SUPER_ADMIN role.
     */
    public boolean isSuperAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + SUPER_ADMIN_ROLE));
    }

    /**
     * Verifies the authenticated provider has access to the given patient.
     * Super-admins bypass this check.
     */
    public void verifyPatientAccess(Authentication authentication, Patient patient) {
        if (isSuperAdmin(authentication)) {
            return;
        }

        Provider provider = getCurrentProvider(authentication);

        if (provider.getClinic() == null) {
            throw new ClinicAccessDeniedException("Provider is not associated with any clinic");
        }

        if (patient.getClinic() == null) {
            throw new ClinicAccessDeniedException("Patient is not associated with any clinic");
        }

        if (!provider.getClinic().getId().equals(patient.getClinic().getId())) {
            throw new ClinicAccessDeniedException(
                    "Access denied: patient belongs to a different clinic");
        }
    }

    /**
     * Verifies the authenticated provider has access to the given consultation
     * by checking the consultation's patient clinic.
     */
    public void verifyConsultationAccess(Authentication authentication, Consultation consultation) {
        verifyPatientAccess(authentication, consultation.getPatient());
    }
}
