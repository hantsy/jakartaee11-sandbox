package com.example.record;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class MyEmbeddedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private MyEmbedded myEmbedded;

    public MyEmbeddedEntity() {
    }

    public MyEmbeddedEntity(MyEmbedded myEmbedded) {
        this.myEmbedded = myEmbedded;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MyEmbedded getMyEmbedded() {
        return myEmbedded;
    }

    public void setMyEmbedded(MyEmbedded myEmbedded) {
        this.myEmbedded = myEmbedded;
    }

    public static record MyEmbedded(String name, int age) { }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyEmbeddedEntity that = (MyEmbeddedEntity) o;
        return Objects.equals(id, that.id) && Objects.equals(myEmbedded, that.myEmbedded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, myEmbedded);
    }

    @Override
    public String toString() {
        return "MyEmbeddedEntity{" +
                "id=" + id +
                ", myEmbedded=" + myEmbedded +
                '}';
    }
}
