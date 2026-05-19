package com.projects.secure_notes_ai.dto.Auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UUID uuid;
    private String email;
}
