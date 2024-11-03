package com.example;

import java.util.UUID;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface CommentRepository extends CrudRepository<Comment, UUID> {
    @Delete
    @Transactional
    void deleteAll();
}