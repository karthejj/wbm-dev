package com.example.WBMdemo.dto;

import java.math.BigDecimal;

import lombok.Data;
	
@Data
public class SummaryDTO {

	private String summaryType;
	private String materialName;
	private String createdBy;
	private String modifiedBy;
	private Integer noOfTransaction;
	private BigDecimal quantity;
	private BigDecimal materialPrice;
	private String startDate;
	private String endDate;
	private String transactionId;
	
}
