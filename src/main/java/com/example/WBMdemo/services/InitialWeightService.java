package com.example.WBMdemo.services;

import java.util.List;

import com.example.WBMdemo.dto.InitialWeightDTO;

public interface InitialWeightService {

	public List<InitialWeightDTO> fetchTransactionList(int order);

	public InitialWeightDTO saveTransaction();
	
	public InitialWeightDTO getTransactionById(long transactionId); // by rawWeight Id

	public InitialWeightDTO findAllByOrderByCreatedDateDesc();
	
}
