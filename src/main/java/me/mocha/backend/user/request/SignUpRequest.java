package me.mocha.backend.user.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignUpRequest {

    @NotNull
    @NotBlank
    @Size(min = 4)
    private String username;

    @NotNull
    @NotBlank
    @Size(min = 8)
    private String password;

    @NotNull
    @NotBlank
    private String nickname;

    @Email
    @NotNull
    @NotBlank
    private String email;

}
