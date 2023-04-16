package com.example.WBMdemo.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "RAW_WEIGHT")
@Data
@Builder
public class InitialWeight {
	
	@Id
	@SequenceGenerator(name="rawweight-seq-gen",sequenceName="RAWWEIGHT_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="rawweight-seq-gen")
	@Column(name = "ID")
	private Long rawWeightId;
	
	@Column(name = "VEHICLE_WEIGHT")
	private String vehicleWeight;
	
	@Column(name = "CREATED_DATE")
	private LocalDateTime createdDate;

//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "TRANSACTION_ID")
//	private TransactionsHeader transactionId;
	

}
