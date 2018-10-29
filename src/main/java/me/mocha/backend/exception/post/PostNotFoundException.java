package me.mocha.backend.exception.post;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PostNotFoundException extends PostException {

    public PostNotFoundException() {
        super("Could not find post");
    }

}
