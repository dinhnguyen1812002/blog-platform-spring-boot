package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.AssignRoleRequest;
import com.Nguyen.blogplatform.payload.response.MessageResponse;
import com.Nguyen.blogplatform.payload.response.RoleResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.RoleRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleManagementService {
    
//    private final RoleRepository roleRepository;
//    private final UserRepository userRepository;
//
//    /**
//     * Lấy tất cả roles với số lượng users
//     */
//    public List<RoleResponse> getAllRoles() {
//        return roleRepository.findAll().stream()
//                .map(this::toRoleResponseWithCount)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Lấy role cụ thể với danh sách users
//     */
//    public RoleResponse getRoleWithUsers(Long roleId) {
//        Role role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
//
//        return toRoleResponseWithUsers(role);
//    }
//
//    /**
//     * Lấy role theo tên với danh sách users
//     */
//    public RoleResponse getRoleByNameWithUsers(ERole roleName) {
//        Role role = roleRepository.findByName(roleName)
//                .orElseThrow(() -> new NotFoundException("Role not found: " + roleName));
//
//        return toRoleResponseWithUsers(role);
//    }
//
//    /**
//     * Lấy users theo role với phân trang
//     */
//    public Page<UserResponse> getUsersByRole(Long roleId, Pageable pageable) {
//        Role role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
//
//        return userRepository.findByRolesContaining(role, pageable)
//                .map(this::toUserResponse);
//    }
//
//    /**
//     * Assign roles cho user
//     */
//    @Transactional
//    public MessageResponse assignRolesToUser(AssignRoleRequest request) {
//        User user = userRepository.findById(request.getUserId())
//                .orElseThrow(() -> new NotFoundException("User not found with id: " + request.getUserId()));
//
//        Set<Role> roles = new HashSet<>();
//        for (Long roleId : request.getRoleIds()) {
//            Role role = roleRepository.findById(roleId)
//                    .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
//            roles.add(role);
//        }
//
//        user.setRoles(roles);
//        userRepository.save(user);
//
//        log.info("Assigned roles {} to user {}",
//                roles.stream().map(r -> r.getName()).collect(Collectors.toList()),
//                user.getUsername());
//
//        return new MessageResponse("Roles assigned successfully to user: " + user.getUsername());
//    }
//
//    /**
//     * Remove role từ user
//     */
//    @Transactional
//    public MessageResponse removeRoleFromUser(String userId, Long roleId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
//
//        Role role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new NotFoundException("Role not found with id: " + roleId));
//
//        user.getRoles().remove(role);
//        userRepository.save(user);
//
//        log.info("Removed role {} from user {}", role.getName(), user.getUsername());
//
//        return new MessageResponse("Role removed successfully from user: " + user.getUsername());
//    }
//
//    /**
//     * Lấy roles của user cụ thể
//     */
//    public List<RoleResponse> getUserRoles(String userId) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
//
//        return user.getRoles().stream()
//                .map(this::toRoleResponseWithCount)
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Thống kê roles
//     */
//    public List<RoleResponse> getRoleStatistics() {
//        return roleRepository.findAll().stream()
//                .map(role -> {
//                    Long userCount = userRepository.countByRolesContaining(role);
//                    return RoleResponse.builder()
//                            .id(role.getId())
//                            .name(role.getName())
//                            .displayName(role.getName())
//                            .userCount(userCount)
//                            .build();
//                })
//                .collect(Collectors.toList());
//    }
//
//    private RoleResponse toRoleResponseWithCount(Role role) {
//        Long userCount = userRepository.countByRolesContaining(role);
//        return RoleResponse.builder()
//                .id(role.getId())
//                .name(role.getName())
//                .displayName(role.getName())
//                .userCount(userCount)
//                .build();
//    }
//
//    private RoleResponse toRoleResponseWithUsers(Role role) {
//        List<UserResponse> users = role.getUsers().stream()
//                .map(this::toUserResponse)
//                .collect(Collectors.toList());
//
//        return RoleResponse.builder()
//                .id(role.getId())
//                .name(role.getName())
//                .displayName(role.getName())
//                .users(users)
//                .userCount((long) users.size())
//                .build();
//    }
//
//    private UserResponse toUserResponse(User user) {
//        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
//    }
}
