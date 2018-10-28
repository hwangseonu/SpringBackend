package me.mocha.backend.model.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
public class User {

    @Id
    private String username;

    private String password;

    private String nickname;

    private String email;

    private String role;

    @Builder
    public User(String username, String password, String nickname, String email, String role) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
    }

}
