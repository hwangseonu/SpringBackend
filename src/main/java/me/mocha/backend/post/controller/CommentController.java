package me.mocha.backend.post.controller;

import me.mocha.backend.common.annotation.CurrentUser;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.post.model.entity.Comment;
import me.mocha.backend.post.model.entity.Post;
import me.mocha.backend.post.model.repository.PostRepository;
import me.mocha.backend.post.request.AddCommentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("/posts/{pid}/comments")
public class CommentController {

    private final PostRepository postRepository;

    @Autowired
    public CommentController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @PostMapping
    public ResponseEntity<?> addComment(@PathVariable("pid") long pid, @CurrentUser User user, @Valid @RequestBody AddCommentRequest request) {
        Post post = postRepository.findById(pid).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();
        post.getComments().add(Comment.builder()
                .content(request.getContent())
                .createAt(new Date())
                .updateAt(new Date())
                .writer(user)
                .build());
        postRepository.save(post);
        return ResponseEntity.ok().build();
    }

}
