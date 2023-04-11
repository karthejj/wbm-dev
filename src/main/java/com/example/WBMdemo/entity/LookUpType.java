package com.example.WBMdemo.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
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
@Table(name = "LOOKUPTYPE", uniqueConstraints={@UniqueConstraint(columnNames={"CODE"})})
public class LookUpType {

	@Id
	@SequenceGenerator(name="lookuptype-seq-gen",sequenceName="LOOKUPTYPE_SEQ_GEN", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="lookuptype-seq-gen")
	@Column(name = "ID")
	private Long lookUpTypeId;
	
	@Column(name="CODE", nullable = false, length = 100)
	private String code;
	
	@Column(name = "DESCRIPTION", length = 200)
	private String description;
	
	@Enumerated(EnumType.STRING)
	@Column(name="ENABLED", length = 1, nullable = false)
	private YesNoEnum enabled = YesNoEnum.Y;
	
//	@OneToMany(mappedBy = "lookUpType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
//	private List<LookUpValue> values = new ArrayList<LookUpValue>();
}
