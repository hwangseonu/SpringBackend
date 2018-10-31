package me.mocha.backend.payload.post;

import lombok.Data;

@Data
public class CommentResponse {

    private long comment_id;

    private String content;

    private String writer;

    public CommentResponse(long comment_id, String content, String user) {
        this.comment_id = comment_id;
        this.content = content;
        this.writer = user;
    }
}
