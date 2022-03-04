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
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"vehicleNumber"})})
public class Vehicle {

	@Id
	@SequenceGenerator(name="vehicle-seq-gen",sequenceName="MY_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="vehicle-seq-gen")
	@Column(name = "ID")
	private Long vehicleId;

	@Column(name = "vehicleNumber")
	private String vehicleNumber;
	
	@Column(name = "vehicleWeight")
	private BigDecimal vehicleWeight;
	
	@Column(name = "driverCount")
	private int driverCount;
}
