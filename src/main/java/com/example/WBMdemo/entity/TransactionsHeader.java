package com.example.WBMdemo.entity;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TRANSACTIONS_HEADER")
@Data
@Builder
public class TransactionsHeader {
	
	@Id
	@SequenceGenerator(name="transactions-seq-gen",sequenceName="TRANSACTIONS_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="transactions-seq-gen")
	@Column(name = "ID")
	private Long transactionId;
	
	@Column(name = "CUSTOMER_NAME")
	private String customerName;
	
	@Column(name = "CUSTOMER_ID")
	private String customerId;
	
	@Column(name = "VEHICLE_NUMBER")
	private String vehicleNumber;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CUSTOMER_TYPE_ID")
	private Customer customer;
	
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "MATERIAL_ID")
//	private Material material;
	
	@Column(name = "DRIVER_COUNT")
	private Integer driverCount;

//	@Column(name = "FIRST_WEIGHT")
//	private BigDecimal firstWeight;
//	
//	@Column(name = "SECOND_WEIGHT")
//	private BigDecimal secondWeight;
	
	@OneToMany(fetch = FetchType.LAZY)
	private List<ChildTransaction> transactionDetials = new ArrayList<ChildTransaction>();

	@Column(name = "TOTAL_WEIGHT")
	private BigDecimal totalWeight;
	
	@Column(name = "MATERIAL_PRICE")
	private BigDecimal finalAmountWithoutVat;
	
	@Column(name = "VAT")
	private BigDecimal vatCost;
	
	@Column(name = "FINAL_AMOUNT")
	private BigDecimal finalAmountWithVat;
	
	@Column(name = "TRANSACTION_COMPLETED")
	private Boolean transactionCompleted;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "STATUS_ID")
	private StatusMaster status;
	
	@Column(name = "CANCEL_REASON")
	private String cancelReason;
	
	@Column(name = "TRANSFER_TYPE")
	private String transfer_type;
	
//	@Column(name = "BALE_LOOSE")
//	private String baleOrLoose;
	
//	@Column(name="CREATED_DATE")
//	private LocalDate createdDate;
//	
//	@Column(name="MODIFIED_DATE")
//	private LocalDate modifiedDate;
	
	@Column(name = "PHONE_NUMBER")
	private String phoneNumber;
	
	@Column(name = "VAT_INCLUDED")
	private Boolean vatIncluded=true;
	
	@Column(name = "created_by", length = 50)
	private String createdBy;

	@Column(name = "CREATED_DATE")
	private LocalDateTime createdDate;
	
	@Column(name = "modified_by", length = 50)
	private String modifiedBy;
	
	@Column(name = "MODIFIED_DATE")
	private LocalDateTime modifiedDate;
	
	@Column(name = "closed_by", length = 50)
	private String closedBy;
	
	@Column(name = "closed_DATE")
	private LocalDateTime closedDate;
	
	@Column(name = "RAW_WEIGHT_ID")
	private long rawWeightId;
	
	@Column(name = "VEHICLE_TYPE")
	private String vehicle_type;
	
	@Column(name = "ADDITIONAL_COMMENTS")
	private String additional_comments;
	
	
}
