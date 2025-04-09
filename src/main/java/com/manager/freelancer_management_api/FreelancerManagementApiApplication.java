package com.manager.freelancer_management_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FreelancerManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FreelancerManagementApiApplication.class, args);
	}

}
