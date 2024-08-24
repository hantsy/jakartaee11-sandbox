package com.example.record;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;

@Entity
@IdClass(MyIdClassEntity.MyIdClass.class)
public class MyIdClassEntity {

    @Id
    String id1;
    @Id
    String id2;

    public MyIdClassEntity() {
    }

    public MyIdClassEntity(MyIdClass classId) {
        this.id1 = classId.id1();
        this.id2 = classId.id2();
    }

    public String getId1() {
        return id1;
    }

    public void setId1(String id1) {
        this.id1 = id1;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    @Override
    public String toString() {
        return "MyClassIdEntity{" +
                "id1='" + id1 + '\'' +
                ", id2='" + id2 + '\'' +
                '}';
    }

    public static record MyIdClass(
            String id1,
            String id2
    ) {
    }
}
