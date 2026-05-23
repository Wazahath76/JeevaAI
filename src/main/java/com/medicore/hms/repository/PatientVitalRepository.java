package com.medicore.hms.repository;

import com.medicore.hms.entity.PatientVital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PatientVitalRepository extends JpaRepository<PatientVital, UUID> {

    // All vitals for a patient, newest first
    List<PatientVital> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    // Latest N vitals for chart
    @Query("SELECT v FROM PatientVital v WHERE v.patient.id = :patientId " +
           "ORDER BY v.createdAt DESC LIMIT :limit")
    List<PatientVital> findLatestVitals(@Param("patientId") UUID patientId, @Param("limit") int limit);

    // Vitals in date range
    @Query("SELECT v FROM PatientVital v WHERE v.patient.id = :patientId " +
           "AND v.createdAt BETWEEN :from AND :to ORDER BY v.createdAt ASC")
    List<PatientVital> findByPatientAndDateRange(
        @Param("patientId") UUID patientId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );
}
