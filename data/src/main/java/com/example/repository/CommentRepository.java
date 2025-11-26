package com.example.repository;

import com.example.domain.Comment;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Repository;
import jakarta.transaction.Transactional;

import java.util.UUID;

@Transactional
@Repository
public interface CommentRepository extends CrudRepository<Comment, UUID> {

    @Delete
    void deleteAll();
}