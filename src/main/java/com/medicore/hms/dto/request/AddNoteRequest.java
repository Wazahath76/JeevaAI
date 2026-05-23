package com.medicore.hms.dto.request;

import com.medicore.hms.enums.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddNoteRequest {
    @NotNull(message = "Note type is required")
    private NoteType noteType;

    @NotBlank(message = "Note content is required")
    private String content;

    private Boolean isVisibleToAll = true;
    private Boolean sharedWithSuperSpecialist = true;
}
