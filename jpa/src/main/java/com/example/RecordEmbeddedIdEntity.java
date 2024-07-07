package com.example;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class RecordEmbeddedIdEntity {

    @EmbeddedId
    RecordEmbeddedId id;
    String name;

    public RecordEmbeddedIdEntity() {
    }

    public RecordEmbeddedIdEntity(RecordEmbeddedId id, String name) {
        this.id = id;
        this.name = name;
    }

    public RecordEmbeddedId getId() {
        return id;
    }

    public void setId(RecordEmbeddedId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
