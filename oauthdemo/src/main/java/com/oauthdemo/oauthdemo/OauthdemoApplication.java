package com.oauthdemo.oauthdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("com.*")
@SpringBootApplication(scanBasePackages = "com.*")
public class OauthdemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(OauthdemoApplication.class, args);
	}
}
