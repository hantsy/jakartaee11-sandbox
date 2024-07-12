package com.example;

import java.util.UUID;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Repository;

@Repository
public interface CommentRepository extends CrudRepository<Comment, UUID> {
    
}