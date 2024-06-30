package com.example.WBMdemo.services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.WBMdemo.dto.CommonTransactiondto;
import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.errors.TransactionNotFoundException;

public interface TransactionService {
	
	public List<TransactionDto> fetchTransactionList(String sortParam, int order);
	
	public List<TransactionDto> fetchCurrentDayTransactionList();

	public List<TransactionDto> fetchTemporaryTransactionList();
	
	public TransactionDto saveTransaction(TransactionDto dto);
	
	public TransactionDto getTransactionById(long transactionId) throws TransactionNotFoundException;
	
	public CommonTransactiondto fetchTransactionListPage(String sortParam, String sortBy,  int page, int size); 
}
