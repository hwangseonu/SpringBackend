package me.mocha.backend.model.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne (fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST, CascadeType.REFRESH })
    @JoinColumn(name = "writer")
    private User writer;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Collection<Comment> comments;

    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @Builder
    public Post(String title, String content, User writer, Collection<Comment> comments, LocalDateTime createAt, LocalDateTime updateAt) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.comments = comments;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

}
