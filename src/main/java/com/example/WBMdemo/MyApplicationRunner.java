package com.example.WBMdemo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.WBMdemo.repository.InitialWeightRepository;

@Component
public class MyApplicationRunner implements ApplicationRunner {

	@Autowired
	private InitialWeightRepository rawWeightRepository;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// TODO Auto-generated method stub
	}
	
}
