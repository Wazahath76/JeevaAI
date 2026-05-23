package com.medicore.hms.entity;

import com.medicore.hms.enums.AssignmentStatus;
import com.medicore.hms.enums.DoctorRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "doctor_patient_assignments",
    indexes = {
        @Index(name = "idx_assign_patient", columnList = "patient_id"),
        @Index(name = "idx_assign_doctor", columnList = "assigned_doctor_id"),
        @Index(name = "idx_assign_status", columnList = "status")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_active_assignment",
            columnNames = {"patient_id", "assigned_doctor_id", "status"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorPatientAssignment extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_doctor_id", nullable = false)
    private Doctor assignedDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private Doctor assignedBy;

    // Role at time of assignment (doctor role can change)
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_role", nullable = false, length = 50)
    private DoctorRole assignmentRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.ACTIVE;

    @Column(name = "assignment_reason", columnDefinition = "TEXT")
    private String assignmentReason;

    @Column(name = "revocation_reason", columnDefinition = "TEXT")
    private String revocationReason;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
