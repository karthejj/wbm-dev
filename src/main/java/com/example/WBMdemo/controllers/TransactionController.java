package com.example.WBMdemo.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.entity.Vehicle;
import com.example.WBMdemo.errors.TransactionNotFoundException;
import com.example.WBMdemo.errors.VehicleNotFoundException;
import com.example.WBMdemo.services.TransactionService;

@RestController
public class TransactionController {

	private final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);
	
	@Autowired
	private TransactionService transactionService;
	
	@PostMapping("/transaction/saveTransactions")
	public @ResponseBody TransactionDto savetemporaryTransaction(@RequestBody TransactionDto dto) {
		LOGGER.debug("Inside savetemporaryTransaction method of TransactionController start ");
		TransactionDto dtoResponse = transactionService.saveTemporaryTransaction(dto);
		LOGGER.debug("Inside savetemporaryTransaction method of TransactionController end ");
		return dtoResponse;
	}
	
	@GetMapping("/transaction/transactionById/{Id}")
	public TransactionDto getTransactionById(@PathVariable("Id") long transactionId) throws TransactionNotFoundException {
		LOGGER.debug("Inside getTransactionById method of TransactionController ");
		return transactionService.getTransactionById(transactionId);
	}
	
	@GetMapping("/transaction/transactionList")
	public  List<TransactionDto> fetchTransactionList(){
		LOGGER.debug("Inside fetchVehicleList method of TransactionController ");
		return transactionService.fetchTransactionList();
	}
}
