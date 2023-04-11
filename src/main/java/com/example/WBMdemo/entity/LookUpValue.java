package com.example.WBMdemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.example.WBMdemo.common.YesNoEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
@Table(name = "LOOKUPVALUE", uniqueConstraints={@UniqueConstraint(columnNames={"CODE","LOOKUP_TYPE_ID"})})
public class LookUpValue {

	@Id
	@SequenceGenerator(name="lookupvalue-seq-gen",sequenceName="LOOKUPVALUE_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="lookupvalue-seq-gen")
	@Column(name = "ID")
	private Long lookUpValueId;
	
	@ManyToOne
	@JoinColumn(name = "LOOKUP_TYPE_ID", nullable = false)
	private LookUpType lookuptype;
	
	@Column(name="CODE", nullable = false, length = 100)
	private String code;
	
	@Column(name = "DESCRIPTION", length = 200)
	private String description;
	
	@Column(name= "VALUE_ORDER")
	private Integer valueOrder;
	
	@Enumerated(EnumType.STRING)
	@Column(name="ENABLED", length = 1, nullable = false)
	private YesNoEnum enabled = YesNoEnum.Y;
	
	
}
