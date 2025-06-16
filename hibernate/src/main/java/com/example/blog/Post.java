package com.example.blog;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

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

    @Embedded
    @AttributeOverride(name = "slug", column = @Column(name = "slug", unique = true, nullable = false))
    private Slug slug;

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

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    @Embedded
    @AttributeOverride(name = "name", column = @Column(name = "author"))
    private Author author;

    @Column(name = "created_at", secondPrecision = 3) // secondPrecision to truncate the seconds
    private Instant createdAt;

    @Column(name = "updated_at", secondPrecision = 3) // secondPrecision to truncate the seconds
    private Instant updatedAt;

    @OneToMany(mappedBy = Comment_.POST,
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Comment> comments = new HashSet<>();

    private ModerationStatus moderationStatus = ModerationStatus.NA;

    private String publicationUrl;

    @ManyToOne
    @JoinColumn(name = "published_by")
    private Publication publishedBy;

    private Instant publishedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tags",
            joinColumns = {
                    @JoinColumn(name = "post_id")
            }
    )
    private Collection<String> tags = new HashSet<>();

    public Post() {
    }

    public Post(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @PrePersist
    public void prePersist() {
        slug = Slug.deriveFromTitle(title);
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

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ModerationStatus status) {
        this.moderationStatus = status;
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

    public void setSlug(Slug slug) {
        this.slug = slug;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Publication getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(Publication publishedBy) {
        this.publishedBy = publishedBy;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(Collection<String> tags) {
        this.tags = tags;
    }

    public String getPublicationUrl() {
        return publicationUrl;
    }

    public void setPublicationUrl(String publicationUrl) {
        this.publicationUrl = publicationUrl;
    }

    // add comment
    public void addComment(Comment comment) {
        comment.setPost(this);
        comments.add(comment);
    }

    public void markAsPublic(List<String> taglist) {
        status = Status.PUBLIC;
        updatedAt = Instant.now();
        tags = taglist;
    }

    public void addToPublication(Publication publication) {
        if(status != Status.PUBLIC) {
            throw new IllegalArgumentException("Post is not available for public");
        }
        moderationStatus = ModerationStatus.PENDING;
        publishedBy = publication;
    }

    public void approveBy(Publication publication) {
        publication.approve(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Post post)) return false;
        return Objects.equals(slug, post.slug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slug);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", slug=" + slug +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", author=" + author +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", moderationStatus=" + moderationStatus +
                ", publicationUrl='" + publicationUrl + '\'' +
                ", publishedBy=" + publishedBy +
                ", publishedAt=" + publishedAt +
                ", tags=" + tags +
                '}';
    }
}
