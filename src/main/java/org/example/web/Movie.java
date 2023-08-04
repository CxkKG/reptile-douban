package org.example.web;

import jakarta.persistence.*;

@Entity
@Table(name = "movies4")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String score;
    public Movie() {
    }


    public Movie(Long id, String name, String score) {
        this.id = id;
        this.name = name;
        this.score = score;
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

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }





}

