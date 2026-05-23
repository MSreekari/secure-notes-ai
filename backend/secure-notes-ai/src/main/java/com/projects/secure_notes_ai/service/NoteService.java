package com.projects.secure_notes_ai.service;

import com.projects.secure_notes_ai.dto.Notes.NoteRequest;
import com.projects.secure_notes_ai.dto.Notes.NoteResponse;
import com.projects.secure_notes_ai.entity.Note;
import com.projects.secure_notes_ai.repository.NoteRepository;
import com.projects.secure_notes_ai.repository.UserRepository;
import com.projects.secure_notes_ai.util.EncryptionUtil;
import org.springframework.stereotype.Service;

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
                savedNote.getCreatedAt()
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
                            note.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }
    // search notes
    public List<NoteResponse> serachNotes(UUID userId, String keyword){
        List<Note> notes = noteRepository.searchNotesByKeyword(userId, keyword);
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
                            note.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }
}
