package com.example.record;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class RecordEmbeddedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private RecordEmbedded myEmbedded;

    public RecordEmbeddedEntity() {
    }

    public RecordEmbeddedEntity(RecordEmbedded myEmbedded) {
        this.myEmbedded = myEmbedded;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecordEmbedded getMyEmbedded() {
        return myEmbedded;
    }

    public void setMyEmbedded(RecordEmbedded myEmbedded) {
        this.myEmbedded = myEmbedded;
    }

    public static record RecordEmbedded(String name, int age) { }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordEmbeddedEntity that = (RecordEmbeddedEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(myEmbedded, that.myEmbedded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, myEmbedded);
    }

    @Override
    public String toString() {
        return "RecordEmbeddedEntity{" +
                "id=" + id +
                ", myEmbedded=" + myEmbedded +
                '}';
    }
}
