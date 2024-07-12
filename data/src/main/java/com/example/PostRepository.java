package com.example;

import java.util.UUID;

import jakarta.data.repository.CrudRepository;
import jakarta.data.repository.Delete;
import jakarta.data.repository.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface PostRepository extends CrudRepository<Post, UUID> {

    @Delete
    @Transactional
    void deleteAll();
}
