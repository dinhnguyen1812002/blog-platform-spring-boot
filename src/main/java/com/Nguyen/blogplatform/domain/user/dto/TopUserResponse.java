package com.Nguyen.blogplatform.domain.user.dto;



import com.Nguyen.blogplatform.domain.user.domain.model.SocialMediaLink;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

public record TopUserResponse(
        String id,
        String username,
        String email,
        String avatar,
        String bio,
        Long postCount,
        Set<SocialMediaLink> socialMediaLinks
){}