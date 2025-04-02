package com.Nguyen.blogplatform.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "meme")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Meme {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String description;

    private String memeUrl;

    @Column(unique = true)
    private String slug;


    @PrePersist
    @PreUpdate
    private void generateSlug() {
        if(name != null && (slug == null || slug.isEmpty())) {
            this.slug = name.toLowerCase()
                    .replaceAll("[^a-zA-Z0-9]", "-")
                    .replaceAll("-+", "-")
                    .trim();
        }
    }

}
