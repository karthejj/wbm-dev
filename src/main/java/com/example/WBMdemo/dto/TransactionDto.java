package com.example.WBMdemo.dto;

import java.math.BigDecimal;
import java.util.List;

import com.example.WBMdemo.entity.TransferType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TransactionDto {

	private long Id;
	private String customerName;
	private String customerId;
	private String vehicleNumber;
	private int customerType;
//	private long materialType;
	private int driverCount;
	private List<ChildTransactionDto> childTransactionDtoList; 
//	private BigDecimal firstWeight;
//	private BigDecimal secondWeight;
	private BigDecimal totalWeight;
//	private BigDecimal materialPrice;
	private BigDecimal vatCost;
	private BigDecimal finalAmount;
	private Boolean isTransactionCompleted = false;
	private Boolean isTransactionCancelled = false;
	private String transactionStatus;
	private String cancelReason;
	private TransferType transferType;
	private String phoneNumber;
	private Boolean includeVat;
	
	
}
