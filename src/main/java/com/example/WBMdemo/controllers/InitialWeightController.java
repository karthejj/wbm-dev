package com.example.WBMdemo.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.WBMdemo.dto.InitialWeightDTO;
import com.example.WBMdemo.services.InitialWeightService;

@RestController
public class InitialWeightController {

private final Logger LOGGER = LoggerFactory.getLogger(InitialWeightController.class);
	
	@Autowired
	private InitialWeightService transactionService;
	
	@GetMapping("/getWeight/latestDeviceWeight")
	public InitialWeightDTO fetchLatestDeviceWeight(){
		LOGGER.debug("Inside fetchLatestDeviceWeight method of InitialWeightController ");
		return transactionService.findAllByOrderByCreatedDateDesc();
	}
	
//	@GetMapping("/rawWeight/saveRawWeight")
//	public RawWeightDTO saveRawWeight(){
//		LOGGER.debug("Inside saveRawWeight method of RawWeightController ");
//		RawWeightDTO dto = transactionService.saveTransaction();
//		System.out.println(dto.getId() +"  "+dto.getWeight());
//		return dto;
//	}
	
	
}
