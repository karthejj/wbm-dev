package com.example.WBMdemo.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Table(name = "MATERIAL", uniqueConstraints={@UniqueConstraint(columnNames={"MATERIAL_NAME"})})
public class Material {

	@Id
	@SequenceGenerator(name="material-seq-gen",sequenceName="MATERIAL_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="material-seq-gen")
	@Column(name = "ID")
	private Long materialId;
	
	@Column(name = "MATERIAL_NAME")
	private String materialName;
	
	@Column(name = "MATERIAL_INC_BALE_PRICE")
	private BigDecimal materialIncBalePrice;
	
	@Column(name = "MATERIAL_INC_LOOSE_PRICE")
	private BigDecimal materialIncLoosePrice;
	
	@Column(name = "MATERIAL_OUT_BALE_PRICE")
	private BigDecimal materialOutBalePrice;
	
	@Column(name = "MATERIAL_OUT_LOOSE_PRICE")
	private BigDecimal materialOutLoosePrice;
	
	@Column(name = "VAT")
	private BigDecimal vat;
	
	@Column(name = "created_by", length = 50)
	private Long createdBy;

	@Column(name = "created_date")
	private LocalDateTime createdDateTime = LocalDateTime.now();
	
	@Column(name = "modified_by", length = 50)
	private Long modifiedBy;
	
	@Column(name = "modified_date")
	private LocalDateTime modifiedDateTime = LocalDateTime.now();
	
}
