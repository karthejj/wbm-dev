package com.example.WBMdemo.dto;

import java.util.List;

import lombok.Data;

@Data
public class CommonTransactiondto {
	
	private List<TransactionDto> transactionDtoList;
	private int totalTransaction;
	

}
