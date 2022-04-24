package com.example.WBMdemo.services;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.dto.MaterialDTO;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.errors.DuplicateMaterialException;
import com.example.WBMdemo.repository.MaterialRepository;

@Service
public class MaterialServiceImpl implements MaterialService {

	@Autowired
	private MaterialRepository materialRepository;
	
	@Override
	public Material saveMaterial(Material material) throws DuplicateMaterialException {
		// TODO Auto-generated method stub
		Material materialDB = materialRepository.findByMaterialId(material.getMaterialId());
		if(Objects.nonNull(materialDB)) {
			materialDB.setMaterialPrice(material.getMaterialPrice());
			return materialRepository.save(materialDB);
		} else {
			return materialRepository.save(material);
		}
	}

	@Override
	public List<Material> fetchMaterialList(String sortParam, int order) {
		// TODO Auto-generated method stub
		List<Material> materialList = null;
		if(order==1) {
			materialList = 
					materialRepository.findAll(Sort.by(Sort.Direction.ASC, sortParam));
		} else {
			materialList = 
					materialRepository.findAll(Sort.by(Sort.Direction.DESC, sortParam));
		}
		return materialList;
	}

	@Override
	public Material getMaterial(long materialId) {
		// TODO Auto-generated method stub
		return materialRepository.findByMaterialId(materialId);
	}

	@Override
	public String getActualMaterialCost(MaterialDTO material) {
		// TODO Auto-generated method stub
		return materialRepository.findByMaterialId(material.getMaterialId())
				.getMaterialPrice().multiply(material.getMaterialWeight()).toString();
	}

}
