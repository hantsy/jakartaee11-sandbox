package com.example.repository;

import java.util.UUID;

import com.example.domain.Post;
import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Repository;
import jakarta.transaction.Transactional;

@Transactional
@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {

    @Delete
    void deleteAll();
}
