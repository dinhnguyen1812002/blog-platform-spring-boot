package com.Nguyen.blogplatform.context;



import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import com.Nguyen.blogplatform.exception.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public UserContextService(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    // public User getCurrentUser() {
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     String jwt = (String) auth.getCredentials();
    //     String userId = jwtUtils.getUserIdFromJwtToken(jwt);
    //     return userRepository.findById(userId)
    //             .orElseThrow(() -> new NotFoundException("User not found"));
    // }

    // public User getCurrentUserOrNull() {
    //     try {
    //         return getCurrentUser();
    //     } catch (Exception e) {
    //         return null;
    //     }
    // }
}
