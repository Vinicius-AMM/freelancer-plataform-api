package com.manager.freelancer_management_api;

import org.springframework.boot.SpringApplication;

public class TestFreelancerManagementApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(FreelancerManagementApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
