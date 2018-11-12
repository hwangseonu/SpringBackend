package me.mocha.backend.model.entity;

import lombok.Builder;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "writer")
    private User writer;

    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @Builder
    public Comment(String content, User writer, LocalDateTime createAt, LocalDateTime updateAt) {
        this.content = content;
        this.writer = writer;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

}