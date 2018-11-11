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

    private LocalDateTime create_at;
    private LocalDateTime update_at;

    @Builder
    public Comment(String content, User writer, LocalDateTime create_at, LocalDateTime update_at) {
        this.content = content;
        this.writer = writer;
        this.create_at = create_at;
        this.update_at = update_at;
    }

}