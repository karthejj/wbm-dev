package com.example.WBMdemo.dto;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ChildTransactionDto {
	
	@JsonProperty("Id")
	private long Id;
	@JsonProperty("firstWeight")
	private BigDecimal firstWeight;
	@JsonProperty("secondWeight")
	private BigDecimal secondWeight;
	@JsonProperty("absoluteWeight")
	private BigDecimal absoluteWeight;
	@JsonProperty("materialType")
	private long materialType;
	@JsonProperty("baleOrLoose")
	private String baleOrLoose;
	@JsonProperty("vat")
	private BigDecimal vat;
	@JsonProperty("vatCost")
	private BigDecimal vatCost;
	@JsonProperty("transactionPricewithoutVat")
	private BigDecimal transactionPricewithoutVat;
	@JsonProperty("transactionPricewithVat")
	private BigDecimal transactionPricewithVat;
	@JsonProperty("transactionPricewithoutVatRoundOff")
	private BigDecimal transactionPricewithoutVatRoundOff;
	@JsonProperty("transactionPricewithVatRoundOff")
	private BigDecimal transactionPricewithVatRoundOff;
	@JsonProperty("includeVat")
	private Boolean includeVat;
	@JsonProperty("materialPricePerTonne")
	private BigDecimal materialPricePerTonne;
//	private MultipartFile file;
	@JsonProperty("firstUploadedFileId")
	private String firstUploadedFileId;
	@JsonProperty("secondUploadedFileId")
	private String secondUploadedFileId;
	@JsonProperty("createdBy")
	private String createdBy;
	@JsonProperty("createdDate")
	private String createdDate;
	@JsonProperty("modifiedBy")
	private String modifiedBy;
	@JsonProperty("modifiedDate")
	private String modifiedDate;
}
