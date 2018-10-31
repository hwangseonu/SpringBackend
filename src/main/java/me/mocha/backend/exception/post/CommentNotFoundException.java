package me.mocha.backend.exception.post;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CommentNotFoundException extends PostException {

    public CommentNotFoundException() {
        super("Could not find comment");
    }
}
