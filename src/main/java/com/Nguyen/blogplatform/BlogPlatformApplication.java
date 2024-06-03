package com.Nguyen.blogplatform;

import com.Nguyen.blogplatform.model.ERole;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlogPlatformApplication 	{
//		implements CommandLineRunner


	@Autowired
	RoleRepository roleRepo;

	public static void main(String[] args) {
		SpringApplication.run(BlogPlatformApplication.class, args);
	}

//	@Override
//	public void run(String... args) throws Exception {
//
//		Role admin = new Role(ERole.ADMIN);
//		Role user = new Role(ERole.USER);
//		Role author = new Role(ERole.AUTHOR);
//		roleRepo.save(admin);
//		roleRepo.save(user);
//		roleRepo.save(author);
//	}
}
