package com.projects.secure_notes_ai.dto.Notes;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class UpdateNoteRequest {
    private UUID noteId;
    private String title;
    private String content;
    private LocalDateTime updatedAt;
    private UUID userId;
}
