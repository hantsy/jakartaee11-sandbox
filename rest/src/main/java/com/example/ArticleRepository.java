package com.example;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class ArticleRepository {
    private final static ConcurrentHashMap<Integer, Article> articles = new ConcurrentHashMap<>();
    private final static AtomicInteger ID_GEN = new AtomicInteger(1);

    static {
        var id1 = ID_GEN.getAndIncrement();
        articles.put(
                id1,
                new Article(id1, "My first article",
                        new Author("Hantsy", "Bai"),
                        "This is my first article",
                        List.of("first", "article"),
                        LocalDateTime.now())
        );
        var id2 = ID_GEN.getAndIncrement();
        articles.put(id2,
                new Article(id2, "My second article",
                        new Author("Hantsy", "Bai"),
                        "This is my second article",
                        List.of("second", "article"),
                        LocalDateTime.now())
        );
    }

    public List<Article> findAll() {
        return List.copyOf(articles.values());
    }

    public Article findById(int id) {
        return articles.get(id);
    }

    public Article save(Article article) {
        if (article.id() == null) {
            var id = ID_GEN.getAndIncrement();
            article = article.withId(id);
        }
        articles.put(article.id(), article);
        return article;
    }
}
