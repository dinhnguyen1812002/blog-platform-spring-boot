package com.Nguyen.blogplatform.service;
import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.BanRequest;
import com.Nguyen.blogplatform.payload.response.UserAdminResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ManagementServices {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final RatingRepository ratingRepository;
    private final BookmarkRepository bookmarkRepository;


    /**
     * Retrieves a paginated list of all users with their details and activity counts.
     *
     * @param pageable Pagination information (page, size, sort).
     * @return Page of UserAdminResponse objects containing user details and activity counts.
     */
    public Page<UserAdminResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    @Transactional
    public UserAdminResponse assignRoles(String userId, Set<String> roleNames) {
        User user = findUserById(userId);
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            try {
                ERole eRole = ERole.valueOf(roleName.toUpperCase());
                Role role = roleRepository.findByName(eRole)
                        .orElseThrow(() -> new NotFoundException("Role not found: " + eRole));
                roles.add(role);
            } catch (IllegalArgumentException e) {
                throw new NotFoundException("Invalid role name: " + roleName);
            }
        }
        user.setRoles(roles);
        userRepository.save(user);
        return mapToUserResponse(user);
    }
    /**
     * Bans a user and sets the ban reason.
     *
     * @param userId    ID of the user to ban.
     * @param banRequest Request containing the ban reason.
     * @return UserAdminResponse with updated user details.
     */
    @Transactional
    public UserAdminResponse banUser(String userId, BanRequest banRequest) {
        User user = findUserById(userId);
        user.setBanned(true);
        user.setBanReason(banRequest.getReason());
        userRepository.save(user);
        return mapToUserResponse(user);
    }
    private User findUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }
    private UserAdminResponse mapToUserResponse(User user) {
        return UserAdminResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .postCount(postRepository.countByUser(user))
                .commentCount(commentRepository.countByUser(user))
                .ratingCount(ratingRepository.countByUser(user))
                .likeCount(user.getLike().size())
                .bookmarkCount(bookmarkRepository.countByUser(user))
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .banned(user.isBanned())
                .banReason(user.getBanReason())
                .build();
    }

}
