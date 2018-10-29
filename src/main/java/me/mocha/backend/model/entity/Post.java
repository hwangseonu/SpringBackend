package me.mocha.backend.model.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "writer")
    private User writer;

    @Builder
    public Post(String title, String content, User writer) {
        this.title = title;
        this.content = content;
        this.writer = writer;
    }

}
