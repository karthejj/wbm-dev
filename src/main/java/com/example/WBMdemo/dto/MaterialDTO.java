package com.example.WBMdemo.dto;

import java.math.BigDecimal;

import com.example.WBMdemo.entity.MaterialPrice;

import lombok.Data;

@Data
public class MaterialDTO {

	private Long materialId;
	private String materialName;
	private MaterialPriceDto materialINC;
	private MaterialPriceDto materialOUT;
	private BigDecimal materialWeight;
}
