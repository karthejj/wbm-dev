package com.example.WBMdemo.services;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.entity.Customer;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.errors.DuplicateCustomerException;
import com.example.WBMdemo.errors.DuplicateMaterialException;
import com.example.WBMdemo.repository.CustomerRepository;
import com.example.WBMdemo.repository.MaterialRepository;

@Service
public class MaterialServiceImpl implements MaterialService {

	@Autowired
	private MaterialRepository materialRepository;
	
	@Override
	public Material saveMaterial(Material material) throws DuplicateMaterialException {
		// TODO Auto-generated method stub
		Material materialDB = materialRepository.findByMaterialName(material.getMaterialName());
		if(Objects.nonNull(materialDB)) {
			materialDB.setMaterialPrice(material.getMaterialPrice());
			materialDB.setMaterialName(material.getMaterialName());
		}
		return materialRepository.save(materialDB);
	}

	@Override
	public List<Material> fetchMaterialList() {
		// TODO Auto-generated method stub
		return materialRepository.findAll();
	}

}
