package com.example.record;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class RecordEmbeddedIdEntity {
    @EmbeddedId
    RecordId id;

    public RecordEmbeddedIdEntity() {
    }

    public RecordEmbeddedIdEntity(RecordId id) {
        this.id = id;
    }

    public RecordId getId() {
        return id;
    }

    public void setId(RecordId id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "RecordEmbeddedIdEntity{" +
                "id=" + id +
                '}';
    }

    public static record RecordId(String id) {
    }
}
