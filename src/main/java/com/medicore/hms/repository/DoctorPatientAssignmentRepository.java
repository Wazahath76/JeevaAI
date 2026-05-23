package com.medicore.hms.repository;

import com.medicore.hms.entity.DoctorPatientAssignment;
import com.medicore.hms.enums.AssignmentStatus;
import com.medicore.hms.enums.DoctorRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorPatientAssignmentRepository extends JpaRepository<DoctorPatientAssignment, UUID> {

    List<DoctorPatientAssignment> findByPatientIdAndStatus(UUID patientId, AssignmentStatus status);

    List<DoctorPatientAssignment> findByAssignedDoctorIdAndStatus(UUID doctorId, AssignmentStatus status);

    boolean existsByPatientIdAndAssignedDoctorIdAndStatus(UUID patientId, UUID doctorId, AssignmentStatus status);

    @Query("SELECT a FROM DoctorPatientAssignment a " +
           "JOIN FETCH a.assignedDoctor " +
           "JOIN FETCH a.patient " +
           "WHERE a.patient.id = :patientId AND a.status = 'ACTIVE'")
    List<DoctorPatientAssignment> findActiveAssignmentsForPatient(@Param("patientId") UUID patientId);

    @Query("SELECT a FROM DoctorPatientAssignment a " +
           "WHERE a.patient.id = :patientId " +
           "AND a.assignedDoctor.id = :doctorId " +
           "AND a.status = :status")
    Optional<DoctorPatientAssignment> findByPatientAndDoctorAndStatus(
        @Param("patientId") UUID patientId,
        @Param("doctorId") UUID doctorId,
        @Param("status") AssignmentStatus status
    );

    // Count active patients for a doctor
    @Query("SELECT COUNT(a) FROM DoctorPatientAssignment a WHERE a.assignedDoctor.id = :doctorId AND a.status = 'ACTIVE'")
    long countActivePatients(@Param("doctorId") UUID doctorId);
}
