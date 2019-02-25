package me.mocha.backend.post.model.repository;

import me.mocha.backend.post.model.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
