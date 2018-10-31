package me.mocha.backend.payload.user;

import lombok.Data;

@Data
public class InfoResponse {

    private String username;

    private String email;

    private String nickname;

    public InfoResponse(String username, String email, String nickname) {
        this.username = username;
        this.email = email;
        this.nickname = nickname;
    }

}
