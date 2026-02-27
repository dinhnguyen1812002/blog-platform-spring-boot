package com.Nguyen.blogplatform.model;


import com.Nguyen.blogplatform.util.SlugUtil;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "category")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @NotNull
    @Size(max = 100)
    private String category;

    @NotNull
    @Size(max = 100)
    private String slug;
    @Size(max = 7)
    private String backgroundColor;

    @Size(max = 500)
    private String description;

    @ManyToMany
    @JoinTable(
            name = "post_category",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    @JsonBackReference
    private Set<Post> posts = new HashSet<>();


    @PrePersist
    public void generateSlug() {
        if (this.category != null && !this.category.isEmpty()) {
            this.slug = SlugUtil.toSlug(this.category);
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category1 = (Category) o;
        return Objects.equals(id, category1.id) &&
                Objects.equals(category, category1.category) &&

                Objects.equals(backgroundColor, category1.backgroundColor) &&
                Objects.equals(description, category1.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, category, backgroundColor, description);
    }

    // Override toString
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", category='" + category + '\'' +
                ", backgroundColor='" + backgroundColor + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
