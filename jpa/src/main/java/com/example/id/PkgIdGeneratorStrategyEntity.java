package com.example.id;

import jakarta.persistence.*;

@Entity
public class PkgIdGeneratorStrategyEntity {
    @Id
    @GeneratedValue(generator = "id_gen", strategy = GenerationType.TABLE)
    private Long id;

    private String name;

    public PkgIdGeneratorStrategyEntity() {
    }

    public PkgIdGeneratorStrategyEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PkgIdEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
