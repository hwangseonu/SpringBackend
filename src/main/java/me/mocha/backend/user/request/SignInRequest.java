package me.mocha.backend.user.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignInRequest {

    @NotBlank
    @NotNull
    @Size(min = 4)
    private String username;

    @NotBlank
    @NotNull
    @Size(min = 8)
    private String password;

}
