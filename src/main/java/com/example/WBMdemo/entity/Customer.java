package com.example.WBMdemo.entity;

import java.time.LocalDateTime;

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
@Table(name = "CUSTOMER", uniqueConstraints={@UniqueConstraint(columnNames={"CUSTOMER_NAME"})})
public class Customer {

	@Id
	@SequenceGenerator(name="customer-seq-gen",sequenceName="CUSTOMER_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="customer-seq-gen")
	@Column(name = "ID")
	private Integer customerId;
	
	
	@Column(name = "CUSTOMER_NAME")
	private String customerName;
	
	@Column(name = "created_by", length = 50)
	private Long createdBy;

	@Column(name = "created_date")
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@Column(name = "modified_by", length = 50)
	private Long modifiedBy;
	
	@Column(name = "modified_date")
	private LocalDateTime modifiedDateTime = LocalDateTime.now();
	
	
}
