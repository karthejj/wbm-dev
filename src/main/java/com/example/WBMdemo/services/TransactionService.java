package com.example.WBMdemo.services;

import java.util.List;

import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.errors.TransactionNotFoundException;

public interface TransactionService {
	
	public List<TransactionDto> fetchTransactionList();

	public TransactionDto saveTemporaryTransaction(TransactionDto dto);
	
	public TransactionDto getTransactionById(long transactionId) throws TransactionNotFoundException;
}
