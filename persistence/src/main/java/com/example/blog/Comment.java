package com.example.blog;

import jakarta.persistence.*;

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "tbl_id_gen")
    private Long id;

    private String content;

    @ManyToOne
    private Post post;

    public Comment() {
    }

    public Comment(String content) {
        this.content = content;
    }

    public Comment(Post post, String content) {
        this.post = post;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                '}';
    }
}
