package com.contentiq.contentiq.repository;

import com.contentiq.contentiq.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findByVideoId(String videoId);

    List<Comment> findByVideoIdAndAnalyzed(String videoId, Boolean analyzed);

    long countByVideoIdAndAnalyzed(String videoId, Boolean analyzed);
}
