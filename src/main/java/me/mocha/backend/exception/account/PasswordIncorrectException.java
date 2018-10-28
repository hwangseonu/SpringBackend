package me.mocha.backend.exception.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class PasswordIncorrectException extends UserException {

    public PasswordIncorrectException() {
        super("password is not correct!");
    }

}
