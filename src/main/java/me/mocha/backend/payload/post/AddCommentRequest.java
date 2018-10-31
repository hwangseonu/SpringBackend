package me.mocha.backend.payload.post;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AddCommentRequest {

    @NotBlank
    private String content;

}
