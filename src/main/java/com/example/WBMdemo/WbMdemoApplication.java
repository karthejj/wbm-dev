package com.example.WBMdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

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
//	public CorsFilter corsFilter() {
//	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//	CorsConfiguration config = new CorsConfiguration();
//	config.setAllowCredentials(true);
//	config.addAllowedOrigin("*");
//	config.addAllowedHeader("*");
//	config.addAllowedMethod("OPTIONS");
//	config.addAllowedMethod("GET");
//	config.addAllowedMethod("POST");
//	config.addAllowedMethod("PUT");
//	config.addAllowedMethod("DELETE");
//	source.registerCorsConfiguration("/**", config);
//	return new CorsFilter(source);
//	}
	
	
//	@Bean
//	public PasswordEncoder passwordEncoder() {
//			return NoOpPasswordEncoder.getInstance();
//	} 
	
}
