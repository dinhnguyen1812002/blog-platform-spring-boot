package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.Enum.ESocialMediaPlatform;
import com.Nguyen.blogplatform.model.SocialMediaLink;
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
