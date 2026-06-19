package com.Nguyen.blogplatform.domain.media.infrastructure.repository;



import com.Nguyen.blogplatform.domain.media.domain.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {
}
