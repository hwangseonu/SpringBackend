package me.mocha.backend.common.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    private String username;

    @JsonIgnore
    private String password;

    private String nickname;

    private String email;

    @JsonIgnore
    @Fetch(FetchMode.SUBSELECT)
    @ElementCollection(targetClass = String.class)
    private List<String> roles;

    public boolean equals(Object object) {
        try {
            User user = (User) object;
            if (!this.username.equals(user.username)) return false;
            if (!this.nickname.equals(user.nickname)) return false;
            if (!this.email.equals(user.email)) return false;
            if (!this.roles.containsAll(user.roles)) return false;
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }
}
