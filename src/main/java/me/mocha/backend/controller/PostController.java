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
import me.mocha.backend.payload.post.*;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
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
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .build());
    }

    @GetMapping
    public List<PostResponse> getPosts() {
        List<PostResponse> responses = new ArrayList<>();
        postRepository.findAll().forEach(post -> responses.add(
                new PostResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getWriter() == null ? null : post.getWriter().getNickname(),
                        null
                )
        ));
        return responses;
    }

    @GetMapping("/{pid}")
    public PostResponse get(@PathVariable("pid") long pid) throws PostNotFoundException {
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        List<CommentResponse> comments = new ArrayList<>();
        found.getComments().forEach(c -> comments.add(new CommentResponse(c.getId(), c.getContent(), c.getWriter() == null ? null : c.getWriter().getNickname())));
        return new PostResponse(found.getId(), found.getTitle(), found.getContent(), found.getWriter() == null ? null : found.getWriter().getNickname(), comments);
    }

    @DeleteMapping("/{pid}")
    public void delete(@CurrentUser User user, @PathVariable("pid") long pid) throws PostNotFoundException, AccessDeniedException {
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        checkPermission(user, found.getWriter());
        postRepository.delete(found);
    }

    @PatchMapping("/{pid}")
    public void edit(@CurrentUser User user, @PathVariable("pid") long pid, @RequestBody EditPostRequest request) throws PostNotFoundException, AccessDeniedException {
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        checkPermission(user, found.getWriter());

        if (StringUtils.hasText(request.getTitle())) {
            found.setTitle(request.getTitle());
        }
        if (StringUtils.hasText(request.getContent())) {
            found.setContent(request.getContent());
        }
        found.setUpdateAt(LocalDateTime.now());
        postRepository.save(found);
    }

    @PostMapping("/{pid}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public void addComment(@CurrentUser User user, @PathVariable("pid")long pid, @Valid @RequestBody AddCommentRequest request) throws PostNotFoundException {
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        found.getComments().add(new Comment(request.getContent() , user, LocalDateTime.now(), LocalDateTime.now()));
        postRepository.save(found);
    }

    @DeleteMapping("/{pid}/comments/{cid}")
    public void deleteComment(@CurrentUser User user, @PathVariable("pid") long pid, @PathVariable("cid") long cid) throws PostNotFoundException{
        Post found = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        Comment comment = commentRepository.findById(cid).orElseThrow(CommentNotFoundException::new);

        if (found.getComments().contains(comment)) {
            if (comment.getWriter() == null && !user.getRole().equals("ROLE_ADMIN")) throw new AccessDeniedException();
            else if (!comment.getWriter().equals(user) && !user.getRole().equals("ROLE_ADMIN")) throw new AccessDeniedException();
            found.getComments().remove(comment);
            postRepository.save(found);
        } else {
            throw new CommentNotFoundException();
        }
    }

    @PatchMapping("/{pid}/comments/{cid}")
    public void editComment(@CurrentUser User user, @PathVariable("pid") long pid, @PathVariable("cid") long cid, @RequestBody EditCommentRequest request)
            throws PostNotFoundException, CommentNotFoundException, AccessDeniedException {
        Post post = postRepository.findById(pid).orElseThrow(PostNotFoundException::new);
        Comment comment = commentRepository.findById(cid).orElseThrow(CommentNotFoundException::new);
        checkPermission(user, comment.getWriter());
        if (!post.getComments().contains(comment)) throw new CommentNotFoundException();
        comment.setContent(request.getContent());
        comment.setUpdate_at(LocalDateTime.now());
        commentRepository.save(comment);
    }

    private void checkPermission(@CurrentUser User user, User writer) throws AccessDeniedException {
        if (!user.getRole().equals("ROLE_ADMIN")) {
            if (writer == null) throw new AccessDeniedException();
            else if (!writer.equals(user)) throw new AccessDeniedException();
        }
    }

}
