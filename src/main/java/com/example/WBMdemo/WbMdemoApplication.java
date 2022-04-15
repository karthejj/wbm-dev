package com.example.WBMdemo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.security.crypto.password.NoOpPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.WBMdemo.entity.User;

@SpringBootApplication
public class WbMdemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(WbMdemoApplication.class, args);
	}
	
	@GetMapping("/")
    public String home() {
      return "Hello Weight Bridge!";
    }
	
//	@Bean
//	public PasswordEncoder passwordEncoder() {
//			return NoOpPasswordEncoder.getInstance();
//	} 
	
}
