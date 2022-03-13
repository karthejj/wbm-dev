package com.example.WBMdemo.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.services.TransactionService;

@RestController
public class TransactionController {

	private final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);
	
	@Autowired
	private TransactionService transactionService;
	
	@PostMapping("/transaction/saveTemporary")
	public @ResponseBody TransactionDto savetemporaryTransaction(@RequestBody TransactionDto dto) {
		LOGGER.debug("Inside savetemporaryTransaction method of TransactionController start ");
		TransactionDto dtoResponse = transactionService.saveTemporaryTransaction(dto);
		LOGGER.debug("Inside savetemporaryTransaction method of TransactionController end ");
		return dtoResponse;
	}
}
