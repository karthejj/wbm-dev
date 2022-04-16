package com.example.WBMdemo.entity.controllers;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.WBMdemo.entity.PriceMaster;
import com.example.WBMdemo.services.PriceService;

@RestController
public class PriceController {

	private final Logger LOGGER = LoggerFactory.getLogger(PriceController.class);
	
	@Autowired
	private PriceService priceService;

	@PostMapping("/price/saveprice")
	public PriceMaster saveMaterial(@Valid @RequestBody PriceMaster price) {
		LOGGER.debug("Inside savePrice method of PriceController ");
		return priceService.savePrice(price);
	}
}
