package com.Nguyen.blogplatform.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class JwtResponse {
    private String token;
    private String type = "Bearer ";
    private String id;
    private String username;
    private String email;
    private String avatar;
    private List<String> roles;
    private String message; // Optional message for auto login

    public JwtResponse(String accessToken, String id,
                       String username, String email,
                       String avatar ,List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatar = avatar;
        this.roles = roles;
    }

    public JwtResponse(String accessToken, String id, String username, String email, List<String> roles, String message) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
        this.message = message;
    }
}
