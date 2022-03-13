package com.example.WBMdemo.entity;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"CUSTOMER_NAME"})})
public class Customer {

	@Id
	@SequenceGenerator(name="customer-seq-gen",sequenceName="CUSTOMER_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="customer-seq-gen")
	@Column(name = "ID")
	private Integer customerId;
	
	
	@Column(name = "CUSTOMER_NAME")
	private String customerName;
	
	
}
