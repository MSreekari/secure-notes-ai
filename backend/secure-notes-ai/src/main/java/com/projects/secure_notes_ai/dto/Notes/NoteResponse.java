package com.projects.secure_notes_ai.dto.Notes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class NoteResponse {
    private UUID noteId;
    private String title;
    private String content;
    private String summary;
    private List<String> keywords;
    private Boolean isEncrypted;
    private LocalDateTime createdAt;
}
