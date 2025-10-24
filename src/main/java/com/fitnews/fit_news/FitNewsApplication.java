package com.fitnews.fit_news;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FitNewsApplication {

	public static void main(String[] args) {
		SpringApplication.run(FitNewsApplication.class, args);
	}

}
