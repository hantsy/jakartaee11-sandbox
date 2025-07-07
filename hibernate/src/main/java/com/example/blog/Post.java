package com.example.blog;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "posts")
@NamedQuery(name = "byTitle", query = "SELECT p FROM Post p where p.title = :title")
@NamedEntityGraph(
        name = "withComments",
        attributeNodes = {
                @NamedAttributeNode(value = "title"),
                @NamedAttributeNode(value = "content"),
                @NamedAttributeNode(value = "comments", subgraph = "commentsGraph"),
                @NamedAttributeNode(value = "createdAt")
        },
        subgraphs = @NamedSubgraph(
                name = "commentsGraph",
                attributeNodes = @NamedAttributeNode("content")
        )
)
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

    @Embedded
    private Slug slug;

    private String content;

    private ModerationStatus status;

    @Column(name = "created_at", secondPrecision = 3)
    private Instant createdAt;

    @OneToMany(mappedBy = Comment_.POST,
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Comment> comments = new HashSet<>();

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
        slug = Slug.deriveFromTitle(this.title);
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

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Slug getSlug() {
        return slug;
    }

    // add comment
    public void addComment(Comment comment) {
        comment.setPost(this);
        comments.add(comment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(slug, post.slug) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(slug);
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
