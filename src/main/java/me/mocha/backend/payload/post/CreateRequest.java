package me.mocha.backend.payload.post;

import lombok.Data;

@Data
public class CreateRequest {

    private String title;

    private String content;

}
