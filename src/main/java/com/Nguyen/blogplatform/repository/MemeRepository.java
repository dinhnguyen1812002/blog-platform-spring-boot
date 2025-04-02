package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.Meme;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemeRepository extends JpaRepository<Meme, String> {
    Optional<Meme> findBySlug(String slug);
    Page<Meme> findAll(Pageable pageable);
}
