package com.medicore.hms.repository;

import com.medicore.hms.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, UUID> {

    List<Diagnosis> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<Diagnosis> findByPatientIdAndIsActiveTrue(UUID patientId);

    @Query("SELECT d.diagnosisName, COUNT(d) FROM Diagnosis d GROUP BY d.diagnosisName ORDER BY COUNT(d) DESC LIMIT 10")
    List<Object[]> findTopDiagnoses();
}
