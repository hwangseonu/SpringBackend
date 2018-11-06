package me.mocha.backend.model.repository;

import me.mocha.backend.model.entity.Post;
import me.mocha.backend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByWriter(User user);
}
