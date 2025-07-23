package com.tinusj.stocklee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StockleeApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockleeApplication.class, args);
	}

}
