package me.mocha.backend.post.model.repository;

import me.mocha.backend.post.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
