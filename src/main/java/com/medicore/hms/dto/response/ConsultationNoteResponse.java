package com.medicore.hms.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medicore.hms.enums.NoteType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConsultationNoteResponse {
    private final UUID id;
    private final NoteType noteType;
    private final String content;
    private final Boolean isVisibleToAll;
    private final Boolean sharedWithSuperSpecialist;
    private final Boolean isLocked;
    private final String authorName;
    private final String authorRole;
    private final LocalDateTime createdAt;
}
