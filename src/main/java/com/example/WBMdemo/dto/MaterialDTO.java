package com.example.WBMdemo.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MaterialDTO {

	private Long materialId;
	private String materialName;
	private BigDecimal materialPrice;
	private BigDecimal materialWeight;
}
