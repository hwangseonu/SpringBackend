package me.mocha.backend.payload.post;

import lombok.Data;

@Data
public class AddCommentRequest {

    private String content;

    public AddCommentRequest(String content) {
        this.content = content;
    }

}
