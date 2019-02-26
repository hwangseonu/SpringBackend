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
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String content;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "writer")
    private User writer;

    private Date createAt;
    private Date updateAt;

}
