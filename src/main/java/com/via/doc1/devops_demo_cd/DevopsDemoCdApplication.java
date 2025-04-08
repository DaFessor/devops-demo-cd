package com.via.doc1.devops_demo_cd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DevopsDemoCdApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevopsDemoCdApplication.class, args);
	}

}
