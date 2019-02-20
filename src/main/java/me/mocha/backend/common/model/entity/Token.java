package me.mocha.backend.common.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.mocha.backend.common.security.jwt.JwtType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Token {

    @Id
    private UUID identity;

    @NotNull
    @ManyToOne(targetEntity = User.class)
    private User owner;

    @NotNull
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @NotNull
    private JwtType type;
}
