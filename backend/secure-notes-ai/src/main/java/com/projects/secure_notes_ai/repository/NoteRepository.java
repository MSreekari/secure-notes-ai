package com.projects.secure_notes_ai.repository;

import com.projects.secure_notes_ai.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    List<Note> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<Note> findByUser_IdAndIsEncryptedTrue(UUID userId);

    List<Note> findByUserIdAndTitleContainingIgnoreCaseOrUserIdAndSummaryContainingIgnoreCase(
            UUID userId1, String titleKeyword,
            UUID userId2, String summaryKeyword
    );
}
