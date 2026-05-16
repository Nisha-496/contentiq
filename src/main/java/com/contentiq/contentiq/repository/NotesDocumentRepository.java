package com.contentiq.contentiq.repository;

import com.contentiq.contentiq.model.NotesDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotesDocumentRepository extends MongoRepository<NotesDocument, String> {

    List<NotesDocument> findByVideoId(String videoId);
}
