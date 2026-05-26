package com.projects.secure_notes_ai.service;

import com.projects.secure_notes_ai.dto.Notes.NoteRequest;
import com.projects.secure_notes_ai.dto.Notes.NoteResponse;
import com.projects.secure_notes_ai.dto.Notes.UpdateNoteRequest;
import com.projects.secure_notes_ai.entity.Note;
import com.projects.secure_notes_ai.repository.NoteRepository;
import com.projects.secure_notes_ai.repository.UserRepository;
import com.projects.secure_notes_ai.util.EncryptionUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final AIService aiService;
    private final EncryptionUtil encryptionUtil;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository, AIService aiService, EncryptionUtil encryptionUtil) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.aiService = aiService;
        this.encryptionUtil = encryptionUtil;
    }
    // create a note
    public NoteResponse createNote(NoteRequest noteRequest){
        if(!userRepository.existsById(noteRequest.getUserId())){
            throw new RuntimeException("User does not exist");
        }
        String content = noteRequest.getContent();
        boolean isSensitive = aiService.isNoteSensitive(content);
        String storedContent;
        boolean isEncrypted;
        if(isSensitive){
            storedContent = encryptionUtil.encrypt(content);
            isEncrypted = true;
        }else{
            storedContent = content;
            isEncrypted = false;
        }
        String summary = aiService.generateSummary(content);
        List<String> keywords = aiService.extractKeywords(content);

        Note note = new Note();
        note.setUser(userRepository.findById(noteRequest.getUserId()).get());
        note.setTitle(noteRequest.getTitle());
        note.setContent(storedContent);
        note.setIsSensitive(isSensitive);
        note.setIsEncrypted(isEncrypted);
        note.setSummary(summary);
        note.setKeywords(keywords);
        Note savedNote = noteRepository.save(note);
        return new NoteResponse(
                savedNote.getId(),
                savedNote.getTitle(),
                content,
                savedNote.getSummary(),
                savedNote.getKeywords(),
                savedNote.getIsEncrypted(),
                savedNote.getCreatedAt(),
                savedNote.getUpdatedAt()
        );
    }
    // get notes
    public List<NoteResponse> getNotesForUser(UUID userId){
        List<Note> notes = noteRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        return notes.stream()
                .map( note -> {
                    String displayContent = note.getIsEncrypted() ? "Encypted" : note.getContent();

                    return new NoteResponse(
                            note.getId(),
                            note.getTitle(),
                            displayContent,
                            note.getSummary(),
                            note.getKeywords(),
                            note.getIsEncrypted(),
                            note.getCreatedAt(),
                            note.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }
    // search notes
    public List<NoteResponse> searchNotes(UUID userId, String keyword){
        List<Note> notes = noteRepository.findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndSummaryContainingIgnoreCase(
                userId, keyword,
                userId, keyword
        );

        return notes.stream()
                .map(note -> {
                    String displayContent = note.getIsEncrypted() ? "Encypted" : note.getContent();

                    return new NoteResponse(
                            note.getId(),
                            note.getTitle(),
                            displayContent,
                            note.getSummary(),
                            note.getKeywords(),
                            note.getIsEncrypted(),
                            note.getCreatedAt(),
                            note.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }
    // update notes
    public NoteResponse updateNotes(UpdateNoteRequest updateNoteRequest){
        Note note = noteRepository.findById(updateNoteRequest.getNoteId())
                .orElseThrow(() -> new RuntimeException("Note not found"));
        if(!userRepository.existsById(updateNoteRequest.getUserId())){
            throw new RuntimeException("User does not exist");
        }
        if(!note.getUser().getId().equals(updateNoteRequest.getUserId())){
            throw new RuntimeException("Unauthorized: You do not own this note");
        }
        String newContent = updateNoteRequest.getContent();
        note.setTitle(updateNoteRequest.getTitle());
        boolean isSensitive = aiService.isNoteSensitive(newContent);
        String summary = aiService.generateSummary(newContent);
        List<String> keywords = aiService.extractKeywords(newContent);
        String storedContent;
        boolean isEncrypted;
        if(isSensitive){
            storedContent = encryptionUtil.encrypt(newContent);
            isEncrypted = true;
        }else{
            storedContent = newContent;
            isEncrypted = false;
        }
        note.setContent(storedContent);
        note.setIsSensitive(isSensitive);
        note.setIsEncrypted(isEncrypted);
        note.setSummary(summary);
        note.setKeywords(keywords);
        Note updatedNote =  noteRepository.save(note);
        return new NoteResponse(
                updatedNote.getId(),
                updatedNote.getTitle(),
                newContent,
                updatedNote.getSummary(),
                updatedNote.getKeywords(),
                updatedNote.getIsEncrypted(),
                updatedNote.getCreatedAt(),
                updatedNote.getUpdatedAt()
        );
    }
    // delete a note
    public void deleteNotes(@RequestParam UUID userId, @RequestParam UUID noteId){
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        if(!userRepository.existsById(userId)){
            throw new RuntimeException("User does not exist");
        }
        if(!note.getUser().getId().equals(userId)){
            throw new RuntimeException("Unauthorized: You do not own this note");
        }
        noteRepository.delete(note);
    }
    // get note securely
    public NoteResponse getNoteByIdSecurely(UUID userId, UUID noteId){
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        if(!userRepository.existsById(userId)){
            throw new RuntimeException("User does not exist");
        }
        if(!note.getUser().getId().equals(userId)){
            throw new RuntimeException("Unauthorized: You do not own this note");
        }
        String decryptedContent = "";
        if(note.getIsEncrypted()){
            decryptedContent = encryptionUtil.decrypt(note.getContent());
        }else{
            decryptedContent = note.getContent();
        }
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                decryptedContent,
                note.getSummary(),
                note.getKeywords(),
                note.getIsEncrypted(),
                note.getCreatedAt(),
                note.getUpdatedAt()
        );
    }
}
