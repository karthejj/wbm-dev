package com.example.WBMdemo.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
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
	@Autowired
	private InitialWeightService rawWeightService;
	
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
				transactions.setCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
			}
			transactions.setCreatedBy(dto.getCreated_by());
			transactions.setCustomerName(dto.getCustomerName());
			transactions.setCustomerId(dto.getCustomerId());
			transactions.setVehicleNumber(dto.getVehicleNumber());
			//vehicle type
			transactions.setVehicle_type(dto.getVehicleType());
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
							childTransaction1.setMaterialPrice(childTransaction1.getBaleOrLoose().equals("B") ? 
									materialDB.getMaterialIncBalePrice().multiply(new BigDecimal(1000))
								: materialDB.getMaterialIncLoosePrice().multiply(new BigDecimal(1000)));
						} else if(dto.getTransferType().name().equals("OUT")) {
							materialPriceWithoutVat = childTransaction1.getBaleOrLoose().equals("B") ? 
									materialDB.getMaterialOutBalePrice().multiply(absoluteWeight)
								: materialDB.getMaterialOutLoosePrice().multiply(absoluteWeight);
							childTransaction1.setMaterialPrice(childTransaction1.getBaleOrLoose().equals("B") ? 
									materialDB.getMaterialOutBalePrice().multiply(new BigDecimal(1000))
								: materialDB.getMaterialOutLoosePrice().multiply(new BigDecimal(1000)));
						} else {
							materialPriceWithoutVat = childTransaction1.getBaleOrLoose().equals("B") ? 
									materialDB.getMaterialOutBalePrice().multiply(absoluteWeight)
								: materialDB.getMaterialOutLoosePrice().multiply(absoluteWeight);
						}
						childTransaction1.setTransactionPrice(materialPriceWithoutVat);
						childTransaction1.setVatIncluded(dto.getIncludeVat()); // set the vat 
						if(dto.getIncludeVat()) {
							childTransaction1.setTransactionPriceAfterVat(materialPriceWithoutVat.add(materialPriceWithoutVat.multiply(materialDB.getVat()).divide(new BigDecimal(100))));
							childTransaction1.setVat(materialDB.getVat());
							childTransaction1.setVatCost(childTransaction1.getTransactionPriceAfterVat().subtract(materialPriceWithoutVat));
						} else {
							childTransaction1.setTransactionPriceAfterVat(materialPriceWithoutVat);
							childTransaction1.setVat(new BigDecimal(0));
							childTransaction1.setVatCost(new BigDecimal(0));
						}
						childTransaction1.setTransactionsHeader(transObj);

						childTransactionRepository.save(childTransaction1);
