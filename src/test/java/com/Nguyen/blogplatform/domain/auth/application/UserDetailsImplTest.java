package com.Nguyen.blogplatform.domain.auth.application;

import com.Nguyen.blogplatform.domain.user.domain.model.Role;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.shared.enums.ERole;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {
    @Test void bannedAccountIsLocked() {
        User user = new User();
        user.setBanned(true);
        assertFalse(UserDetailsImpl.build(user).isAccountNonLocked());
        user.setBanned(false);
        assertTrue(UserDetailsImpl.build(user).isAccountNonLocked());
    }
    @Test void canonicalRolesMapToSpringAuthorities() {
        User user = new User();
        user.setRoles(Set.of(new Role(ERole.ROLE_USER), new Role(ERole.ROLE_ADMIN)));
        var names = UserDetailsImpl.build(user).getAuthorities().stream()
                .map(a -> a.getAuthority()).collect(java.util.stream.Collectors.toSet());
        assertEquals(Set.of("ROLE_USER", "ROLE_ADMIN"), names);
    }
}
