package com.projects.secure_notes_ai.dto.Notes;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NoteRequest {
    private String title;
    private String content;
    private UUID userId;
}
