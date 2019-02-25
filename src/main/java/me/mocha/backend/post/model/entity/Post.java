package me.mocha.backend.post.model.entity;

import lombok.*;
import me.mocha.backend.common.model.entity.User;

import javax.persistence.*;
import java.util.Date;

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

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "writer")
    private User writer;

    private Date createAt;

    private Date updateAt;

    private long views;

}
