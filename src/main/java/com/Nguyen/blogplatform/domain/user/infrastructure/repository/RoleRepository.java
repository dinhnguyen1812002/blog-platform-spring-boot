package com.Nguyen.blogplatform.domain.user.infrastructure.repository;



import com.Nguyen.blogplatform.domain.user.domain.model.Role;
import com.Nguyen.blogplatform.shared.enums.ERole;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
    boolean existsByName(ERole name);
}

