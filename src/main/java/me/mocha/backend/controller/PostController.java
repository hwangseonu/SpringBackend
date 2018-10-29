package me.mocha.backend.controller;

import me.mocha.backend.annotation.CurrentUser;
import me.mocha.backend.exception.account.AccessDeniedException;
import me.mocha.backend.exception.post.PostNotFoundException;
import me.mocha.backend.model.entity.Post;
import me.mocha.backend.model.entity.User;
import me.mocha.backend.model.repository.PostRepository;
import me.mocha.backend.payload.post.CreateRequest;
import me.mocha.backend.payload.post.PostResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;

    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@CurrentUser User user, @RequestBody CreateRequest request) {
        postRepository.save(Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(user)
                .build());
    }

    @GetMapping("/")
    public List<PostResponse> getPosts() {
        List<PostResponse> responses = new ArrayList<>();
        postRepository.findAll().forEach(post -> responses.add(new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getWriter().getNickname())));
        return responses;
    }

    @GetMapping("/{pid}")
    public PostResponse get(@PathVariable("pid") long pid) throws PostNotFoundException {
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        return new PostResponse(found.getId(), found.getTitle(), found.getContent(), found.getWriter().getNickname());
    }

    @DeleteMapping("/{pid}")
    public void delete(@CurrentUser User user, @PathVariable("pid") long pid) throws PostNotFoundException, AccessDeniedException {
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        if (!found.getWriter().equals(user) && !user.getRole().equals("ROLE_ADMIN")) throw new AccessDeniedException();
        postRepository.delete(found);
    }

}
