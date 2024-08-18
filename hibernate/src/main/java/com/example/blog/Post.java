package com.example.blog;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blog_seq")
    private Long id;

    @Basic(optional = false)
    @Column(name = "title",
            nullable = false,
            length = 100,
            unique = true,
            comment = "Post title",
            check = @CheckConstraint(name = "title_not_null", constraint = "length(title)>10")
    )
    private String title;

    private String content;

    private ModerationStatus status;

    @Column(name = "created_at", secondPrecision = 3)
    private Instant createdAt;

    public Post() {
    }

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
        this.status = ModerationStatus.PENDING;
    }

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ModerationStatus getStatus() {
        return status;
    }

    public void setStatus(ModerationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(id, post.id) && Objects.equals(title, post.title) && Objects.equals(content, post.content) && status == post.status && Objects.equals(createdAt, post.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, content, status, createdAt);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
