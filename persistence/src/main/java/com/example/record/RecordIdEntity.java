package com.example.record;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class RecordIdEntity {
    @EmbeddedId
    RecordId id;

    public RecordIdEntity() {
    }

    public RecordIdEntity(RecordId id) {
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

}
