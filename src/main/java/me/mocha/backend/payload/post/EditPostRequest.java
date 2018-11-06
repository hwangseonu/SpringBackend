package me.mocha.backend.payload.post;

import lombok.Data;

@Data
public class EditPostRequest {

    private String title;
    private String content;

}
