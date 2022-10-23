package com.example.WBMdemo.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
		
		TransactionsHeader transactions;
		TransactionsHeader transObj=null;
		ChildTransaction childTransaction = null;
		StatusMaster status = new StatusMaster();
		
		if(Objects.nonNull(dto)) {
			if(dto.getId()!=0) {
				transactions = transactionRepository.findByTransactionId(dto.getId());
			}else {
				transactions = new TransactionsHeader();
			}
			transactions.setCustomerName(dto.getCustomerName());
			transactions.setCustomerId(dto.getCustomerId());
			transactions.setVehicleNumber(dto.getVehicleNumber());
			transactions.setCustomer(dto.getCustomerType()!=0 ? 
					customerRepository.findByCustomerId(dto.getCustomerType()): null);
			transactions.setDriverCount(dto.getDriverCount());
			transactions.setPhoneNumber(dto.getPhoneNumber());
			if(dto.getTransferType().name().equals("INC")) {
				transactions.setTransfer_type("INC");
			} else if(dto.getTransferType().name().equals("OUT")) {
				transactions.setTransfer_type("OUT");
			} else {
				transactions.setTransfer_type("WEIGH");
			}
			transObj = transactionRepository.saveAndFlush(transactions);
			List<ChildTransactionDto> childtransactionDetialsDTO =  dto.getChildTransactionDtoList(); // from screen
//			List<ChildTransaction> childTransactionList = transObj.getTransactionDetials(); // from DB
			// if Transaction is completed, calculate weight & price else update Status
			if(Objects.nonNull(dto.getIsTransactionCompleted()) && dto.getIsTransactionCompleted()
					&& !(dto.getCancelReason()!=null && dto.getCancelReason()!="")) {
				List<ChildTransaction> childTransactionList = childTransactionRepository.findByTransactionsHeader(transObj);
				System.out.println(childTransactionList.size());
				if(childtransactionDetialsDTO.size()!=0){
					for(ChildTransaction childTransaction1 : childTransactionList) {
						BigDecimal materialPriceWithoutVat = new BigDecimal(0);
						Material materialDB = materialRepository.findByMaterialId(childTransaction1.getMaterial().getMaterialId());
						BigDecimal secondWeight;
						if(Objects.nonNull(childTransaction1.getMaterialSecondWeight())) {
							secondWeight = childTransaction1.getMaterialSecondWeight();
						} else {
							secondWeight = childtransactionDetialsDTO.get(childtransactionDetialsDTO.size()-1).getSecondWeight();
						}
						BigDecimal absoluteWeight = childTransaction1.getMaterialFirstWeight().subtract(secondWeight).abs();
						childTransaction1.setMaterial(materialDB);
						childTransaction1.setMaterialFirstWeight(childTransaction1.getMaterialFirstWeight());
						childTransaction1.setMaterialSecondWeight(secondWeight);
						childTransaction1.setMaterialAbsoluteWeight(absoluteWeight);
						if(dto.getTransferType().name().equals("INC")) {
							materialPriceWithoutVat = childTransaction1.getBaleOrLoose().equals("B") ? 
									materialDB.getMaterialIncBalePrice().multiply(absoluteWeight)
								: materialDB.getMaterialIncLoosePrice().multiply(absoluteWeight);
						} else if(dto.getTransferType().name().equals("OUT")) {
							materialPriceWithoutVat = childTransaction1.getBaleOrLoose().equals("B") ? 
									materialDB.getMaterialOutBalePrice().multiply(absoluteWeight)
								: materialDB.getMaterialOutLoosePrice().multiply(absoluteWeight);
						} else {
							materialPriceWithoutVat = childTransaction1.getBaleOrLoose().equals("B") ? 
									materialDB.getMaterialOutBalePrice().multiply(absoluteWeight)
								: materialDB.getMaterialOutLoosePrice().multiply(absoluteWeight);
						}
						childTransaction1.setMaterialPrice(materialPriceWithoutVat);
						if(dto.getIncludeVat()) {
							childTransaction1.setMaterialPriceAfterVat(materialPriceWithoutVat.add(materialPriceWithoutVat.multiply(materialDB.getVat()).divide(new BigDecimal(100))));
							childTransaction1.setVat(materialDB.getVat());
						} else {
							childTransaction1.setMaterialPriceAfterVat(materialPriceWithoutVat);
							childTransaction1.setVat(new BigDecimal(0));
						}
						childTransaction1.setVatCost(childTransaction1.getMaterialPriceAfterVat().subtract(materialPriceWithoutVat));
						childTransaction1.setTransactionsHeader(transObj);
						childTransactionRepository.save(childTransaction1);
//						childTransactionList.add(childTransaction1);
					}
					transObj.setTransactionDetials(childTransactionList);
					transObj.setTotalWeight(new BigDecimal(
							childTransactionList.stream().mapToDouble(o -> o.getMaterialAbsoluteWeight().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
					transObj.setMaterialPrice(new BigDecimal(
							childTransactionList.stream().mapToDouble(o -> o.getMaterialPrice().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));

					transObj.setFinalAmountWithVat(new BigDecimal(
							childTransactionList.stream().mapToDouble(o -> o.getMaterialPriceAfterVat().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
					transObj.setVatIncluded(dto.getIncludeVat());
					transObj.setVatCost(transObj.getFinalAmountWithVat()
							.subtract(transObj.getMaterialPrice()));
					dto.setTotalWeight(transObj.getTotalWeight());
					dto.setVatCost(transObj.getVatCost());
					dto.setFinalAmount(transObj.getFinalAmountWithVat());
					
				}
				List<ChildTransactionDto> childtransactionDetialsDTO2 = new ArrayList<ChildTransactionDto>();
				if(childtransactionDetialsDTO.size()!=0){
					for(ChildTransaction childTransaction1 : childTransactionList) {
						ChildTransactionDto childTransactionDto1 = new ChildTransactionDto();
						childTransactionDto1.setId(childTransaction1.getChildTransactionId());
						childTransactionDto1.setBaleOrLoose(childTransaction1.getBaleOrLoose());
						childTransactionDto1.setFirstWeight(childTransaction1.getMaterialFirstWeight());
						childTransactionDto1.setSecondWeight(childTransaction1.getMaterialSecondWeight());
						childTransactionDto1.setAbsoluteWeight(childTransaction1.getMaterialAbsoluteWeight());
						childTransactionDto1.setMaterialType(childTransaction1.getMaterial().getMaterialId());
						childTransactionDto1.setVat(childTransaction1.getVat());
						childTransactionDto1.setVatCost(childTransaction1.getVatCost());
						childTransactionDto1.setMaterialPricewithVat(childTransaction1.getMaterialPriceAfterVat());
						childTransactionDto1.setMaterialPricewithoutVat(childTransaction1.getMaterialPrice());
						childtransactionDetialsDTO2.add(childTransactionDto1);
					}
				}
				dto.setChildTransactionDtoList(childtransactionDetialsDTO2);
				transObj.setModifiedDate(LocalDate.now());
				transObj.setTransactionCompleted(true);
				//transaction completed
				status = statusMasterRepository.findByStatusId(1);
				transObj.setStatus(status);
				dto.setTransactionStatus(status.getStatusName());
				if(dto.getId()!=0) {
					transObj.setTransactionId(dto.getId());
				}
			} else {
				dto.setIsTransactionCancelled((dto.getCancelReason()!=null && dto.getCancelReason()!="")?
						true : false);
				if(dto.getIsTransactionCancelled()) {
					//transaction cancelled
//					transObj.setStatus(statusMasterRepository.findByStatusId(2));
					transObj.setCancelReason(dto.getCancelReason());
					transObj.setModifiedDate(LocalDate.now());
					transObj.setTransactionCompleted(true);
					status = statusMasterRepository.findByStatusId(2);
					transObj.setStatus(status);
					dto.setTransactionStatus(status.getStatusName());
				} else { //temporary transaction
					int sizeOfChildRecords = dto.getChildTransactionDtoList().size();
					ChildTransactionDto lastRecordAdded = dto.getChildTransactionDtoList().get(sizeOfChildRecords-1);
					ChildTransaction childTransactionLastOfDB;
					ChildTransaction childTransactionNewAddition;
					List<ChildTransaction> 	childTransactionListTemp = transObj.getTransactionDetials()!=null ? transObj.getTransactionDetials() : new ArrayList<ChildTransaction>();
					if(sizeOfChildRecords!=0){
						if(transObj.getTransactionDetials().size()!=0) {
							childTransactionLastOfDB = childTransactionListTemp.get(transObj.getTransactionDetials().size()-1);
							childTransactionLastOfDB.setMaterialSecondWeight(lastRecordAdded.getFirstWeight());
							childTransactionRepository.saveAndFlush(childTransactionLastOfDB);
						} 
							childTransactionNewAddition = new ChildTransaction();
							Material materialDB = materialRepository.findByMaterialId(lastRecordAdded.getMaterialType());
							childTransactionNewAddition.setMaterial(materialDB);
							childTransactionNewAddition.setBaleOrLoose(lastRecordAdded.getBaleOrLoose()); //B or L
							childTransactionNewAddition.setMaterialFirstWeight(lastRecordAdded.getFirstWeight());
							childTransactionNewAddition.setTransactionsHeader(transObj);
							ChildTransaction newChildTransaction = childTransactionRepository.saveAndFlush(childTransactionNewAddition);
							dto.getChildTransactionDtoList().get(sizeOfChildRecords-1)
								.setId(newChildTransaction.getChildTransactionId());
							childTransactionListTemp.add(childTransactionNewAddition);
					}
					transObj.setTransactionDetials(childTransactionListTemp);
					transObj.setVatIncluded(dto.getIncludeVat());
					transObj.setTransactionCompleted(false);
					status = statusMasterRepository.findByStatusId(3); 					//transaction temporary
					transObj.setStatus(status);
					dto.setTransactionStatus(status.getStatusName());
				}
					transObj.setCreatedDate(LocalDate.now());
					
					if(dto.getId()!=0) {
						transObj.setTransactionId(dto.getId());
					}
				}
			}
		TransactionsHeader transObj2 = transactionRepository.saveAndFlush(transObj);
		dto.setId(transObj2.getTransactionId());
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
		StatusMaster status = new StatusMaster(3, "TEMPORARY");
		List<TransactionsHeader> transactionList  = 
				transactionRepository.findByStatusAndCreatedDate(status, LocalDate.now());
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
				transDto.setPhoneNumber(transactionObj.getPhoneNumber());
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
			transDto.setPhoneNumber(transactionObj.getPhoneNumber());
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