//						childTransactionList.add(childTransaction1);
					}
					transObj.setTransactionDetials(childTransactionList);
					transObj.setTotalWeight(new BigDecimal(
							childTransactionList.stream().mapToDouble(o -> o.getMaterialAbsoluteWeight().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
					transObj.setFinalAmountWithoutVat(new BigDecimal(
							childTransactionList.stream().mapToDouble(o -> o.getTransactionPrice().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));

					transObj.setFinalAmountWithVat(new BigDecimal(
							childTransactionList.stream().mapToDouble(o -> o.getTransactionPriceAfterVat().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
//					transObj.setVatIncluded(dto.getIncludeVat());
					transObj.setVatCost(transObj.getFinalAmountWithVat()
							.subtract(transObj.getFinalAmountWithoutVat()));
					dto.setTotalWeight(transObj.getTotalWeight());
					dto.setVatCost(transObj.getVatCost());
					//setting the final Amount without & with round off checking vat - 10-jun-23
//					if(dto.getIncludeVat()) {
						dto.setFinalAmountRoundOff(new BigDecimal(transObj.getFinalAmountWithVat().toBigInteger().toString()));
						dto.setFinalAmount(transObj.getFinalAmountWithVat());
//					} else {
//						dto.setFinalAmountRoundOff(new BigDecimal(transObj.getMaterialPrice().toBigInteger().toString()));
//						dto.setFinalAmount(transObj.getMaterialPrice());
//					}
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
						childTransactionDto1.setTransactionPricewithVat(childTransaction1.getTransactionPriceAfterVat());
						childTransactionDto1.setTransactionPricewithoutVat(childTransaction1.getTransactionPrice());
						
						childTransactionDto1.setMaterialPricePerTonne(childTransaction1.getMaterialPrice());
						
						//rounding off the amount - 10-jun-23
						childTransactionDto1.setTransactionPricewithVatRoundOff(new BigDecimal(childTransaction1.getTransactionPriceAfterVat().toBigInteger().toString()));
						childTransactionDto1.setTransactionPricewithoutVatRoundOff(new BigDecimal(childTransaction1.getTransactionPrice().toBigInteger().toString()));
						//get vat included or not - 11-jun-23
						childTransactionDto1.setIncludeVat(childTransaction1.getVatIncluded());
						childtransactionDetialsDTO2.add(childTransactionDto1);
					}
				}
				dto.setChildTransactionDtoList(childtransactionDetialsDTO2);
				transObj.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
				transObj.setCreatedBy(dto.getCreated_by());
				transObj.setClosedDate(LocalDateTime.now(ZoneOffset.UTC));
				transObj.setClosedBy(dto.getClosed_by());
				transObj.setTransactionCompleted(true);
				//transaction completed
				status = statusMasterRepository.findByStatusId(1);
				transObj.setStatus(status);
				dto.setTransactionStatus(status.getStatusName());
				if(dto.getId()!=0) {
					transObj.setTransactionId(dto.getId());
				}
				//for latest weight read from device
				if(dto.getRawWeightId()!=null) {
					transObj.setRawWeightId(Long.parseLong(dto.getRawWeightId()));
				}
				TransactionsHeader transObj2 = transactionRepository.saveAndFlush(transObj);
				dto.setId(transObj2.getTransactionId());
				
				dto.setCreated_date(format_date(transObj2.getCreatedDate()));
				dto.setClosed_date(Objects.nonNull(transObj2.getClosedDate()) ? format_date(transObj2.getClosedDate()) : null);
			} else {
				dto.setIsTransactionCancelled((dto.getCancelReason()!=null && dto.getCancelReason()!="")?
						true : false);
				if(dto.getIsTransactionCancelled()) {
					//transaction cancelled
//					transObj.setStatus(statusMasterRepository.findByStatusId(2));
					transObj.setCancelReason(dto.getCancelReason());
					transObj.setModifiedDate(LocalDateTime.now(ZoneOffset.UTC));
					transObj.setClosedDate(LocalDateTime.now(ZoneOffset.UTC));
					transObj.setClosedBy(dto.getClosed_by());
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
							childTransactionNewAddition.setVatIncluded(dto.getIncludeVat());
							childTransactionNewAddition.setTransactionsHeader(transObj);
							
							if(dto.getTransferType().name().equals("INC")) {
								childTransactionNewAddition.setMaterialPrice(lastRecordAdded.getBaleOrLoose().equals("B") ? 
										materialDB.getMaterialIncBalePrice().multiply(new BigDecimal(1000))
									: materialDB.getMaterialIncLoosePrice().multiply(new BigDecimal(1000)));
							} else if(dto.getTransferType().name().equals("OUT")) {
								childTransactionNewAddition.setMaterialPrice(lastRecordAdded.getBaleOrLoose().equals("B") ? 
										materialDB.getMaterialOutBalePrice().multiply(new BigDecimal(1000))
									: materialDB.getMaterialOutLoosePrice().multiply(new BigDecimal(1000)));
							} else { // Outgoing considered default 
								childTransactionNewAddition.setMaterialPrice(lastRecordAdded.getBaleOrLoose().equals("B") ? 
										materialDB.getMaterialOutBalePrice().multiply(new BigDecimal(1000))
									: materialDB.getMaterialOutLoosePrice().multiply(new BigDecimal(1000)));
							}
							
						
							ChildTransaction newChildTransaction = childTransactionRepository.saveAndFlush(childTransactionNewAddition);
							dto.getChildTransactionDtoList().get(sizeOfChildRecords-1)
								.setId(newChildTransaction.getChildTransactionId());
							childTransactionListTemp.add(childTransactionNewAddition);
					}
					transObj.setTransactionDetials(childTransactionListTemp);
//					transObj.setVatIncluded(dto.getIncludeVat());
					transObj.setTransactionCompleted(false);
					status = statusMasterRepository.findByStatusId(3); 					//transaction temporary
					transObj.setStatus(status);
					dto.setTransactionStatus(status.getStatusName());
//					transObj.setCreatedDate(LocalDateTime.now());

				}
					
					
					if(dto.getId()!=0) {
						transObj.setTransactionId(dto.getId());
					}
					//for latest weight read from device
					if(dto.getRawWeightId()!=null) {
						transObj.setRawWeightId(Long.parseLong(dto.getRawWeightId()));
					}
					TransactionsHeader transObj2 = transactionRepository.saveAndFlush(transObj);
					dto.setId(transObj2.getTransactionId());
					
					dto.setCreated_date(format_date(transObj2.getCreatedDate()));
					dto.setClosed_date(Objects.nonNull(transObj2.getClosedDate()) ? format_date(transObj2.getClosedDate()) : null);
					dto.setCreated_by(transObj2.getCreatedBy());
					dto.setClosed_by(transObj2.getClosedBy());
					
					
					
					
					
					
					List<ChildTransactionDto> transactionDetials = null;
					List<ChildTransaction> transactionDetialsfromDB = null;
					if(transObj2.getTransactionDetials().size()!=0) {
						transactionDetialsfromDB = transObj2.getTransactionDetials();
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
							childTransactionDto.setTransactionPricewithoutVat(childtrans.getTransactionPrice());
							childTransactionDto.setTransactionPricewithVat(childtrans.getTransactionPriceAfterVat());
							
							//rounding off the amount - 10-jun-23
							if(childtrans.getTransactionPriceAfterVat()!=null) {
								childTransactionDto.setTransactionPricewithVatRoundOff(new BigDecimal(childtrans.getTransactionPriceAfterVat().toBigInteger().toString()));
							}
							if(childtrans.getTransactionPrice()!=null) {
								childTransactionDto.setTransactionPricewithoutVatRoundOff(new BigDecimal(childtrans.getTransactionPrice().toBigInteger().toString()));
							}
							
							childTransactionDto.setMaterialPricePerTonne(childtrans.getMaterialPrice());
							childTransactionDto.setIncludeVat(childtrans.getVatIncluded());
							childTransactionDto.setId(childtrans.getChildTransactionId());
							transactionDetials.add(childTransactionDto);
						}
					}

					
					dto.setChildTransactionDtoList(transactionDetials);
				
					
				}
			}

		
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
						transactionRepository.findByCreatedDate(LocalDateTime.now(ZoneOffset.UTC));
				List<TransactionsHeader> sortedUsers = transactionList.stream()
						  .sorted(Comparator.comparing(TransactionsHeader::getTransactionId))
						  .collect(Collectors.toList());
		
		return transactionByField(sortedUsers);
	}

	@Override
	public List<TransactionDto> fetchTemporaryTransactionList(){
		StatusMaster status = new StatusMaster(3, "TEMPORARY");
		List<TransactionsHeader> transactionList  = 
				transactionRepository.findByStatusAndCreatedDate(status, LocalDate.now(ZoneOffset.UTC));
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
				transDto.setVehicleType(transactionObj.getVehicle_type()); // vehicle type
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
						childTransactionDto.setTransactionPricewithoutVat(childtrans.getTransactionPrice());
						childTransactionDto.setTransactionPricewithVat(childtrans.getTransactionPriceAfterVat());
						
						//rounding off the amount - 10-jun-23
						if(childtrans.getTransactionPriceAfterVat()!=null) {
							childTransactionDto.setTransactionPricewithVatRoundOff(new BigDecimal(childtrans.getTransactionPriceAfterVat().toBigInteger().toString()));
						}
						if(childtrans.getTransactionPrice()!=null) {
							childTransactionDto.setTransactionPricewithoutVatRoundOff(new BigDecimal(childtrans.getTransactionPrice().toBigInteger().toString()));
						}
						childTransactionDto.setMaterialPricePerTonne(childtrans.getMaterialPrice());
						childTransactionDto.setIncludeVat(childtrans.getVatIncluded());
						childTransactionDto.setId(childtrans.getChildTransactionId());
						transactionDetials.add(childTransactionDto);
					}
				}
				
				transDto.setTotalWeight(transactionObj.getTotalWeight());
				transDto.setVatCost(transactionObj.getVatCost());
								
				transDto.setChildTransactionDtoList(transactionDetials);
				transDto.setIsTransactionCompleted(transactionObj.getTransactionCompleted());
				transDto.setTransactionStatus(transactionObj.getStatus() !=null ?
						transactionObj.getStatus().getStatusName() : "");
				transDto.setCancelReason(transactionObj.getCancelReason());
				transDto.setCreated_by(transactionObj.getCreatedBy());
				transDto.setClosed_by(transactionObj.getClosedBy());
				transDto.setCreated_date(Objects.nonNull(transactionObj.getCreatedDate()) ? 
						format_date(transactionObj.getCreatedDate()) : null);
				transDto.setClosed_date(Objects.nonNull(transactionObj.getClosedDate()) ? 
						format_date(transactionObj.getClosedDate()) : null);
				if(transactionObj.getFinalAmountWithVat()!=null) {
					transDto.setFinalAmountRoundOff(new BigDecimal(transactionObj.getFinalAmountWithVat().toBigInteger().toString()));
				}
				transDto.setFinalAmount(transactionObj.getFinalAmountWithVat());
				
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
			transDto.setVehicleType(transactionObj.getVehicle_type()); // vehicle type
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
					childTransactionDto.setTransactionPricewithoutVat(childtrans.getTransactionPrice());
					childTransactionDto.setTransactionPricewithVat(childtrans.getTransactionPriceAfterVat());
					
					//rounding off the amount - 10-jun-23
					if(childtrans.getTransactionPriceAfterVat()!=null) {
						childTransactionDto.setTransactionPricewithVatRoundOff(new BigDecimal(childtrans.getTransactionPriceAfterVat().toBigInteger().toString()));
					}
					if(childtrans.getTransactionPrice()!=null) {
						childTransactionDto.setTransactionPricewithoutVatRoundOff(new BigDecimal(childtrans.getTransactionPrice().toBigInteger().toString()));
					}
					childTransactionDto.setMaterialPricePerTonne(childtrans.getMaterialPrice());
					childTransactionDto.setIncludeVat(childtrans.getVatIncluded());
					childTransactionDto.setId(childtrans.getChildTransactionId());
					transactionDetials.add(childTransactionDto);
				}
			}
			
			transDto.setTotalWeight(transactionObj.getTotalWeight());
			transDto.setVatCost(transactionObj.getVatCost());
			
			transDto.setChildTransactionDtoList(transactionDetials);
			transDto.setIsTransactionCompleted(transactionObj.getTransactionCompleted());
			transDto.setTransactionStatus(transactionObj.getStatus() !=null ?
					transactionObj.getStatus().getStatusName() : "");
			transDto.setCancelReason(transactionObj.getCancelReason());
			transDto.setTransferType(TransferType.valueOf(transactionObj.getTransfer_type()));
			transDto.setCreated_by(transactionObj.getCreatedBy());
			transDto.setClosed_by(transactionObj.getClosedBy());
			transDto.setCreated_date(Objects.nonNull(transactionObj.getCreatedDate()) ? 
					format_date(transactionObj.getCreatedDate()) : null);
			transDto.setClosed_date(Objects.nonNull(transactionObj.getClosedDate()) ? 
					format_date(transactionObj.getClosedDate()) : null);
			
			if(transactionObj.getFinalAmountWithVat()!=null) {
				transDto.setFinalAmountRoundOff(new BigDecimal(transactionObj.getFinalAmountWithVat().toBigInteger().toString()));
			}
			
			transDto.setFinalAmount(transactionObj.getFinalAmountWithVat());

		} else {
			throw new TransactionNotFoundException("Transaction Not Found ");
		}
		return transDto;
	}
	

private String format_date(LocalDateTime time) {
	
	// Format the OffsetDateTime to ISO 8601 format
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    String utcTimeInISOFormat = time.format(formatter);
	return utcTimeInISOFormat;
	
	
// Old Code
	
//	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
//	SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//	Date d=new Date();
//	try {
//		d = sdf.parse(time.toString());
//	} catch (ParseException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	String formattedTime = output.format(d);
//	return formattedTime;
	
    
// Below for UTC ISO format	
	
//        // Get the current LocalDateTime
//        LocalDateTime localDateTime = LocalDateTime.now();
//
//        // Convert the LocalDateTime to an OffsetDateTime with UTC offset
//        OffsetDateTime offsetDateTime = localDateTime.atOffset(ZoneOffset.UTC);
//
//        // Format the OffsetDateTime to ISO 8601 format
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
//        String utcTimeInISOFormat = offsetDateTime.format(formatter);
//
//        // Print the UTC time in ISO format
//        System.out.println(utcTimeInISOFormat);
  
	
}

//@Scheduled(cron = "${app.scheduler.transaction.process.cron}")
//public void schedulerOnMidnight() {   
//	transactionRepository.updateTransactionGreatTwentyFourHr();
//
//}


}
