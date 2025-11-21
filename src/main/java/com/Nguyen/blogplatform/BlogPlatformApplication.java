package com.Nguyen.blogplatform;

import com.Nguyen.blogplatform.Enum.ERole;
import com.Nguyen.blogplatform.model.Role;
import com.Nguyen.blogplatform.repository.RoleRepository;

import java.util.List;
import java.util.TimeZone;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BlogPlatformApplication implements  CommandLineRunner  {


    @Autowired
	private RoleRepository roleRepo;


	public static void main(String[] args) {
		SpringApplication.run(BlogPlatformApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        if (roleRepo.count() == 0) {
            Role admin = new Role(ERole.ROLE_ADMIN);
            Role user = new Role(ERole.ROLE_USER);
            roleRepo.save(admin);
            roleRepo.save(user);

            System.out.println("Default roles created: ADMIN and USER");
        } else {
            System.out.println("Roles already exist, skipping initialization");
        }
    }
    @PostConstruct
    public void init() {
        System.out.println(">>> Spring timezone: " + TimeZone.getDefault().getID());
    }


}
