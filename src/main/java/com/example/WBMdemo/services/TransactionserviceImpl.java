package com.example.WBMdemo.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.entity.Customer;
import com.example.WBMdemo.entity.StatusMaster;
import com.example.WBMdemo.entity.Transactions;
import com.example.WBMdemo.errors.TransactionNotFoundException;
import com.example.WBMdemo.errors.VehicleNotFoundException;
import com.example.WBMdemo.repository.CustomerRepository;
import com.example.WBMdemo.repository.MaterialRepository;
import com.example.WBMdemo.repository.StatusMasterRepository;
import com.example.WBMdemo.repository.TransactionRepository;


@Service
public class TransactionserviceImpl implements TransactionService {

	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private MaterialRepository materialRepository; 
	@Autowired
	private StatusMasterRepository statusMasterRepository;
	
	@Override
	public TransactionDto saveTemporaryTransaction(TransactionDto dto) {
		
		Transactions transactions = new Transactions();
		StatusMaster status = new StatusMaster();
		
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
			if(Objects.nonNull(dto.getIsTransactionCompleted()) && 
					dto.getIsTransactionCompleted()){
				transactions.setTransactionCompleted(true);
				transactions.setStatus(statusMasterRepository.findByStatusId(3));
			} else {
				if(Objects.nonNull(dto.getIsTransactionCancelled()) && 
						dto.getIsTransactionCancelled()) {
					transactions.setStatus(statusMasterRepository.findByStatusId(2));
				} else {
					transactions.setStatus(statusMasterRepository.findByStatusId(1));
				}
				transactions.setTransactionCompleted(false);
			}
		}
		Transactions transObj = transactionRepository.save(transactions);
		dto.setId(transObj.getTransactionId());
		return dto;
	}
	
	@Override
	public List<TransactionDto> fetchTransactionList() {
		// TODO Auto-generated method stub
		List<Transactions> transactionList =  transactionRepository.findAll();
		List<TransactionDto> transactionDtoList = new ArrayList<TransactionDto>();
		if(Objects.nonNull(transactionList.size()) && transactionList.size()>0) {
			for(Transactions transactionObj : transactionList) {
				TransactionDto transDto = new TransactionDto();
				transDto.setId(transactionObj.getTransactionId());
				transDto.setCustomerId(transactionObj.getCustomerId());
				transDto.setCustomerName(transactionObj.getCustomerName());
				transDto.setCustomerType(transactionObj.getCustomer() != null ? 
						transactionObj.getCustomer().getCustomerId() : 0);
				transDto.setVehicleNumber(transactionObj.getVehicleNumber());
				transDto.setMaterialType(transactionObj.getMaterial() != null ? 
						transactionObj.getMaterial().getMaterialId() : 0);
				transDto.setDriverCount(transactionObj.getDriverCount());
				transDto.setFirstWeight(transactionObj.getFirstWeight());
				transDto.setSecondWeight(transactionObj.getSecondWeight());
				transDto.setTotalWeight(transactionObj.getTotalWeight());
				transDto.setMaterialPrice(transactionObj.getMaterialPrice());
				transDto.setVat(transactionObj.getVat());
				transDto.setFinalAmount(transactionObj.getFinalAmount());
				transDto.setIsTransactionCompleted(transactionObj.getTransactionCompleted());
				transDto.setTransactionStatus(transactionObj.getStatus() !=null ?
						transactionObj.getStatus().getStatusId() : 0);
				
				transactionDtoList.add(transDto);
			}
			return transactionDtoList;
		} else {
			return transactionDtoList;
		}
	}
	
	public TransactionDto getTransactionById(long transactionId) throws TransactionNotFoundException {
		TransactionDto transDto = new TransactionDto();
		Transactions transactionObj = transactionRepository.findById(transactionId).get();
		if(Objects.nonNull(transactionObj)) {		
			transDto.setId(transactionObj.getTransactionId());
			transDto.setCustomerId(transactionObj.getCustomerId());
			transDto.setCustomerName(transactionObj.getCustomerName());
			transDto.setCustomerType(transactionObj.getCustomer().getCustomerId());
			transDto.setVehicleNumber(transactionObj.getVehicleNumber());
			transDto.setMaterialType(transactionObj.getMaterial().getMaterialId());
			transDto.setDriverCount(transactionObj.getDriverCount());
			transDto.setFirstWeight(transactionObj.getFirstWeight());
			transDto.setSecondWeight(transactionObj.getSecondWeight());
			transDto.setTotalWeight(transactionObj.getTotalWeight());
			transDto.setMaterialPrice(transactionObj.getMaterialPrice());
			transDto.setVat(transactionObj.getVat());
			transDto.setFinalAmount(transactionObj.getFinalAmount());
			transDto.setIsTransactionCompleted(transactionObj.getTransactionCompleted());
			transDto.setTransactionStatus(transactionObj.getStatus().getStatusId());
		} else {
			throw new TransactionNotFoundException("Transaction Not Found ");
		}
		return transDto;
	}

}
