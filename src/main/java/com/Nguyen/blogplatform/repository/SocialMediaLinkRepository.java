package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.SocialMediaLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialMediaLinkRepository extends JpaRepository<SocialMediaLink, String> {
}