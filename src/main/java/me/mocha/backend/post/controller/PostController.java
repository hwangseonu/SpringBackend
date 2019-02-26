package me.mocha.backend.post.controller;

import me.mocha.backend.common.annotation.CurrentUser;
import me.mocha.backend.common.model.entity.User;
import me.mocha.backend.post.model.entity.Post;
import me.mocha.backend.post.model.repository.PostRepository;
import me.mocha.backend.post.request.NewPostRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;

    @Autowired
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @PostMapping
    public ResponseEntity<Post> newPost(@CurrentUser User user, @Valid @RequestBody NewPostRequest request) {
        Post post = postRepository.save(Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(user)
                .views(0)
                .createAt(new Date())
                .updateAt(new Date())
                .build());
        return ResponseEntity.ok(post);
    }

    @GetMapping
    public ResponseEntity<?> getAllPost(@PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return ResponseEntity.ok(posts.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPost(@PathVariable("id") long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(post);
    }

    @PatchMapping("/{id}")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<Post> updatePost(@PathVariable("id") long id, @CurrentUser User user, @Valid @RequestBody NewPostRequest request) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();
        if (!post.getWriter().equals(user)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setUpdateAt(new Date());
        return ResponseEntity.ok(postRepository.save(post));
    }

    @DeleteMapping("/{id}")
    @SuppressWarnings("Duplicates")
    public ResponseEntity<?> deletePost(@CurrentUser User user, @PathVariable("id") long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();
        if (!post.getWriter().equals(user)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        postRepository.delete(post);
        return ResponseEntity.ok().build();
    }

}
