package com.example.service;

import com.example.domain.Comment;
import com.example.domain.Post;
import com.example.repository.CommentRepository;
import com.example.repository.PostRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

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

    public Comment addCommentForPost(UUID postId, Comment comment) {
        var post = this.postRepository.findById(postId).orElseThrow();
        comment.setPost(post);
        return this.commentRepository.save(comment);
    }
}
