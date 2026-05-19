package com.projects.secure_notes_ai.controller;

import com.projects.secure_notes_ai.dto.Auth.AuthResponse;
import com.projects.secure_notes_ai.dto.Auth.LoginRequest;
import com.projects.secure_notes_ai.dto.Auth.RegisterRequest;
import com.projects.secure_notes_ai.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    /**
     * Endpoint to handle new user registration
     * POST http://localhost:8080/api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        try{
            String message = authService.registerUser(request);
            return new ResponseEntity<>(message, HttpStatus.OK);
        }catch(RuntimeException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    /**
     * Endpoint to handle user authentication and token issuance
     * POST http://localhost:8080/api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try{
            AuthResponse authResponse = authService.loginUser(request);
            return ResponseEntity.ok(authResponse);
        }catch(RuntimeException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
