package me.mocha.backend.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignUpRequest {

    @NotNull
    @Size(min = 4)
    private String username;

    @NotNull
    @Size(min = 8)
    private String password;

    @NotNull
    @NotBlank
    private String nickname;

    @NotNull
    @Email
    private String email;

}
