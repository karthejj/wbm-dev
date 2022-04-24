package com.example.WBMdemo.services;

import java.util.List;

import com.example.WBMdemo.dto.MaterialDTO;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.errors.DuplicateMaterialException;

public interface MaterialService {

	public Material saveMaterial(Material material) throws DuplicateMaterialException;
	public List<Material> fetchMaterialList(String sortParam, int order);
	public Material getMaterial(long materialId);
	public String getActualMaterialCost(MaterialDTO material);
}
