package com.Nguyen.blogplatform.repository;

import java.util.Optional;

import com.Nguyen.blogplatform.model.ERole;
import com.Nguyen.blogplatform.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}