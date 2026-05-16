package com.contentiq.contentiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContentiqApplication {

	public static void main(String[] args) {
		SpringApplication.run(ContentiqApplication.class, args);
	}

}
