package com.Nguyen.blogplatform.repository;

import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
    Optional<User> findByEmail(String email);
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
    User findByResetToken(String resetToken);

    // Role-related queries
    Page<User> findByRolesContaining(Role role, Pageable pageable);
    List<User> findByRolesContaining(Role role);
    Long countByRolesContaining(Role role);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") com.Nguyen.blogplatform.Enum.ERole roleName);

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRoleName(@Param("roleName") com.Nguyen.blogplatform.Enum.ERole roleName, Pageable pageable);
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") String id);
}