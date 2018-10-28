package me.mocha.backend.exception.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends UserException {

    public UserAlreadyExistsException() {
        super("User already exists!");
    }

}
