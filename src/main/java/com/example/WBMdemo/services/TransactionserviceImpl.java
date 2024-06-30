package com.example.WBMdemo.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.WBMdemo.dto.ChildTransactionDto;
import com.example.WBMdemo.dto.CommonTransactiondto;
import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.entity.ChildTransaction;
import com.example.WBMdemo.entity.FileDB;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.entity.StatusMaster;
import com.example.WBMdemo.entity.TransactionsHeader;
import com.example.WBMdemo.entity.TransferType;
import com.example.WBMdemo.errors.TransactionNotFoundException;
import com.example.WBMdemo.message.ResponseMessage;
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
	@Autowired
	private FileStorageService storageService;
	
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
				ResponseMessage message1 = new ResponseMessage();
				if(transObj.getVehicle_type().equals("HT") && dto.getFile()!=null && dto.getFile().getSize()!=0) {
					message1 = uploadFile(dto.getFile());
				}
				if(childtransactionDetialsDTO.size()!=0){
					int iterationCount=0;
					for(ChildTransaction childTransaction1 : childTransactionList) {
							Material materialDB = materialRepository.findByMaterialLookupId(dto.getChildTransactionDtoList().get(iterationCount).getMaterialType());
							childTransaction1.setMaterial(materialDB);
						transactionPriceCalculation(dto, transObj, childtransactionDetialsDTO, childTransaction1);
						//for multiple transaction entry, only two transaction designed as of now, so second upload file of second transaction is final
						if(iterationCount==childtransactionDetialsDTO.size()-1) {
							childTransaction1.setSecondUploadedFileId(message1.getFileID());
							childTransaction1.setModifiedBy(dto.getClosed_by());
							childTransaction1.setModifiedDateTime(LocalDateTime.now(ZoneOffset.UTC));
						}

						childTransactionRepository.save(childTransaction1);
//						childTransactionList.add(childTransaction1);
						iterationCount++;
					}
					finalWeightCalculation(dto, transObj, childTransactionList);
				}
				List<ChildTransactionDto> childtransactionDetialsDTO2 = new ArrayList<ChildTransactionDto>();
				int iteration = 0;
				if(childtransactionDetialsDTO.size()!=0){
					for(ChildTransaction childTransaction1 : childTransactionList) {
						ChildTransactionDto childTransactionDto1 = new ChildTransactionDto();
						childTransactionDto1.setId(childTransaction1.getChildTransactionId());
						childTransactionDto1.setBaleOrLoose(childTransaction1.getBaleOrLoose());
						childTransactionDto1.setFirstWeight(childTransaction1.getMaterialFirstWeight());
						childTransactionDto1.setSecondWeight(childTransaction1.getMaterialSecondWeight());
						childTransactionDto1.setAbsoluteWeight(childTransaction1.getMaterialAbsoluteWeight());
						childTransactionDto1.setMaterialType(childTransaction1.getMaterial().getMaterialLookupId());
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
						childTransactionDto1.setFirstUploadedFileId(childTransaction1.getFirstUploadedFileId());
						childTransactionDto1.setSecondUploadedFileId(childTransaction1.getSecondUploadedFileId());
						childTransactionDto1.setCreatedBy(childTransaction1.getCreatedBy());
						childTransactionDto1.setCreatedDate(childTransaction1.getCreatedDateTime()!=null?
								childTransaction1.getCreatedDateTime().toString() : null);
						childTransactionDto1.setModifiedBy(childTransaction1.getModifiedBy());
						childTransactionDto1.setModifiedDate(childTransaction1.getModifiedDateTime()!=null?
								childTransaction1.getModifiedDateTime().toString() : null);
						childtransactionDetialsDTO2.add(childTransactionDto1);
						iteration++;
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
					ResponseMessage message1 = new ResponseMessage();
					if(dto.getFile()!=null && dto.getFile().getSize()!=0) {
						message1 = uploadFile(dto.getFile());
					}
					if(sizeOfChildRecords!=0){
						if(transObj.getTransactionDetials().size()!=0) {
							childTransactionLastOfDB = childTransactionListTemp.get(transObj.getTransactionDetials().size()-1);
							childTransactionLastOfDB.setMaterialSecondWeight(lastRecordAdded.getFirstWeight());
							childTransactionLastOfDB.setModifiedBy(dto.getClosed_by());
							childTransactionLastOfDB.setModifiedDateTime(LocalDateTime.now(ZoneOffset.UTC));
							//second upload file Id for first cycle is the file uploaded on second temporary transaction
							childTransactionLastOfDB.setSecondUploadedFileId(message1.getFileID());
							childTransactionRepository.saveAndFlush(childTransactionLastOfDB);
						} 
							childTransactionNewAddition = new ChildTransaction();
							Material materialDB = materialRepository.findByMaterialLookupId(lastRecordAdded.getMaterialType());
							childTransactionNewAddition.setMaterial(materialDB);
							childTransactionNewAddition.setBaleOrLoose(lastRecordAdded.getBaleOrLoose()); //B or L
							childTransactionNewAddition.setMaterialFirstWeight(lastRecordAdded.getFirstWeight());
							childTransactionNewAddition.setVatIncluded(dto.getIncludeVat());
							childTransactionNewAddition.setTransactionsHeader(transObj);

							childTransactionNewAddition.setFirstUploadedFileId(message1.getFileID());	
//							if(dto.getTransferType().name().equals("INC")) {
//								childTransactionNewAddition.setMaterialPrice(lastRecordAdded.getBaleOrLoose().equals("B") ? 
//										materialDB.getMaterialIncBalePrice().multiply(new BigDecimal(1000))
//									: materialDB.getMaterialIncLoosePrice().multiply(new BigDecimal(1000)));
//							} else if(dto.getTransferType().name().equals("OUT")) {
//								childTransactionNewAddition.setMaterialPrice(lastRecordAdded.getBaleOrLoose().equals("B") ? 
//										materialDB.getMaterialOutBalePrice().multiply(new BigDecimal(1000))
//									: materialDB.getMaterialOutLoosePrice().multiply(new BigDecimal(1000)));
//							} else { // Outgoing considered default 
//								childTransactionNewAddition.setMaterialPrice(lastRecordAdded.getBaleOrLoose().equals("B") ? 
//										materialDB.getMaterialOutBalePrice().multiply(new BigDecimal(1000))
//									: materialDB.getMaterialOutLoosePrice().multiply(new BigDecimal(1000)));
//							}
//							if(dto.getTransferType().name().equals("WEIGH")){
								transactionPriceCalculation(dto, transObj, childtransactionDetialsDTO, childTransactionNewAddition);
//							}
							childTransactionNewAddition.setCreatedBy(dto.getCreated_by());
							childTransactionNewAddition.setCreatedDateTime(LocalDateTime.now(ZoneOffset.UTC));
							ChildTransaction newChildTransaction = childTransactionRepository.saveAndFlush(childTransactionNewAddition);
							dto.getChildTransactionDtoList().get(sizeOfChildRecords-1)
								.setId(newChildTransaction.getChildTransactionId());
							childTransactionListTemp.add(childTransactionNewAddition);
					}
					transObj.setTransactionDetials(childTransactionListTemp);
//					transObj.setVatIncluded(dto.getIncludeVat());
					if(dto.getTransferType().name().equals("WEIGH")){
						finalWeightCalculation(dto, transObj, childTransactionListTemp);
					}
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
							childTransactionDto.setMaterialType(childtrans.getMaterial().getMaterialLookupId());
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
							
//							childTransactionDto.setUploadedFileId(childtrans.getUploadedFileId()!=null? childtrans.getUploadedFileId() : "NA");
//							childTransactionDto.setMultipleTransClosureUploadId("NA");
							childTransactionDto.setFirstUploadedFileId(childtrans.getFirstUploadedFileId()!=null? childtrans.getFirstUploadedFileId() : "NA");
							childTransactionDto.setSecondUploadedFileId(childtrans.getSecondUploadedFileId()!=null? childtrans.getSecondUploadedFileId() : "NA");
							childTransactionDto.setCreatedBy(childtrans.getCreatedBy());
							childTransactionDto.setCreatedDate(childtrans.getCreatedDateTime()!=null? 
									childtrans.getCreatedDateTime().toString() : null);
							childTransactionDto.setModifiedBy(childtrans.getModifiedBy());
							childTransactionDto.setModifiedDate(childtrans.getModifiedDateTime()!=null? 
									childtrans.getModifiedDateTime().toString() : null);
							
							transactionDetials.add(childTransactionDto);
						}
					}

					
					dto.setChildTransactionDtoList(transactionDetials);
				
					
				}
			}

		
		return dto;
	}

	private void finalWeightCalculation(TransactionDto dto, TransactionsHeader transObj,
			List<ChildTransaction> childTransactionList) {
		transObj.setTransactionDetials(childTransactionList);

		if(dto.getTransferType().name().equals("WEIGH")){
			transObj.setTotalWeight((childTransactionList.get(0).getMaterialSecondWeight()!=null) ? 
					childTransactionList.get(0).getMaterialFirstWeight().add(childTransactionList.get(0).getMaterialSecondWeight()) 
					: childTransactionList.get(0).getMaterialFirstWeight());
			transObj.setFinalAmountWithoutVat(childTransactionList.get(0).getTransactionPrice().setScale(2, RoundingMode.HALF_UP));
			transObj.setFinalAmountWithVat(childTransactionList.get(0).getTransactionPriceAfterVat().setScale(2, RoundingMode.HALF_UP));
		} else {
			transObj.setTotalWeight(new BigDecimal(
					childTransactionList.stream().mapToDouble(o -> o.getMaterialAbsoluteWeight().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
			transObj.setFinalAmountWithoutVat(new BigDecimal(
					childTransactionList.stream().mapToDouble(o -> o.getTransactionPrice().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
			transObj.setFinalAmountWithVat(new BigDecimal(
					childTransactionList.stream().mapToDouble(o -> o.getTransactionPriceAfterVat().doubleValue()).sum()).setScale(2, RoundingMode.HALF_UP));
		}
		transObj.setVatCost(transObj.getFinalAmountWithVat()
				.subtract(transObj.getFinalAmountWithoutVat()));
		dto.setTotalWeight(transObj.getTotalWeight());
		dto.setVatCost(transObj.getVatCost());
		dto.setFinalAmountRoundOff(new BigDecimal(transObj.getFinalAmountWithVat().toBigInteger().toString()));
		dto.setFinalAmount(transObj.getFinalAmountWithVat());
	}

	private void transactionPriceCalculation(TransactionDto dto, TransactionsHeader transObj,
			List<ChildTransactionDto> childtransactionDetialsDTO, ChildTransaction childTransaction1) {
		BigDecimal materialPriceWithoutVat = new BigDecimal(0);
		Material materialDB = materialRepository.findByMaterialLookupId(childTransaction1.getMaterial().getMaterialLookupId());
		BigDecimal absoluteWeight = new BigDecimal(0);
		if(dto.getTransferType().name().equals("WEIGH")){
			if(Objects.nonNull(dto.getChildTransactionDtoList().get(0).getSecondWeight())) {
				absoluteWeight = dto.getChildTransactionDtoList().get(0).getSecondWeight();
			} else {
				absoluteWeight = dto.getChildTransactionDtoList().get(0).getFirstWeight();
			}
			childTransaction1.setMaterialFirstWeight(dto.getChildTransactionDtoList().get(0).getFirstWeight());
			childTransaction1.setMaterialSecondWeight(dto.getChildTransactionDtoList().get(0).getSecondWeight());
			childTransaction1.setMaterialAbsoluteWeight(absoluteWeight);
		} else {
			BigDecimal secondWeight;
			if(Objects.nonNull(childTransaction1.getMaterialSecondWeight())) {
				secondWeight = childTransaction1.getMaterialSecondWeight();
				absoluteWeight = childTransaction1.getMaterialFirstWeight().subtract(secondWeight);
				childTransaction1.setMaterialSecondWeight(secondWeight);
				childTransaction1.setMaterialAbsoluteWeight(absoluteWeight);
			} else {
				secondWeight = childtransactionDetialsDTO.get(childtransactionDetialsDTO.size()-1).getSecondWeight();
				if(Objects.nonNull(secondWeight)) {
					absoluteWeight = childTransaction1.getMaterialFirstWeight().subtract(secondWeight);
					childTransaction1.setMaterialSecondWeight(secondWeight);
					childTransaction1.setMaterialAbsoluteWeight(absoluteWeight);
				
				}
			}
			childTransaction1.setMaterialFirstWeight(childTransaction1.getMaterialFirstWeight());

		}
		childTransaction1.setMaterial(materialDB);

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
					materialDB.getMaterialOutBalePrice()
				: materialDB.getMaterialOutLoosePrice();
		
			childTransaction1.setMaterialPrice(childTransaction1.getBaleOrLoose().equals("B") ? 
					materialDB.getMaterialOutBalePrice().multiply(new BigDecimal(1000))
				: materialDB.getMaterialOutLoosePrice().multiply(new BigDecimal(1000)));
		}
		
		childTransaction1.setTransactionPrice(materialPriceWithoutVat);
		childTransaction1.setVatIncluded(dto.getIncludeVat()); // set the vat 
		if(Objects.nonNull(dto.getIncludeVat()) && dto.getIncludeVat()) {
			childTransaction1.setTransactionPriceAfterVat(materialPriceWithoutVat.add(materialPriceWithoutVat.multiply(materialDB.getVat()).divide(new BigDecimal(100))));
			childTransaction1.setVat(materialDB.getVat());
			childTransaction1.setVatCost(childTransaction1.getTransactionPriceAfterVat().subtract(materialPriceWithoutVat));
		} else {
			childTransaction1.setTransactionPriceAfterVat(materialPriceWithoutVat);
			childTransaction1.setVat(new BigDecimal(0));
			childTransaction1.setVatCost(new BigDecimal(0));
		}
//		childTransaction1.setCreatedBy(dto.getCreated_by());
//		childTransaction1.setCreatedDateTime(dto.getCreated_date());
		childTransaction1.setTransactionsHeader(transObj);
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
		
		return transactionByFieldForCount(transactionList);
	}
	
	@Override
	public CommonTransactiondto fetchTransactionListPage(String sortParam, String sortBy,  int page, int size) {
		// TODO Auto-generated method stub
		List<TransactionsHeader> transactionList = null;
		List<TransactionDto> transactionDtoList;
		CommonTransactiondto commonTransactiondto = new CommonTransactiondto();
		Sort sort = sortBy.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortParam).ascending()
				: Sort.by(sortParam).descending();
		Pageable pageable = PageRequest.of(page, size, sort);
		Page<TransactionsHeader> posts = transactionRepository.findAll(pageable);
		transactionList = posts.getContent();
//		if(sortParam!=null && order!=0) {
//			if(order==1) {
//				transactionList = 
//						transactionRepository.findAll(Sort.by(Sort.Direction.ASC, sortParam));
//				
//			} else {
//				transactionList = 
//						transactionRepository.findAll(Sort.by(Sort.Direction.DESC, sortParam));
//			}
//		} else {
//			transactionList = 
//					transactionRepository.findAll(Sort.by(Sort.Direction.ASC));
//		}
		transactionDtoList = transactionByField(transactionList);
		commonTransactiondto.setTransactionDtoList(transactionDtoList);
		commonTransactiondto.setTotalTransaction(fetchTransactionList(sortParam, 1).size());
		return commonTransactiondto;
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
	
//	private List<TransactionDto> transactionByField1(List<TransactionsHeader> sortedUsers) {
//		List<TransactionDto> transactionDtoList = new ArrayList<TransactionDto>();
//		System.out.println(System.currentTimeMillis());
//			TransactionDto transDto = null;
//			for(TransactionsHeader transactionObj : sortedUsers) {
//				transDto = new TransactionDto();
//				transDto.setId(transactionObj.getTransactionId());
//				transDto.setCustomerId(transactionObj.getCustomerId());
//				transDto.setCustomerName(transactionObj.getCustomerName());
//				transDto.setCustomerType(transactionObj.getCustomer() != null ? 
//						transactionObj.getCustomer().getCustomerId() : 0);
//				transDto.setVehicleNumber(transactionObj.getVehicleNumber());
//				transDto.setVehicleType(transactionObj.getVehicle_type()); // vehicle type
//				transDto.setDriverCount(transactionObj.getDriverCount());
//				transDto.setPhoneNumber(transactionObj.getPhoneNumber());
//				transDto.setTransferType(TransferType.valueOf(transactionObj.getTransfer_type()));
//				
//				List<ChildTransactionDto> transactionDetials = new ArrayList<ChildTransactionDto>();
//				if(transactionObj.getTransactionDetials().size()!=0) {
//					transactionObj.getTransactionDetials().forEach(childtrans -> {
//						ChildTransactionDto childTransactionDto = new ChildTransactionDto();
//							childTransactionDto.setBaleOrLoose(childtrans.getBaleOrLoose());
//							childTransactionDto.setFirstWeight(childtrans.getMaterialFirstWeight());
//							childTransactionDto.setSecondWeight(childtrans.getMaterialSecondWeight());
//							childTransactionDto.setAbsoluteWeight(childtrans.getMaterialAbsoluteWeight());
//							childTransactionDto.setMaterialType(childtrans.getMaterial().getMaterialId());
//							childTransactionDto.setVat(childtrans.getVat());
//							childTransactionDto.setVatCost(childtrans.getVatCost());
//							childTransactionDto.setTransactionPricewithoutVat(childtrans.getTransactionPrice());
//							childTransactionDto.setTransactionPricewithVat(childtrans.getTransactionPriceAfterVat());
//							
//							//rounding off the amount - 10-jun-23
//							if(childtrans.getTransactionPriceAfterVat()!=null) {
//								childTransactionDto.setTransactionPricewithVatRoundOff(new BigDecimal(childtrans.getTransactionPriceAfterVat().toBigInteger().toString()));
//							}
//							if(childtrans.getTransactionPrice()!=null) {
//								childTransactionDto.setTransactionPricewithoutVatRoundOff(new BigDecimal(childtrans.getTransactionPrice().toBigInteger().toString()));
//							}
//							childTransactionDto.setMaterialPricePerTonne(childtrans.getMaterialPrice());
//							childTransactionDto.setIncludeVat(childtrans.getVatIncluded());
//							childTransactionDto.setId(childtrans.getChildTransactionId());
//							transactionDetials.add(childTransactionDto);
//					});
//				}
//				transDto.setTotalWeight(transactionObj.getTotalWeight());
//				transDto.setVatCost(transactionObj.getVatCost());
//				transDto.setChildTransactionDtoList(transactionDetials);
//				transDto.setIsTransactionCompleted(transactionObj.getTransactionCompleted());
//				transDto.setTransactionStatus(transactionObj.getStatus() !=null ?
//						transactionObj.getStatus().getStatusName() : "");
//				transDto.setCancelReason(transactionObj.getCancelReason());
//				transDto.setCreated_by(transactionObj.getCreatedBy());
//				transDto.setClosed_by(transactionObj.getClosedBy());
//				transDto.setCreated_date(Objects.nonNull(transactionObj.getCreatedDate()) ? 
//						format_date(transactionObj.getCreatedDate()) : null);
//				transDto.setClosed_date(Objects.nonNull(transactionObj.getClosedDate()) ? 
//						format_date(transactionObj.getClosedDate()) : null);
//				if(transactionObj.getFinalAmountWithVat()!=null) {
//					transDto.setFinalAmountRoundOff(new BigDecimal(transactionObj.getFinalAmountWithVat().toBigInteger().toString()));
//				}
//				transDto.setFinalAmount(transactionObj.getFinalAmountWithVat());
//				
//				transactionDtoList.add(transDto);
//			}
//			System.out.println(System.currentTimeMillis());
//			return transactionDtoList;
////		} 
////		else {
////			System.out.println(System.currentTimeMillis());
////			return transactionDtoList;
////		}
//	}
	
	
	private List<TransactionDto> transactionByField(List<TransactionsHeader> sortedUsers) {
		List<TransactionDto> transactionDtoList = new ArrayList<TransactionDto>();
		System.out.println(System.currentTimeMillis());
			sortedUsers.stream().forEach(transactionObj -> {
				TransactionDto transDto = new TransactionDto();
				transDto.setId(transactionObj.getTransactionId());
				transDto.setCustomerId(transactionObj.getCustomerId());
				transDto.setCustomerName(transactionObj.getCustomerName());
				transDto.setCustomerType(transactionObj.getCustomer() != null ? transactionObj.getCustomer().getCustomerId() : 0);
				transDto.setVehicleNumber(transactionObj.getVehicleNumber());
				transDto.setVehicleType(transactionObj.getVehicle_type()); 
				transDto.setDriverCount(transactionObj.getDriverCount());
				transDto.setPhoneNumber(transactionObj.getPhoneNumber());
				transDto.setTransferType(TransferType.valueOf(transactionObj.getTransfer_type()));
				transDto.setTotalWeight(transactionObj.getTotalWeight());
				transDto.setVatCost(transactionObj.getVatCost());
				transDto.setIsTransactionCompleted(transactionObj.getTransactionCompleted());
				transDto.setTransactionStatus(transactionObj.getStatus() !=null ? transactionObj.getStatus().getStatusName() : "");
				transDto.setCancelReason(transactionObj.getCancelReason());
				transDto.setCreated_by(transactionObj.getCreatedBy());
				transDto.setClosed_by(transactionObj.getClosedBy());
				transDto.setCreated_date(Objects.nonNull(transactionObj.getCreatedDate()) ? format_date(transactionObj.getCreatedDate()) : null);
				transDto.setClosed_date(Objects.nonNull(transactionObj.getClosedDate()) ? format_date(transactionObj.getClosedDate()) : null);
				if(transactionObj.getFinalAmountWithVat()!=null) {
					transDto.setFinalAmountRoundOff(new BigDecimal(transactionObj.getFinalAmountWithVat().toBigInteger().toString()));
				}
				transDto.setFinalAmount(transactionObj.getFinalAmountWithVat());
				if(Objects.nonNull(transactionObj.getStatus()) && transactionObj.getStatus().getStatusId() ==3) {
					List<ChildTransactionDto> transactionDetials = new ArrayList<ChildTransactionDto>();
					if(transactionObj.getTransactionDetials().size()!=0) {
						transactionObj.getTransactionDetials().forEach(childtrans -> {
							ChildTransactionDto childTransactionDto = new ChildTransactionDto();
								childTransactionDto.setBaleOrLoose(childtrans.getBaleOrLoose());
								childTransactionDto.setFirstWeight(childtrans.getMaterialFirstWeight());
								childTransactionDto.setSecondWeight(childtrans.getMaterialSecondWeight());
								childTransactionDto.setAbsoluteWeight(childtrans.getMaterialAbsoluteWeight());
								childTransactionDto.setMaterialType(childtrans.getMaterial().getMaterialLookupId());
								childTransactionDto.setVat(childtrans.getVat());
								childTransactionDto.setVatCost(childtrans.getVatCost());
								childTransactionDto.setTransactionPricewithoutVat(childtrans.getTransactionPrice());
								childTransactionDto.setTransactionPricewithVat(childtrans.getTransactionPriceAfterVat());
								if(childtrans.getTransactionPriceAfterVat()!=null) {
									childTransactionDto.setTransactionPricewithVatRoundOff(new BigDecimal(childtrans.getTransactionPriceAfterVat().toBigInteger().toString()));
								}
								if(childtrans.getTransactionPrice()!=null) {
									childTransactionDto.setTransactionPricewithoutVatRoundOff(new BigDecimal(childtrans.getTransactionPrice().toBigInteger().toString()));
								}
								childTransactionDto.setMaterialPricePerTonne(childtrans.getMaterialPrice());
								childTransactionDto.setIncludeVat(childtrans.getVatIncluded());
								childTransactionDto.setId(childtrans.getChildTransactionId());
								childTransactionDto.setFirstUploadedFileId(childtrans.getFirstUploadedFileId());
								childTransactionDto.setSecondUploadedFileId(childtrans.getSecondUploadedFileId());
								transactionDetials.add(childTransactionDto);
						});
					}
					transDto.setChildTransactionDtoList(transactionDetials);
				}

				transactionDtoList.add(transDto);
			});
			System.out.println(System.currentTimeMillis());
			return transactionDtoList;

	}

	private List<TransactionDto> transactionByFieldForCount(List<TransactionsHeader> sortedUsers) {
		List<TransactionDto> transactionDtoList = new ArrayList<TransactionDto>();
		System.out.println(System.currentTimeMillis());
			sortedUsers.stream().forEach(transactionObj -> {
				TransactionDto transDto = new TransactionDto();
				transDto.setId(transactionObj.getTransactionId());
				transDto.setCustomerId(transactionObj.getCustomerId());
				transDto.setCustomerName(transactionObj.getCustomerName());
				transDto.setCustomerType(transactionObj.getCustomer() != null ? transactionObj.getCustomer().getCustomerId() : 0);
				transDto.setVehicleNumber(transactionObj.getVehicleNumber());
				transDto.setVehicleType(transactionObj.getVehicle_type()); 
				transDto.setDriverCount(transactionObj.getDriverCount());
				transDto.setPhoneNumber(transactionObj.getPhoneNumber());
				transDto.setTransferType(TransferType.valueOf(transactionObj.getTransfer_type()));
				transDto.setTotalWeight(transactionObj.getTotalWeight());
				transDto.setVatCost(transactionObj.getVatCost());
				transDto.setIsTransactionCompleted(transactionObj.getTransactionCompleted());
				transDto.setTransactionStatus(transactionObj.getStatus() !=null ? transactionObj.getStatus().getStatusName() : "");
				transDto.setCancelReason(transactionObj.getCancelReason());
				transDto.setCreated_by(transactionObj.getCreatedBy());
				transDto.setClosed_by(transactionObj.getClosedBy());
				transDto.setCreated_date(Objects.nonNull(transactionObj.getCreatedDate()) ? format_date(transactionObj.getCreatedDate()) : null);
				transDto.setClosed_date(Objects.nonNull(transactionObj.getClosedDate()) ? format_date(transactionObj.getClosedDate()) : null);
				if(transactionObj.getFinalAmountWithVat()!=null) {
					transDto.setFinalAmountRoundOff(new BigDecimal(transactionObj.getFinalAmountWithVat().toBigInteger().toString()));
				}
				transDto.setFinalAmount(transactionObj.getFinalAmountWithVat());
				transactionDtoList.add(transDto);
			});
			System.out.println(System.currentTimeMillis());
			return transactionDtoList;

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
					childTransactionDto.setMaterialType(childtrans.getMaterial().getMaterialLookupId());
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
					childTransactionDto.setFirstUploadedFileId(childtrans.getFirstUploadedFileId());
					childTransactionDto.setSecondUploadedFileId(childtrans.getSecondUploadedFileId());
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

	public ResponseMessage uploadFile(MultipartFile file) {
	    String message = "";
	    try {
	      FileDB file1 = storageService.store(file);
	      message = "Uploaded the file successfully: " + file.getOriginalFilename();
	      return new ResponseMessage(message, file1.getId());
	    } catch (Exception e) {
	      message = "Could not upload the file: " + file.getOriginalFilename() + "!";
	      return new ResponseMessage(message, "");
	    }
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
