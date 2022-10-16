package com.example.WBMdemo.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ChildTransactionDto {
	
	private long Id;
	private BigDecimal firstWeight;
	private BigDecimal secondWeight;
	private BigDecimal absoluteWeight;
	private long materialType;
	private String baleOrLoose;
	private BigDecimal vat;
	private BigDecimal vatCost;
	private BigDecimal materialPricewithoutVat;
	private BigDecimal materialPricewithVat;

}
