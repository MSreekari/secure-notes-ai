package com.projects.secure_notes_ai.controller;

import com.projects.secure_notes_ai.dto.Notes.NoteRequest;
import com.projects.secure_notes_ai.dto.Notes.NoteResponse;
import com.projects.secure_notes_ai.dto.Notes.UpdateNoteRequest;
import com.projects.secure_notes_ai.entity.Note;
import com.projects.secure_notes_ai.service.NoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*")
public class NotesController {
    private final NoteService noteService;
    public NotesController(NoteService noteService) {
        this.noteService = noteService;
    }
    /**
     * Endpoint to handle new post creation
     * POST http://localhost:8080/api/notes/new
     */
    @PostMapping("/new")
    public ResponseEntity<NoteResponse> createNote(@RequestBody NoteRequest noteRequest) {
        try{
            NoteResponse noteResponse = noteService.createNote(noteRequest);
            return ResponseEntity.ok(noteResponse);
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    /**
     * Endpoint to handle new post creation
     * GET http://localhost:8080/api/notes/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NoteResponse>> getAllNotes(@PathVariable UUID userId) {
        try{
            List<NoteResponse> notes = noteService.getNotesForUser(userId);
            return ResponseEntity.ok(notes);
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    /**
     * Endpoint to handle new post creation
     * POST http://localhost:8080/api/notes/search
     */
    @GetMapping("/search")
    public ResponseEntity<List<NoteResponse>> searchNotes(@RequestParam UUID userId, @RequestParam String keyword) {
        try{
            List<NoteResponse> matchedNotes = noteService.searchNotes(userId, keyword);
            return ResponseEntity.ok(matchedNotes);
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    /**
     * Endpoint to handle update post
     * PUT http://localhost:8080/api/notes/update
     */
    @PutMapping("/update")
    public ResponseEntity<NoteResponse> updateNotes(@RequestBody UpdateNoteRequest updateNoteRequest) {
        try{
            NoteResponse response = noteService.updateNotes(updateNoteRequest);
            return ResponseEntity.ok(response);
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    /**
     * Endpoint to handle update post
     * DELETE http://localhost:8080/api/notes/delete
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteNotes(@RequestParam UUID userId, UUID noteId) {
        try{
            noteService.deleteNotes(userId, noteId);
            return ResponseEntity.ok("Note deleted");
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
    /**
     * Endpoint to handle update post
     * POST http://localhost:8080/api/notes/view
     */
    @GetMapping("/view")
    public ResponseEntity<NoteResponse> getNoteByIdSecurely(@RequestParam UUID userId, @RequestParam UUID noteId) {
        try{
            NoteResponse response = noteService.getNoteByIdSecurely(userId, noteId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
