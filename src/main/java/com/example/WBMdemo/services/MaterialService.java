package com.example.WBMdemo.services;

import java.util.List;

import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.errors.DuplicateMaterialException;

public interface MaterialService {

	public Material saveMaterial(Material material) throws DuplicateMaterialException;
	
	public List<Material> fetchMaterialList();
}
