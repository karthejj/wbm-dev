package com.example.WBMdemo.services;

import com.example.WBMdemo.dto.TransactionDto;

public interface TransactionService {

	public TransactionDto saveTemporaryTransaction(TransactionDto dto);
}
