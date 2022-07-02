package com.example.WBMdemo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PRICE_MASTER")
@Data
@Builder
public class PriceMaster {
	
	@Id
	@SequenceGenerator(name="price-seq-gen",sequenceName="price_seq_gen", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="price-seq-gen")
	@Column(name = "PRICE_ID")
	private Long priceId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MATERIAL_ID")
	private Material material;
	
	@Column(name = "CREATED_MODIFIED_DATE", nullable = true)
	private LocalDateTime createdModifiedDate;
	
	private BigDecimal cost;
}
