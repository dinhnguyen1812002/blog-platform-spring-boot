package com.Nguyen.blogplatform.controller.Authentication;

import com.Nguyen.blogplatform.payload.response.RoleResponse;
import com.Nguyen.blogplatform.service.RoleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Role Management", description = "APIs for managing roles and user role assignments")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

//    @GetMapping
//    @Operation(summary = "Get all roles", description = "Get all roles with user count")
//    public ResponseEntity<List<RoleResponse>> getAllRoles() {
//        List<RoleResponse> roles = roleManagementService.get();
//        return ResponseEntity.ok(roles);
//    }

//    @GetMapping("/statistics")
//    @Operation(summary = "Get role statistics", description = "Get role statistics with user counts")
//    public ResponseEntity<List<RoleResponse>> getRoleStatistics() {
//        List<RoleResponse> statistics = roleManagementService.getRoleStatistics();
//        return ResponseEntity.ok(statistics);
//    }

//    @GetMapping("/{roleId}")
//    @Operation(summary = "Get role with users", description = "Get specific role with list of users")
//    public ResponseEntity<RoleResponse> getRoleWithUsers(
//            @Parameter(description = "Role ID") @PathVariable Long roleId) {
//        RoleResponse role = roleManagementService.getRoleWithUsers(roleId);
//        return ResponseEntity.ok(role);
//    }

//    @GetMapping("/name/{roleName}")
//    @Operation(summary = "Get role by name with users", description = "Get role by name with list of users")
//    public ResponseEntity<RoleResponse> getRoleByNameWithUsers(
//            @Parameter(description = "Role name") @PathVariable ERole roleName) {
//        RoleResponse role = roleManagementService.getRoleByNameWithUsers(roleName);
//        return ResponseEntity.ok(role);
//    }
//
//    @GetMapping("/{roleId}/users")
//    @Operation(summary = "Get users by role", description = "Get paginated list of users with specific role")
//    public ResponseEntity<Page<UserResponse>> getUsersByRole(
//            @Parameter(description = "Role ID") @PathVariable Long roleId,
//            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
//            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<UserResponse> users = roleManagementService.getUsersByRole(roleId, pageable);
//        return ResponseEntity.ok(users);
//    }
//
//    @PostMapping("/assign")
//    @Operation(summary = "Assign roles to user", description = "Assign multiple roles to a user")
//    public ResponseEntity<MessageResponse> assignRolesToUser(
//            @Valid @RequestBody AssignRoleRequest request) {
//        MessageResponse response = roleManagementService.assignRolesToUser(request);
//        return ResponseEntity.ok(response);
//    }
//
//    @DeleteMapping("/users/{userId}/roles/{roleId}")
//    @Operation(summary = "Remove role from user", description = "Remove specific role from user")
//    public ResponseEntity<MessageResponse> removeRoleFromUser(
//            @Parameter(description = "User ID") @PathVariable String userId,
//            @Parameter(description = "Role ID") @PathVariable Long roleId) {
//        MessageResponse response = roleManagementService.removeRoleFromUser(userId, roleId);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/users/{userId}")
//    @Operation(summary = "Get user roles", description = "Get all roles assigned to specific user")
//    public ResponseEntity<List<RoleResponse>> getUserRoles(
//            @Parameter(description = "User ID") @PathVariable String userId) {
//        List<RoleResponse> roles = roleManagementService.getUserRoles(userId);
//        return ResponseEntity.ok(roles);
//    }
}
