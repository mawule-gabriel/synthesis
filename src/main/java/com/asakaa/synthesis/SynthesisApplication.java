package com.asakaa.synthesis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SynthesisApplication {

	public static void main(String[] args) {
		SpringApplication.run(SynthesisApplication.class, args);
	}

}
