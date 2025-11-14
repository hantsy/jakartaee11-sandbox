package com.example.service;

import com.example.domain.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Transactional
public class BlogService {
    private PostRepository postRepository;
    private CommentRepository commentRepository;

    public BlogService() {
    }

    @Inject
    public BlogService(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public Post addPost(Post post) {
        return this.postRepository.save(post);
    }
}
