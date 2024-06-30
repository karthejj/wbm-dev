package com.example.WBMdemo.dto;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.WBMdemo.entity.TransferType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@JsonIgnoreProperties({"file"})
public class TransactionDto {

	@JsonProperty("id")
	private long id;
	@JsonProperty("customerName")
	private String customerName;
	@JsonProperty("customerId")
	private String customerId;
	@JsonProperty("vehicleNumber")
	private String vehicleNumber;
	@JsonProperty("customerType")
	private int customerType;
//	private long materialType;
	@JsonProperty("driverCount")
	private int driverCount;
	@JsonProperty("childTransactionDtoList")
	private List<ChildTransactionDto> childTransactionDtoList; 
//	private BigDecimal firstWeight;
//	private BigDecimal secondWeight;
	@JsonProperty("totalWeight")
	private BigDecimal totalWeight;
//	private BigDecimal materialPrice;
	@JsonProperty("vatCost")
	private BigDecimal vatCost;
	@JsonProperty("finalAmount")
	private BigDecimal finalAmount;
	@JsonProperty("isTransactionCompleted")
	private Boolean isTransactionCompleted = false;
	@JsonProperty("isTransactionCancelled")
	private Boolean isTransactionCancelled = false;
	@JsonProperty("transactionStatus")
	private String transactionStatus;
	@JsonProperty("cancelReason")
	private String cancelReason;
	@JsonProperty("transferType")
	private TransferType transferType;
	@JsonProperty("phoneNumber")
	private String phoneNumber;
	@JsonProperty("includeVat")
	private Boolean includeVat;
	@JsonProperty("created_by")
	private String created_by;
	@JsonProperty("closed_by")
	private String closed_by;
	@JsonProperty("created_date")
	private String created_date;
	@JsonProperty("closed_date")
	private String closed_date;
	@JsonProperty("rawWeightId")
	private String rawWeightId;
//	private String created_date1 = format_date(created_date);
//	private String closed_date1 = format_date(closed_date);
	@JsonProperty("vehicleType")
	private String vehicleType;
	@JsonProperty("file")
	private MultipartFile file;
	@JsonProperty("finalAmountRoundOff")
	private BigDecimal finalAmountRoundOff;
	@JsonProperty("exceptionMessage")
	private String exceptionMessage;
	
}
