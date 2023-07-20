package org.example.web;

import jakarta.persistence.*;

@Entity
@Table(name = "movies1")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String rating;

    public Movie(Long id, String title, String rating) {
        this.id = id;
        this.title = title;
        this.rating = rating;
    }
    public Movie() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

}

