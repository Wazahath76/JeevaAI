package com.medicore.hms.entity;

import com.medicore.hms.enums.NoteType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(
    name = "consultation_notes",
    indexes = {
        @Index(name = "idx_note_patient", columnList = "patient_id"),
        @Index(name = "idx_note_author", columnList = "author_id"),
        @Index(name = "idx_note_type", columnList = "note_type")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationNote extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Doctor author;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "note_type", nullable = false, length = 30)
    private NoteType noteType;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_visible_to_all", nullable = false)
    @Builder.Default
    private Boolean isVisibleToAll = true;

    // Immutable flag - notes cannot be edited once created (audit trail)
    @Column(name = "is_locked", nullable = false)
    @Builder.Default
    private Boolean isLocked = false;

    @Column(name = "shared_with_super_specialist", nullable = false)
    @Builder.Default
    private Boolean sharedWithSuperSpecialist = true;
}
