package com.example;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(RecordIdClass.class)
public class RecordIdClassEntity {

    @Id
    String id;

    @Id
    String id2;

    String name;

    public RecordIdClassEntity() {
    }

    public RecordIdClassEntity(String id, String id2, String name) {
        this.id = id;
        this.id2 = id2;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
