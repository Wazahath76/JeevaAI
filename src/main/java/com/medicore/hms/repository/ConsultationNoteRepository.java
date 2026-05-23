package com.medicore.hms.repository;

import com.medicore.hms.entity.ConsultationNote;
import com.medicore.hms.enums.NoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConsultationNoteRepository extends JpaRepository<ConsultationNote, UUID> {

    List<ConsultationNote> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<ConsultationNote> findByPatientIdAndNoteType(UUID patientId, NoteType noteType);

    // Super specialist can see all notes shared with them
    @Query("SELECT n FROM ConsultationNote n WHERE n.patient.id = :patientId " +
           "AND n.sharedWithSuperSpecialist = true ORDER BY n.createdAt DESC")
    List<ConsultationNote> findSharedNotesForPatient(@Param("patientId") UUID patientId);

    List<ConsultationNote> findByAuthorIdOrderByCreatedAtDesc(UUID authorId);
}
