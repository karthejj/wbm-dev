package com.example.WBMdemo.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.entity.StatusMaster;
import com.example.WBMdemo.entity.Transactions;
import com.example.WBMdemo.entity.TransferType;
import com.example.WBMdemo.errors.TransactionNotFoundException;
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
	public TransactionDto saveTransaction(TransactionDto dto) {
		
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
			BigDecimal materialPrice = new BigDecimal(0);
//			transactions.setTotalWeight(dto.getTotalWeight());
//			BigDecimal materialPrice = dto.getMaterialPrice().multiply(dto.getTotalWeight());
//			transactions.setFinalAmount(dto.getFinalAmount());
			Material materialDB = materialRepository.findByMaterialId(dto.getMaterialType());
			transactions.setBaleOrLoose(dto.getBaleOrLoose()); // B - Bale , L - Loose
			
			// if Transaction is completed, calculate weight & price else update Status
			if(Objects.nonNull(dto.getIsTransactionCompleted()) && dto.getIsTransactionCompleted()) {
					transactions.setTotalWeight(dto.getFirstWeight().subtract(dto.getSecondWeight()).abs());
					if(dto.getTransferType().name().equals("INC")) {
						transactions.setTransfer_type("INC");
						materialPrice = dto.getBaleOrLoose().equals("B") ? 
								materialDB.getMaterialIncBalePrice().multiply(dto.getFirstWeight().subtract(dto.getSecondWeight()))
							: materialDB.getMaterialIncLoosePrice().multiply(dto.getFirstWeight().subtract(dto.getSecondWeight()));
						
					} else {
						transactions.setTransfer_type("OUT");
						materialPrice = dto.getBaleOrLoose().equals("B") ? 
								materialDB.getMaterialOutBalePrice().multiply(dto.getFirstWeight().subtract(dto.getSecondWeight()))
							: materialDB.getMaterialOutLoosePrice().multiply(dto.getFirstWeight().subtract(dto.getSecondWeight()));
			
					}
				transactions.setMaterialPrice(materialPrice);
				transactions.setVat(dto.getVat());
				BigDecimal finalAmount = materialPrice.add(materialPrice.multiply(dto.getVat()).multiply(new BigDecimal(0.01)));
				transactions.setFinalAmount(finalAmount);
				transactions.setTransactionCompleted(true);
				//transaction completed
				transactions.setStatus(statusMasterRepository.findByStatusId(3));
				if(dto.getId()!=0) {
					transactions.setTransactionId(dto.getId());
				}
			} else {
				if(Objects.nonNull(dto.getIsTransactionCancelled()) && 
						dto.getIsTransactionCancelled()) {
					//transaction cancelled
					transactions.setStatus(statusMasterRepository.findByStatusId(2));
					transactions.setCancelReason(dto.getCancelReason());
				} else {
					//transaction temporary; allowed for the second transaction
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
	public List<TransactionDto> fetchTransactionList(String sortParam, int order) {
		// TODO Auto-generated method stub
		List<Transactions> transactionList = null;
		if(order==1) {
			transactionList = 
					transactionRepository.findAll(Sort.by(Sort.Direction.ASC, sortParam));
		} else {
			transactionList = 
					transactionRepository.findAll(Sort.by(Sort.Direction.DESC, sortParam));
		}
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
				transDto.setCancelReason(transactionObj.getCancelReason());
				transDto.setTransferType(TransferType.valueOf(transactionObj.getTransfer_type()));
				transDto.setBaleOrLoose(transactionObj.getBaleOrLoose());
				
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
		if(transactionObj!=null) {
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
			transDto.setCancelReason(transactionObj.getCancelReason());
			transDto.setTransferType(TransferType.valueOf(transactionObj.getTransfer_type()));
			transDto.setBaleOrLoose(transactionObj.getBaleOrLoose());
		} else {
			throw new TransactionNotFoundException("Transaction Not Found ");
		}
		return transDto;
	}

}
