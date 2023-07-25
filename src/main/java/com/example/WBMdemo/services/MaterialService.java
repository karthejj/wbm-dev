package com.example.WBMdemo.services;

import java.util.List;

import com.example.WBMdemo.dto.MaterialDTO;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.errors.DuplicateMaterialException;

public interface MaterialService {

	public MaterialDTO saveMaterial(MaterialDTO material) throws DuplicateMaterialException;
	public List<MaterialDTO> fetchMaterialList(String sortParam, int order);
	public MaterialDTO getMaterial(long materialId);
	public String getActualIncBaleMaterialCost(MaterialDTO material);
	public String getActualIncLooseMaterialCost(MaterialDTO material);
	public String getActualOutBaleMaterialCost(MaterialDTO material);
	public String getActualOutLooseMaterialCost(MaterialDTO material);
}
