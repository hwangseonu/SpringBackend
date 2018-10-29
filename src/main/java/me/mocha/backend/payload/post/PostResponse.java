package me.mocha.backend.payload.post;

import lombok.Data;

@Data
public class PostResponse {

    private long post_id;

    private String title;

    private String content;

    private String writer;

    public PostResponse(long postId, String title, String content, String writer) {
        this.post_id = postId;
        this.title = title;
        this.content = content;
        this.writer = writer;
    }

}
