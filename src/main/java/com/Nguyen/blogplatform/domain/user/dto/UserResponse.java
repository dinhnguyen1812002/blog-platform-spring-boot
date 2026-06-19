package com.Nguyen.blogplatform.domain.user.dto;



import com.Nguyen.blogplatform.shared.enums.ERole;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
public class UserResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String username;
    private String slug;
    private String email;
    private String avatar;
    private List<ERole> roles;

    public UserResponse(String id, String username, String email, String avatar,List<ERole> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.roles = roles;
    }

//    public UserResponse(String id, String username, String email, List<ERole> roles) {
//        this.id = id;
//        this.username = username;
//        this.email = email;
//        this.roles = roles;
//    }

    public UserResponse(String id, String username, String email, String slug ,String avatar ) {
        this.id = id;
        this.username = username;
        this.avatar = avatar;
        this.email = email;
        this.slug = slug;
    }

}