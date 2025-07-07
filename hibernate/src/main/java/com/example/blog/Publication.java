package com.example.blog;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.Year;
import java.util.SequencedCollection;
import java.util.UUID;

@Entity
@Table(name ="publications")
public class Publication {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private Year foundIn;

    private String baseUrl = "http://java.com/";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "subscriptions",
            joinColumns = {
                    @JoinColumn(name = "blog_id")
            }
    )
    private SequencedCollection<Subscription> subscriptions;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Year getFoundIn() {
        return foundIn;
    }

    public void setFoundIn(Year establishedYear) {
        this.foundIn = establishedYear;
    }

    public SequencedCollection<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(SequencedCollection<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void approve(Post post) {
        post.setPublicationUrl(generatePublicationUrl(post));
        post.setPublishedBy(this);
        post.setModerationStatus(ModerationStatus.APPROVED);
        post.setPublishedAt(Instant.now());
    }

    private String generatePublicationUrl(Post post) {
        return baseUrl + post.getSlug();
    }
}
