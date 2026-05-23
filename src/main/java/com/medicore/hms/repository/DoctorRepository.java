package com.medicore.hms.repository;

import com.medicore.hms.entity.Doctor;
import com.medicore.hms.enums.DoctorRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByLicenseNumber(String licenseNumber);

    List<Doctor> findByRoleAndIsAvailableTrueAndIsActiveTrue(DoctorRole role);

    List<Doctor> findByDepartmentAndIsActiveTrue(String department);

    @Query("SELECT d FROM Doctor d WHERE d.isActive = true AND d.isAvailable = true " +
           "AND (:role IS NULL OR d.role = :role) " +
           "AND (:department IS NULL OR d.department = :department) " +
           "ORDER BY d.fullName ASC")
    List<Doctor> findAvailableDoctors(
        @Param("role") DoctorRole role,
        @Param("department") String department
    );

    @Query("SELECT d FROM Doctor d WHERE d.isActive = true " +
           "AND (LOWER(d.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Doctor> searchDoctors(@Param("search") String search, Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.role IN :roles AND d.isActive = true")
    List<Doctor> findByRoles(@Param("roles") List<DoctorRole> roles);

    // For analytics: count by department
    @Query("SELECT d.department, COUNT(d) FROM Doctor d WHERE d.isActive = true GROUP BY d.department")
    List<Object[]> countByDepartment();

    // For analytics: count by role
    @Query("SELECT d.role, COUNT(d) FROM Doctor d WHERE d.isActive = true GROUP BY d.role")
    List<Object[]> countByRole();
}
