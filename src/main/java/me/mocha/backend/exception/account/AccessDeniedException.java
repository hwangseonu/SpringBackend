package me.mocha.backend.exception.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends UserException {

    public AccessDeniedException() {
        super("access denied");
    }

}
