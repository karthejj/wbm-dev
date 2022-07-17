package com.example.WBMdemo.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.dto.ChildTransactionDto;
import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.entity.ChildTransaction;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.entity.StatusMaster;
import com.example.WBMdemo.entity.TransactionsHeader;
import com.example.WBMdemo.entity.TransferType;
import com.example.WBMdemo.errors.TransactionNotFoundException;
import com.example.WBMdemo.repository.ChildTransactionRepository;
import com.example.WBMdemo.repository.CustomerRepository;
import com.example.WBMdemo.repository.MaterialRepository;
import com.example.WBMdemo.repository.StatusMasterRepository;
import com.example.WBMdemo.repository.TransactionRepository;


@Service
public class TransactionserviceImpl implements TransactionService {

	@Autowired
	private TransactionRepository transactionRepository;
	@Autowired
	private ChildTransactionRepository childTransactionRepository;
	@Autowired
	private CustomerRepository customerRepository;
	@Autowired
	private MaterialRepository materialRepository; 
	@Autowired
	private StatusMasterRepository statusMasterRepository;
	
	@Override
	public TransactionDto saveTransaction(TransactionDto dto) {
		
		TransactionsHeader transactions = new TransactionsHeader();
		ChildTransaction childTransaction = null;
		StatusMaster status = new StatusMaster();
		
		if(Objects.nonNull(dto)) {
			transactions.setCustomerName(dto.getCustomerName());
			transactions.setCustomerId(dto.getCustomerId());
			transactions.setVehicleNumber(dto.getVehicleNumber());
			transactions.setCustomer(customerRepository.findByCustomerId(dto.getCustomerType()));
			transactions.setDriverCount(dto.getDriverCount());
			if(dto.getTransferType().name().equals("INC")) {
				transactions.setTransfer_type("INC");
			} else {
				transactions.setTransfer_type("OUT");
			}
			// if Transaction is completed, calculate weight & price else update Status
			if(Objects.nonNull(dto.getIsTransactionCompleted()) && dto.getIsTransactionCompleted()) {
				
				List<ChildTransactionDto> transactionDetials = null;
			
				if(dto.getChildTransactionDtoList().size()!=0){
					List<ChildTransaction> childTransactionList = new ArrayList<ChildTransaction>();
					transactionDetials = new ArrayList<ChildTransactionDto>();	
					for(ChildTransactionDto childTransactionDto : dto.getChildTransactionDtoList()) {
						childTransaction = new ChildTransaction();
						BigDecimal materialPriceWithoutVat = new BigDecimal(0);
						Material materialDB = materialRepository.findByMaterialId(childTransactionDto.getMaterialType());
						childTransaction.setBaleOrLoose(childTransactionDto.getBaleOrLoose()); //B or L
						BigDecimal absoluteWeight = childTransactionDto.getFirstWeight().subtract(
								childTransactionDto.getSecondWeight()).abs();
						childTransaction.setMaterial(materialDB);
						childTransaction.setMaterialFirstWeight(childTransactionDto.getFirstWeight());
						childTransaction.setMaterialSecondWeight(childTransactionDto.getSecondWeight());
						childTransaction.setMaterialAbsoluteWeight(absoluteWeight);
						if(dto.getTransferType().name().equals("INC")) {
							materialPriceWithoutVat = childTransactionDto.getBaleOrLoose().equals("B") ? 
									materialDB.getMaterialIncBalePrice().multiply(absoluteWeight)
								: materialDB.getMaterialIncLoosePrice().multiply(absoluteWeight);
						} else {
							materialPriceWithoutVat = childTransactionDto.getBaleOrLoose().equals("B") ? 
									materialDB.getMaterialOutBalePrice().multiply(absoluteWeight)
								: materialDB.getMaterialOutLoosePrice().multiply(absoluteWeight);
						}
						childTransaction.setMaterialPrice(materialPriceWithoutVat);
//						childTransaction.setMaterialPriceAfterVat(absoluteWeight.multiply(childTransactionDto.getVat()));
						childTransaction.setMaterialPriceAfterVat(materialPriceWithoutVat.add(materialPriceWithoutVat.multiply(childTransactionDto.getVat()).divide(new BigDecimal(100))));
						childTransaction.setVat(childTransactionDto.getVat());
						childTransaction.setVatCost(childTransaction.getMaterialPriceAfterVat().subtract(materialPriceWithoutVat));
						childTransactionRepository.save(childTransaction);
						childTransactionList.add(childTransaction);
						
						ChildTransactionDto childTransactionDto1 = new ChildTransactionDto();
//						childTransactionDto1.setBaleOrLoose(childTransaction.getBaleOrLoose());
//						childTransactionDto1.setFirstWeight(childTransaction.getMaterialFirstWeight());
//						childTransactionDto1.setSecondWeight(childTransaction.getMaterialSecondWeight());
						childTransactionDto1.setAbsoluteWeight(childTransaction.getMaterialAbsoluteWeight());
						childTransactionDto1.setMaterialType(childTransaction.getMaterial().getMaterialId());
						childTransactionDto1.setVat(childTransaction.getVat());
						childTransactionDto1.setVatCost(childTransaction.getVatCost());
						childTransactionDto1.setMaterialPricewithoutVat(childTransaction.getMaterialPrice());
						childTransactionDto1.setMaterialPricewithVat(childTransaction.getMaterialPriceAfterVat());
						transactionDetials.add(childTransactionDto1);
					}
					
					dto.setChildTransactionDtoList(transactionDetials);

					transactions.setTransactionDetials(childTransactionList);
					transactions.setTotalWeight(new BigDecimal(
							childTransactionList.stream().mapToDouble(o -> o.getMaterialAbsoluteWeight().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
					transactions.setMaterialPrice(new BigDecimal(
							childTransactionList.stream().mapToDouble(o -> o.getMaterialPrice().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
					transactions.setFinalAmountWithVat(new BigDecimal(
							childTransactionList.stream().mapToDouble(o -> o.getMaterialPriceAfterVat().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
					transactions.setVatCost(transactions.getFinalAmountWithVat()
							.subtract(transactions.getMaterialPrice()));
					
					dto.setTotalWeight(transactions.getTotalWeight());
					dto.setVatCost(transactions.getVatCost());
					dto.setFinalAmount(transactions.getFinalAmountWithVat());
					
				}
				transactions.setModifiedDate(LocalDateTime.now());
				transactions.setTransactionCompleted(true);
				//transaction completed
				status = statusMasterRepository.findByStatusId(1);
				transactions.setStatus(status);
				dto.setTransactionStatus(status.getStatusName());
				if(dto.getId()!=0) {
					transactions.setTransactionId(dto.getId());
				}
			} else {
				dto.setIsTransactionCancelled((dto.getCancelReason()!=null && dto.getCancelReason()!="")?
						true : false);
				if(dto.getIsTransactionCancelled()) {
					//transaction cancelled
					transactions.setStatus(statusMasterRepository.findByStatusId(2));
					transactions.setCancelReason(dto.getCancelReason());
					transactions.setModifiedDate(LocalDateTime.now());
					transactions.setTransactionCompleted(true);
				} else { //temporary transaction
					if(dto.getChildTransactionDtoList().size()!=0){
						List<ChildTransaction> childTransactionList = new ArrayList<ChildTransaction>();
						for(ChildTransactionDto childTransactionDto : dto.getChildTransactionDtoList()) {
							childTransaction = new ChildTransaction();
							Material materialDB = materialRepository.findByMaterialId(childTransactionDto.getMaterialType());
							childTransaction.setMaterial(materialDB);
							childTransaction.setBaleOrLoose(childTransactionDto.getBaleOrLoose()); //B or L
							childTransaction.setMaterialFirstWeight(childTransactionDto.getFirstWeight());
							childTransactionRepository.save(childTransaction);
							childTransactionList.add(childTransaction);
						}
						transactions.setTransactionDetials(childTransactionList);
					}
					transactions.setCreatedDate(LocalDateTime.now());
					transactions.setTransactionCompleted(false);
					status = statusMasterRepository.findByStatusId(3); 					//transaction temporary
					transactions.setStatus(status);
					dto.setTransactionStatus(status.getStatusName());
					if(dto.getId()!=0) {
						transactions.setTransactionId(dto.getId());
					}
				}
			}
		}
		TransactionsHeader transObj = transactionRepository.save(transactions);
		dto.setId(transObj.getTransactionId());
		return dto;
	}
	
	@Override
	public List<TransactionDto> fetchTransactionList(String sortParam, int order) {
		// TODO Auto-generated method stub
		List<TransactionsHeader> transactionList = null;
		if(sortParam!=null && order!=0) {
			if(order==1) {
				transactionList = 
						transactionRepository.findAll(Sort.by(Sort.Direction.ASC, sortParam));
			} else {
				transactionList = 
						transactionRepository.findAll(Sort.by(Sort.Direction.DESC, sortParam));
			}
		} else {
			transactionList = 
					transactionRepository.findAll(Sort.by(Sort.Direction.ASC));
		}
		
		return transactionByField(transactionList);
	}
	
	@Override
	public List<TransactionDto> fetchCurrentDayTransactionList() {
		// TODO Auto-generated method stub
		List<TransactionsHeader> transactionList  = 
						transactionRepository.findByCreatedDate(LocalDate.now());
				List<TransactionsHeader> sortedUsers = transactionList.stream()
						  .sorted(Comparator.comparing(TransactionsHeader::getTransactionId))
						  .collect(Collectors.toList());
		
		return transactionByField(sortedUsers);
	}

	@Override
	public List<TransactionDto> fetchTemporaryTransactionList(){
		List<TransactionsHeader> transactionList  = 
				transactionRepository.findByStatus(new StatusMaster(3, "TEMPORARY"), LocalDate.now());
		List<TransactionsHeader> sortedUsers = transactionList.stream()
				  .sorted(Comparator.comparing(TransactionsHeader::getTransactionId))
				  .collect(Collectors.toList());

		return transactionByField(sortedUsers);
	}
	
	private List<TransactionDto> transactionByField(List<TransactionsHeader> sortedUsers) {
		List<TransactionDto> transactionDtoList = new ArrayList<TransactionDto>();
		if(Objects.nonNull(sortedUsers.size()) && sortedUsers.size()>0) {
			for(TransactionsHeader transactionObj : sortedUsers) {
				TransactionDto transDto = new TransactionDto();
				transDto.setId(transactionObj.getTransactionId());
				transDto.setCustomerId(transactionObj.getCustomerId());
				transDto.setCustomerName(transactionObj.getCustomerName());
				transDto.setCustomerType(transactionObj.getCustomer() != null ? 
						transactionObj.getCustomer().getCustomerId() : 0);
				transDto.setVehicleNumber(transactionObj.getVehicleNumber());
				transDto.setDriverCount(transactionObj.getDriverCount());
				transDto.setTransferType(TransferType.valueOf(transactionObj.getTransfer_type()));
				
				List<ChildTransactionDto> transactionDetials = null;
				List<ChildTransaction> transactionDetialsfromDB = null;
				if(transactionObj.getTransactionDetials().size()!=0) {
					transactionDetialsfromDB = transactionObj.getTransactionDetials();
					transactionDetials = new ArrayList<ChildTransactionDto>();
					for(ChildTransaction childtrans : transactionDetialsfromDB) {
						ChildTransactionDto childTransactionDto = new ChildTransactionDto();
						childTransactionDto.setBaleOrLoose(childtrans.getBaleOrLoose());
						childTransactionDto.setFirstWeight(childtrans.getMaterialFirstWeight());
						childTransactionDto.setSecondWeight(childtrans.getMaterialSecondWeight());
						childTransactionDto.setAbsoluteWeight(childtrans.getMaterialAbsoluteWeight());
						childTransactionDto.setMaterialType(childtrans.getMaterial().getMaterialId());
						childTransactionDto.setVat(childtrans.getVat());
						childTransactionDto.setVatCost(childtrans.getVatCost());
						childTransactionDto.setMaterialPricewithoutVat(childtrans.getMaterialPrice());
						childTransactionDto.setMaterialPricewithVat(childtrans.getMaterialPriceAfterVat());
						transactionDetials.add(childTransactionDto);
					}
				}
				
				transDto.setTotalWeight(transactionObj.getTotalWeight());
				transDto.setVatCost(transactionObj.getVatCost());
				transDto.setFinalAmount(transactionObj.getFinalAmountWithVat());
				
				transDto.setChildTransactionDtoList(transactionDetials);
				transDto.setIsTransactionCompleted(transactionObj.getTransactionCompleted());
				transDto.setTransactionStatus(transactionObj.getStatus() !=null ?
						transactionObj.getStatus().getStatusName() : "");
				transDto.setCancelReason(transactionObj.getCancelReason());
				transactionDtoList.add(transDto);
			}
			return transactionDtoList;
		} else {
			return transactionDtoList;
		}
	}
	
	public TransactionDto getTransactionById(long transactionId) throws TransactionNotFoundException {
		TransactionDto transDto = new TransactionDto();
		TransactionsHeader transactionObj = transactionRepository.findById(transactionId).get(); 
		if(transactionObj!=null) {
			transDto.setId(transactionObj.getTransactionId());
			transDto.setCustomerId(transactionObj.getCustomerId());
			transDto.setCustomerName(transactionObj.getCustomerName());
			transDto.setCustomerType(transactionObj.getCustomer() != null ? 
					transactionObj.getCustomer().getCustomerId() : 0);
			transDto.setVehicleNumber(transactionObj.getVehicleNumber());
			transDto.setDriverCount(transactionObj.getDriverCount());
			
			List<ChildTransactionDto> transactionDetials = null;
			List<ChildTransaction> transactionDetialsfromDB = null;
			if(transactionObj.getTransactionDetials().size()!=0) {
				transactionDetialsfromDB = transactionObj.getTransactionDetials();
				transactionDetials = new ArrayList<ChildTransactionDto>();
				for(ChildTransaction childtrans : transactionDetialsfromDB) {
					ChildTransactionDto childTransactionDto = new ChildTransactionDto();
					childTransactionDto.setBaleOrLoose(childtrans.getBaleOrLoose());
					childTransactionDto.setFirstWeight(childtrans.getMaterialFirstWeight());
					childTransactionDto.setSecondWeight(childtrans.getMaterialSecondWeight());
					childTransactionDto.setAbsoluteWeight(childtrans.getMaterialAbsoluteWeight());
					childTransactionDto.setMaterialType(childtrans.getMaterial().getMaterialId());
					childTransactionDto.setVat(childtrans.getVat());
					childTransactionDto.setVatCost(childtrans.getVatCost());
					childTransactionDto.setMaterialPricewithoutVat(childtrans.getMaterialPrice());
					childTransactionDto.setMaterialPricewithVat(childtrans.getMaterialPriceAfterVat());
					transactionDetials.add(childTransactionDto);
				}
			}
			
			transDto.setTotalWeight(transactionObj.getTotalWeight());
			transDto.setVatCost(transactionObj.getVatCost());
			transDto.setFinalAmount(transactionObj.getFinalAmountWithVat());
			
			transDto.setChildTransactionDtoList(transactionDetials);
			transDto.setIsTransactionCompleted(transactionObj.getTransactionCompleted());
			transDto.setTransactionStatus(transactionObj.getStatus() !=null ?
					transactionObj.getStatus().getStatusName() : "");
			transDto.setCancelReason(transactionObj.getCancelReason());
			transDto.setTransferType(TransferType.valueOf(transactionObj.getTransfer_type()));

		} else {
			throw new TransactionNotFoundException("Transaction Not Found ");
		}
		return transDto;
	}

}
