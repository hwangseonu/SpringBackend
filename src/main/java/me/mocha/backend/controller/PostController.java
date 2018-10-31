package me.mocha.backend.controller;

import me.mocha.backend.annotation.CurrentUser;
import me.mocha.backend.exception.account.AccessDeniedException;
import me.mocha.backend.exception.post.CommentNotFoundException;
import me.mocha.backend.exception.post.PostNotFoundException;
import me.mocha.backend.model.entity.Comment;
import me.mocha.backend.model.entity.Post;
import me.mocha.backend.model.entity.User;
import me.mocha.backend.model.repository.CommentRepository;
import me.mocha.backend.model.repository.PostRepository;
import me.mocha.backend.payload.post.AddCommentRequest;
import me.mocha.backend.payload.post.CommentResponse;
import me.mocha.backend.payload.post.CreateRequest;
import me.mocha.backend.payload.post.PostResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostController(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@CurrentUser User user, @Valid @RequestBody CreateRequest request) {
        postRepository.save(Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(user)
                .build());
    }

    @GetMapping
    public List<PostResponse> getPosts() {
        List<PostResponse> responses = new ArrayList<>();
        postRepository.findAll().forEach(post -> responses.add(new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getWriter().getNickname(), null)));
        return responses;
    }

    @GetMapping("/{pid}")
    public PostResponse get(@PathVariable("pid") long pid) throws PostNotFoundException {
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        List<CommentResponse> comments = new ArrayList<>();
        found.getComments().forEach(c -> comments.add(new CommentResponse(c.getId(), c.getContent(), c.getWriter().getNickname())));
        return new PostResponse(found.getId(), found.getTitle(), found.getContent(), found.getWriter().getNickname(), comments);
    }

    @DeleteMapping("/{pid}")
    public void delete(@CurrentUser User user, @PathVariable("pid") long pid) throws PostNotFoundException, AccessDeniedException {
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        if (!found.getWriter().equals(user) && !user.getRole().equals("ROLE_ADMIN")) throw new AccessDeniedException();
        postRepository.delete(found);
    }

    @PostMapping("/{pid}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public void addComment(@CurrentUser User user, @PathVariable("pid")long pid, @Valid @RequestBody AddCommentRequest request) throws PostNotFoundException {
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        found.getComments().add(new Comment(request.getContent() , user, LocalDateTime.now()));
        postRepository.save(found);
    }

    @DeleteMapping("/{pid}/comments/{cid}")
    public void deleteComment(@CurrentUser User user, @PathVariable("pid") long pid, @PathVariable("cid") long cid) throws PostNotFoundException{
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        Comment comment = commentRepository.findById(cid).orElseThrow(CommentNotFoundException::new);

        if (found.getComments().contains(comment)) {
            if (!comment.getWriter().equals(user) && !user.getRole().equals("ROLE_ADMIN")) throw new AccessDeniedException();
            found.getComments().remove(comment);
            postRepository.save(found);
        } else {
            throw new CommentNotFoundException();
        }
    }

}
