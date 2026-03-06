package com.asakaa.synthesis.repository;

import com.asakaa.synthesis.domain.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByNationalId(String nationalId);

    List<Patient> findByClinicId(Long clinicId);

    boolean existsByNationalId(String nationalId);

    Page<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    // Clinic-scoped queries
    Page<Patient> findByClinicId(Long clinicId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(
            "SELECT p FROM Patient p WHERE p.clinic.id = :clinicId AND " +
            "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Patient> searchByClinicId(@org.springframework.data.repository.query.Param("clinicId") Long clinicId,
                                   @org.springframework.data.repository.query.Param("query") String query,
                                   Pageable pageable);
}

