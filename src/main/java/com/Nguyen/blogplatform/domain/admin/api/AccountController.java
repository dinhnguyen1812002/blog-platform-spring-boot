package com.Nguyen.blogplatform.domain.admin.api;



import com.Nguyen.blogplatform.domain.admin.application.ManagementServices;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.dto.BanRequest;
import com.Nguyen.blogplatform.domain.user.dto.UserAdminResponse;
import com.Nguyen.blogplatform.domain.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AccountController {

    private final ManagementServices managementServices;


    /**
     * Retrieves a paginated list of all users with their details and activity counts.
     *
     * @param pageable Pagination information (page, size, sort).
     * @return Page of UserResponse objects.
     */
    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserAdminResponse>> getAllUsers(Pageable pageable) {
        Page<UserAdminResponse> users = managementServices.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }


    /**
     * Assigns roles to a user.
     *
     * @param id    User ID.
     * @param roles Set of role names to assign (e.g., ["USER", "ADMIN"]).
     * @return Updated UserResponse.
     */
    @PutMapping("/{id}/roles")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminResponse> assignRoles(@PathVariable String id, @RequestBody Set<String> roles) {
        UserAdminResponse updatedUser = managementServices.assignRoles(id, roles);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Bans a user with a specified reason.
     *
     * @param id        User ID.
     * @param banRequest Request containing the ban reason.
     * @return Updated UserResponse.
     */
    @PutMapping("/{id}/ban")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserAdminResponse> banUser(@PathVariable String id, @RequestBody BanRequest banRequest) {
        UserAdminResponse bannedUser = managementServices.banUser(id, banRequest);
        return ResponseEntity.ok(bannedUser);
    }



}
