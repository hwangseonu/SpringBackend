package me.mocha.backend.exception.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class TokenRefreshException extends UserException {

    public TokenRefreshException() {
        super("Could not token refreshing");
    }

}
