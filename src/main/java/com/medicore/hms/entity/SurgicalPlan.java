package com.medicore.hms.entity;

import com.medicore.hms.enums.SurgicalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "surgical_plans",
    indexes = {
        @Index(name = "idx_surgery_patient", columnList = "patient_id"),
        @Index(name = "idx_surgery_surgeon", columnList = "surgeon_id"),
        @Index(name = "idx_surgery_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SurgicalPlan extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surgeon_id")
    private Doctor surgeon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "anaesthetist_id")
    private Doctor anaesthetist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Doctor createdBy;

    @NotBlank
    @Column(name = "procedure_name", nullable = false, length = 500)
    private String procedureName;

    @Column(name = "procedure_code", length = 50)
    private String procedureCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private SurgicalStatus status = SurgicalStatus.PLANNED;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "anaesthesia_type", length = 100)
    private String anaesthesiaType;

    @Column(name = "pre_op_notes", columnDefinition = "TEXT")
    private String preOpNotes;

    @Column(name = "intra_op_notes", columnDefinition = "TEXT")
    private String intraOpNotes;

    @Column(name = "post_op_notes", columnDefinition = "TEXT")
    private String postOpNotes;

    @Column(name = "complications", columnDefinition = "TEXT")
    private String complications;

    @Column(name = "theatre_number", length = 20)
    private String theatreNumber;
}
