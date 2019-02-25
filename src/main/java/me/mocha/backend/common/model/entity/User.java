package me.mocha.backend.common.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements UserDetails {

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

    @JsonIgnore
    transient private Collection<? extends GrantedAuthority> authorities;

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

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
