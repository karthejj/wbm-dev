package com.example.WBMdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableScheduling
@SpringBootApplication
public class WbMdemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(WbMdemoApplication.class, args);
	}
	
	@RestController
	class Helloweight{
		@GetMapping("/")
	    String home() {
	      return "Hello Weight Bridge!";
	    }	
	}
	
	
//	@Bean
//	public PasswordEncoder passwordEncoder() {
//			return NoOpPasswordEncoder.getInstance();
//	} 
	
}
