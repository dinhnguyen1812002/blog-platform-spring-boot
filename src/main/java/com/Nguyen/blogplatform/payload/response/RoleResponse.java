package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.Enum.ERole;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private Integer id;
    private String name;
    private String displayName;
    private List<UserResponse> users;
    private Long userCount;
    
    public RoleResponse(Integer id, String name, Long userCount) {
        this.id = id;
        this.name = name;
        this.displayName = name;
        this.userCount = userCount;
    }
}
