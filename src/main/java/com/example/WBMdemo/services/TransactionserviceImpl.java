package com.example.WBMdemo.services;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.entity.Transactions;
import com.example.WBMdemo.repository.CustomerRepository;
import com.example.WBMdemo.repository.MaterialRepository;
import com.example.WBMdemo.repository.TransactionRepository;


@Service
public class TransactionserviceImpl implements TransactionService {

	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private MaterialRepository materialRepository; 
	
	@Override
	public TransactionDto saveTemporaryTransaction(TransactionDto dto) {
		
		Transactions transactions = new Transactions();
		if(Objects.nonNull(dto)) {
			transactions.setCustomerName(dto.getCustomerName());
			transactions.setCustomerId(dto.getCustomerId());
			transactions.setVehicleNumber(dto.getVehicleNumber());
			transactions.setCustomer(customerRepository.findByCustomerId(dto.getCustomerType()));
			transactions.setMaterial(materialRepository.findByMaterialId(dto.getMaterialType()));
			transactions.setDriverCount(dto.getDriverCount());
			transactions.setFirstWeight(dto.getFirstWeight());
			transactions.setSecondWeight(dto.getSecondWeight());
			transactions.setTotalWeight(dto.getTotalWeight());
			transactions.setMaterialPrice(dto.getMaterialPrice());
			transactions.setVat(dto.getVat());
			transactions.setFinalAmount(dto.getFinalAmount());
//			if(dto.flag){
//			transactions.setfinalflag(true);
//			}
		}
		Transactions transObj = transactionRepository.save(transactions);
		dto.setId(transObj.getTransactionId());
		return dto;
	}

}
