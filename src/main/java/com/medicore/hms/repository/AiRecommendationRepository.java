package com.medicore.hms.repository;

import com.medicore.hms.entity.AiRecommendation;
import com.medicore.hms.enums.RecommendationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AiRecommendationRepository extends JpaRepository<AiRecommendation, UUID> {

    List<AiRecommendation> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<AiRecommendation> findByPatientIdAndStatus(UUID patientId, RecommendationStatus status);

    List<AiRecommendation> findByRequestedByIdOrderByCreatedAtDesc(UUID doctorId);

    // Analytics: approval rates per doctor
    @Query("SELECT a.reviewedBy.id, a.status, COUNT(a) FROM AiRecommendation a " +
           "WHERE a.reviewedBy IS NOT NULL GROUP BY a.reviewedBy.id, a.status")
    List<Object[]> getReviewStatsByDoctor();

    // Hospital-wide AI stats
    @Query("SELECT a.status, COUNT(a) FROM AiRecommendation a GROUP BY a.status")
    List<Object[]> countByStatus();

    @Query("SELECT COUNT(a) FROM AiRecommendation a WHERE a.requestedBy.id = :doctorId AND a.status = :status")
    long countByDoctorAndStatus(@Param("doctorId") UUID doctorId, @Param("status") RecommendationStatus status);
}
