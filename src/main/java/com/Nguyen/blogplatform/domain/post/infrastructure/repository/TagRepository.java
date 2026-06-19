package com.Nguyen.blogplatform.domain.post.infrastructure.repository;



import com.Nguyen.blogplatform.domain.post.domain.model.Tags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface TagRepository extends JpaRepository<Tags, UUID> {
    boolean existsByNameOrSlug(String name, String slug);
    List<Tags> findTop5ByOrderByCreatedAtDesc();
}
