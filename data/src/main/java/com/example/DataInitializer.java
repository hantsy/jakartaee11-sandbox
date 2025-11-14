package com.example;

import com.example.domain.Comment;
import com.example.domain.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@Transactional
@ApplicationScoped
//@Singleton
//@Startup
public class DataInitializer {

    @Inject
    PostRepository postRepository;

    @Inject
    CommentRepository commentRepository;

    // `@Observes Startup event` or  `@Observes @Initialized(ApplicationScoped.class) Object any`
    // raised exception `jakarta.data.exceptions.DataException: No active transaction for update or delete query`
     public void init(@Observes Startup event) {
    // public void init(@Observes @Initialized(ApplicationScoped.class) Object any) {
    // when adding @Transactional on class, still failed the tests, the observer method does not work.
//    @PostConstruct
//    public void init() {
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
