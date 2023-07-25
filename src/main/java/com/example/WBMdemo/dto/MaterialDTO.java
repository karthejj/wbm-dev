package com.example.WBMdemo.dto;

import java.math.BigDecimal;

import com.example.WBMdemo.entity.MaterialPrice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDTO {

	private Long materialId;
	private String materialName;
//	private MaterialPriceDto materialINC;
//	private MaterialPriceDto materialOUT;
	private BigDecimal materialWeight;
	private BigDecimal vat;
	private BigDecimal materialIncBalePrice;
	private BigDecimal materialIncLoosePrice;
	private BigDecimal materialOutBalePrice;
	private BigDecimal materialOutLoosePrice;
	private String created_by;
	private String created_date;
	
	public MaterialDTO(Long materialId,  String materialName, BigDecimal vat, BigDecimal materialIncBalePrice,
			BigDecimal materialIncLoosePrice, BigDecimal materialOutBalePrice, BigDecimal materialOutLoosePrice,
			String created_by, String created_date) {
		this.materialId = materialId;
		this.materialName = materialName;
		this.vat = vat;
		this.materialIncBalePrice = materialIncBalePrice;
		this.materialIncLoosePrice = materialIncLoosePrice;
		this.materialOutBalePrice = materialOutBalePrice;
		this.materialOutLoosePrice = materialOutLoosePrice;
		this.created_by = created_by;
		this.created_date = created_date;
	}
	
}
