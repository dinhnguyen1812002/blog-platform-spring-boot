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
public class BlogPlatformApplication  {

	@Autowired
	private RoleRepository roleRepo;


	public static void main(String[] args) {
		SpringApplication.run(BlogPlatformApplication.class, args);
	}



}
