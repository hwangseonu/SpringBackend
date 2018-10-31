package me.mocha.backend.payload.post;

import lombok.Data;

import java.util.List;

@Data
public class PostResponse {

    private long post_id;

    private String title;

    private String content;

    private String writer;

    private List<CommentResponse> comments;

    public PostResponse(long postId, String title, String content, String writer, List<CommentResponse> comments) {
        this.post_id = postId;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.comments = comments;
    }

}
