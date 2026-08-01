package com.example.record;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class RecordValueEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private RecordValue value;

    public RecordValueEntity() {
    }

    public RecordValueEntity(RecordValue myEmbedded) {
        this.value = myEmbedded;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RecordValue getValue() {
        return value;
    }

    public void setValue(RecordValue value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordValueEntity that = (RecordValueEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value);
    }

    @Override
    public String toString() {
        return "RecordEmbeddedEntity{" +
                "id=" + id +
                ", myEmbedded=" + value +
                '}';
    }
}
