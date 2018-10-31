package me.mocha.backend.payload.auth;

import lombok.Data;

@Data
public class SignInResponse {

    private String accessToken;

    private String refreshToken;

    public SignInResponse(String accessToken) {
        this(accessToken, null);
    }

    public SignInResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}
