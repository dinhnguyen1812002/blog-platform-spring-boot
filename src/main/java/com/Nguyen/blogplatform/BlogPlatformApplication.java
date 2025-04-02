package com.Nguyen.blogplatform;

import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogPlatformApplication implements CommandLineRunner {
//implements CommandLineRunner
	@Autowired
	private RoleRepository roleRepo;


	public static void main(String[] args) {
		SpringApplication.run(BlogPlatformApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// Define roles
		Role admin = new Role(ERole.ADMIN);
		Role user = new Role(ERole.USER);
		Role author = new Role(ERole.AUTHOR);

		// Save roles only if they do not already exist
		saveRoleIfNotExists(admin);
		saveRoleIfNotExists(user);
		saveRoleIfNotExists(author);
	}

	private void saveRoleIfNotExists(Role role) {
		// Check if the role already exists
		if (!roleRepo.existsByName(role.getName())) {
			roleRepo.save(role);
			System.out.println("Role " + role.getName() + " saved.");
		} else {
			System.out.println("Role " + role.getName() + " already exists.");
		}
	}


}
