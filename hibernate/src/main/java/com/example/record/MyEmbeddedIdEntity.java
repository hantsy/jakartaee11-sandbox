package com.example.record;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class MyEmbeddedIdEntity {
    @EmbeddedId
    MyId id;

    public MyEmbeddedIdEntity() {
    }

    public MyEmbeddedIdEntity(MyId id) {
        this.id = id;
    }

    public MyId getId() {
        return id;
    }

    public void setId(MyId id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "MyEmbeddedIdEntity{" +
                "id=" + id +
                '}';
    }

    public static record MyId(String id) {
    }
}
