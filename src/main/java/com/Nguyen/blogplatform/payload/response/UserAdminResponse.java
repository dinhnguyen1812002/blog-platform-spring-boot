package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.Enum.ERole;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class UserAdminResponse {
    private String id;
    private String username;
    private String email;
    private String avatar;
    private long postCount;
    private long commentCount;
    private long ratingCount;
    private long likeCount;
    private long bookmarkCount;
    private Set<ERole> roles;
    private boolean banned;
    private String banReason;
}
