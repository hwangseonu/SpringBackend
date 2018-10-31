package me.mocha.backend.payload.post;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

}
