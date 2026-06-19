package com.Nguyen.blogplatform.domain.post.infrastructure.repository;



import com.Nguyen.blogplatform.domain.post.domain.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByCategory(String name);

    Optional<Category> findBySlug(String slug);
}
