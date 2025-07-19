package com.Nguyen.blogplatform;

import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.repository.RoleRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogPlatformApplication implements CommandLineRunner {

	@Autowired
	private RoleRepository roleRepo;


	public static void main(String[] args) {
		SpringApplication.run(BlogPlatformApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		List<ERole> roles = List.of(ERole.ROLE_ADMIN, ERole.ROLE_USER, ERole.ROLE_AUTHOR);
		for (ERole roleName : roles) {
			saveRoleIfNotExists(roleName);
		}
	}

	private void saveRoleIfNotExists(ERole roleName) {
		if (!roleRepo.findByName(roleName).isPresent()) {
			Role role = new Role();
			role.setName(roleName);
			roleRepo.save(role);
			System.out.println("Saved role: " + roleName);
		} else {
			System.out.println("Role already exists: " + roleName);
		}
	}


}
