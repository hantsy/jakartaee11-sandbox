package com.example;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

//@ApplicationScoped
@Singleton
@Startup
public class DataInitializer {

    @Inject
    PostRepository postRepository;

    @Inject
    CommentRepository commentRepository;

    // `@Observes Startup event` raised exception `No active contexts for scope type jakarta.enterprise.context.RequestScoped`
    // public void init(@Observes @Initialized(ApplicationScoped.class) Object any) {

    @PostConstruct
    public void init() {
        commentRepository.deleteAll();
        postRepository.deleteAll();

        // insert two sample posts
        Post post1 = Post.builder().title("Post 1").content("Content 1").build();
        postRepository.insert(post1);
        Post post2 = Post.builder().title("Post 2").content("Content 2").build();
        postRepository.insert(post2);

        // insert two sample comments
        Comment comment1 = Comment.builder().content("Comment 1").post(post1).build();
        commentRepository.insert(comment1);
        Comment comment2 = Comment.builder().content("Comment 2").post(post1).build();
        commentRepository.insert(comment2);
    }
}
