package com.example.WBMdemo.entity;

import java.math.BigDecimal;

import javax.annotation.Generated;
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
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"MATERIAL_NAME"})})
public class Material {

	@Id
	@SequenceGenerator(name="material-seq-gen",sequenceName="MATERIAL_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="material-seq-gen")
	@Column(name = "ID")
	private Integer materialId;
	
	@Column(name = "MATERIAL_NAME")
	private String materialName;
	
	@Column(name = "MATERIAL_PRICE")
	private Long materialPrice;
	
}
