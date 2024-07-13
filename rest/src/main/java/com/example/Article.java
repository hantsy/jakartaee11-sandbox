package com.example;

import java.time.LocalDateTime;
import java.util.List;

public record Article(
        Integer id,
        String title,
        Author author,
        String content,
        List<String> tags,
        LocalDateTime publishedAt
) {
    public Article withId(int id) {
        return new Article(id, title, author, content, tags, publishedAt);
    }

    public Article withTags(List<String> tags) {
        return new Article(id, title, author, content, tags, publishedAt);
    }

    public Article withAuthor(Author author) {
        return new Article(id, title, author, content, tags, publishedAt);
    }
}

