package me.mocha.backend.post.model.entity;

import lombok.*;
import me.mocha.backend.common.model.entity.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne (fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinColumn(name = "writer")
    private User writer;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    private long views;

}
