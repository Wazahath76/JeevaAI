package com.medicore.hms.repository;

import com.medicore.hms.entity.Patient;
import com.medicore.hms.enums.PatientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByPatientCode(String patientCode);

    boolean existsByPatientCode(String patientCode);

    // For generating next patient code
    @Query("SELECT COUNT(p) FROM Patient p WHERE YEAR(p.createdAt) = :year")
    long countByYear(@Param("year") int year);

    // Primary doctor's patients
    Page<Patient> findByPrimaryDoctorId(UUID doctorId, Pageable pageable);

    List<Patient> findByPrimaryDoctorIdAndStatus(UUID doctorId, PatientStatus status);

    // Full text search across name, code, phone
    @Query("SELECT p FROM Patient p WHERE " +
           "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.patientCode) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR p.phone LIKE CONCAT('%', :search, '%')")
    Page<Patient> searchPatients(@Param("search") String search, Pageable pageable);

    // Filter by status
    Page<Patient> findByStatus(PatientStatus status, Pageable pageable);

    // Patients assigned to a doctor (via assignment table)
    @Query("SELECT DISTINCT p FROM Patient p " +
           "JOIN p.assignments a " +
           "WHERE a.assignedDoctor.id = :doctorId AND a.status = 'ACTIVE'")
    List<Patient> findAssignedPatients(@Param("doctorId") UUID doctorId);

    // Super specialist view: all patients assigned to their consultees
    @Query("SELECT DISTINCT p FROM Patient p " +
           "JOIN p.assignments a " +
           "WHERE a.assignedDoctor.id = :superSpecialistId " +
           "AND a.status = 'ACTIVE'")
    Page<Patient> findPatientsForSuperSpecialist(
        @Param("superSpecialistId") UUID superSpecialistId,
        Pageable pageable
    );

    // Critical/IPD patients
    List<Patient> findByStatusIn(List<PatientStatus> statuses);

    // Analytics: admissions over time
    @Query("SELECT DATE(p.admissionDate), COUNT(p) FROM Patient p " +
           "WHERE p.admissionDate BETWEEN :from AND :to " +
           "GROUP BY DATE(p.admissionDate) ORDER BY DATE(p.admissionDate)")
    List<Object[]> countAdmissionsByDate(
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    // Analytics: count by status
    @Query("SELECT p.status, COUNT(p) FROM Patient p GROUP BY p.status")
    List<Object[]> countByStatus();

    // Fetch patient with all associations eagerly for EMR view
    @Query("SELECT DISTINCT p FROM Patient p " +
           "LEFT JOIN FETCH p.primaryDoctor " +
           "LEFT JOIN FETCH p.admittedBy " +
           "WHERE p.id = :id")
    Optional<Patient> findByIdWithDoctors(@Param("id") UUID id);
}
