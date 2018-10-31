package me.mocha.backend.payload.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SignInRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

}
