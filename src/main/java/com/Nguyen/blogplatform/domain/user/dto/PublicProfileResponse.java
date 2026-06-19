package com.Nguyen.blogplatform.domain.user.dto;



import com.Nguyen.blogplatform.domain.post.dto.PostSummaryResponse;
import com.Nguyen.blogplatform.shared.enums.ESocialMediaPlatform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicProfileResponse {
    private String username;
    private String slug;
    private String avatar;
    private String bio;
    private String website;
    private String customInformation; // reuse customProfileMarkdown as public custom info if available
    private long postCount;
    private Map<ESocialMediaPlatform, String> socialMediaLinks;
    private List<PostSummaryResponse> featuredPosts;
}
