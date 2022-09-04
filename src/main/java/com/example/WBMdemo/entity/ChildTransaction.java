package com.example.WBMdemo.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CHILD_TRANSACTION")
@Data
@Builder
public class ChildTransaction {

	@Id
	@SequenceGenerator(name="child_transaction-seq-gen",sequenceName="CHILD_TRANSACTION_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="child_transaction-seq-gen")
	@Column(name = "ID")
	private Long childTransactionId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MATERIAL_ID")
	private Material material;
	
	@Column(name = "MATERIAL_BALE_LOOSE")
	private String baleOrLoose;
	
	@Column(name = "MATERIAL_FIRST_WEIGHT")
	private BigDecimal materialFirstWeight;
	
	@Column(name = "MATERIAL_SECOND_WEIGHT")
	private BigDecimal materialSecondWeight;
	
	@Column(name = "MATERIAL_WEIGHT")
	private BigDecimal materialAbsoluteWeight;
	
	@Column(name = "MATERIAL_PRICE")
	private BigDecimal materialPrice;
	
	@Column(name = "VAT")
	private BigDecimal vat;

	@Column(name = "VAT_COST")
	private BigDecimal vatCost;

	@Column(name = "MATERIAL_PRICE_AFTER_VAT")
	private BigDecimal materialPriceAfterVat;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="TRANSACTION_HEADER_ID")
	private TransactionsHeader transactionsHeader;
	
}
