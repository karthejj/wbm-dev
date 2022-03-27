package com.example.WBMdemo.dto;

import java.math.BigDecimal;

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
	private int materialType;
	private int driverCount;
	private BigDecimal firstWeight;
	private BigDecimal secondWeight;
	private BigDecimal totalWeight;
	private BigDecimal materialPrice;
	private BigDecimal vat;
	private BigDecimal finalAmount;
	private Boolean isTransactionCompleted = false;
	private Boolean isTransactionCancelled = false;
	private int transactionStatus;
	
	
}
