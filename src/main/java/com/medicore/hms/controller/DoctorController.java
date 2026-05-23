package com.medicore.hms.controller;

import com.medicore.hms.dto.response.ApiResponse;
import com.medicore.hms.dto.response.DoctorResponse;
import com.medicore.hms.dto.response.PagedResponse;
import com.medicore.hms.entity.Doctor;
import com.medicore.hms.enums.DoctorRole;
import com.medicore.hms.exception.ResourceNotFoundException;
import com.medicore.hms.repository.DoctorRepository;
import com.medicore.hms.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final SecurityUtil     securityUtil;

    /** GET /api/doctors - paginated list (Admin only) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<DoctorResponse>>> getAllDoctors(
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false)    String search
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        var pagedDoctors = (search != null && !search.isBlank())
            ? doctorRepository.searchDoctors(search, pageable)
            : doctorRepository.findAll(pageable);

        return ResponseEntity.ok(
            ApiResponse.success(PagedResponse.from(pagedDoctors.map(this::toResponse)))
        );
    }

    /** GET /api/doctors/available - list available doctors (filter by role/dept) */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAvailable(
        @RequestParam(required = false) DoctorRole role,
        @RequestParam(required = false) String department
    ) {
        List<Doctor> doctors = doctorRepository.findAvailableDoctors(role, department);
        return ResponseEntity.ok(
            ApiResponse.success(doctors.stream().map(this::toResponse).toList())
        );
    }

    /** GET /api/doctors/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorResponse>> getById(@PathVariable UUID id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        return ResponseEntity.ok(ApiResponse.success(toResponse(doctor)));
    }

    /** PATCH /api/doctors/{id}/availability */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasRole('ADMIN') or @securityUtil.getCurrentDoctor().id == #id")
    public ResponseEntity<ApiResponse<DoctorResponse>> toggleAvailability(
        @PathVariable UUID id,
        @RequestParam boolean available
    ) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        doctor.setIsAvailable(available);
        doctorRepository.save(doctor);
        return ResponseEntity.ok(ApiResponse.success("Availability updated", toResponse(doctor)));
    }

    /** PATCH /api/doctors/{id}/deactivate - Admin deactivates a doctor */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable UUID id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id));
        doctor.setIsActive(false);
        doctor.setIsAvailable(false);
        doctorRepository.save(doctor);
        return ResponseEntity.ok(ApiResponse.success("Doctor deactivated"));
    }

    // ── Mapper ───────────────────────────────────────────────────────────────

    private DoctorResponse toResponse(Doctor d) {
        return DoctorResponse.builder()
            .id(d.getId())
            .fullName(d.getFullName())
            .email(d.getEmail())
            .role(d.getRole())
            .specialization(d.getSpecialization())
            .department(d.getDepartment())
            .licenseNumber(d.getLicenseNumber())
            .phone(d.getPhone())
            .qualification(d.getQualification())
            .experienceYears(d.getExperienceYears())
            .isActive(d.getIsActive())
            .isAvailable(d.getIsAvailable())
            .profileImageUrl(d.getProfileImageUrl())
            .createdAt(d.getCreatedAt())
            .build();
    }
}
