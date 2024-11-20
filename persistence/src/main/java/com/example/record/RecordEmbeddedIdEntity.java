package com.example.record;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

@Entity
public class RecordEmbeddedIdEntity {
    @EmbeddedId
    MyId id;

    public RecordEmbeddedIdEntity() {
    }

    public RecordEmbeddedIdEntity(MyId id) {
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
        return "RecordEmbeddedIdEntity{" +
                "id=" + id +
                '}';
    }

    @Embeddable
    public static record MyId(String id) {
    }
}
