package com.medicore.hms.repository;

import com.medicore.hms.entity.SurgicalPlan;
import com.medicore.hms.enums.SurgicalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SurgicalPlanRepository extends JpaRepository<SurgicalPlan, UUID> {
    List<SurgicalPlan> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    List<SurgicalPlan> findBySurgeonIdAndStatus(UUID surgeonId, SurgicalStatus status);
    List<SurgicalPlan> findByAnaesthetistIdAndStatus(UUID anaesthetistId, SurgicalStatus status);

    @Query("SELECT s FROM SurgicalPlan s JOIN FETCH s.surgeon JOIN FETCH s.anaesthetist " +
           "WHERE s.patient.id = :patientId ORDER BY s.createdAt DESC")
    List<SurgicalPlan> findByPatientWithDoctors(@Param("patientId") UUID patientId);
}
