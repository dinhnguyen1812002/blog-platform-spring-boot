package com.Nguyen.blogplatform.domain.user.infrastructure.repository;



import com.Nguyen.blogplatform.domain.user.domain.model.SocialMediaLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialMediaLinkRepository extends JpaRepository<SocialMediaLink, String> {
}