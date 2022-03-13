package com.example.WBMdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WBMdemo.entity.Customer;
import com.example.WBMdemo.entity.Material;

public interface MaterialRepository extends JpaRepository<Material, Integer> {

	public Material findByMaterialName(String materialName);
	public Material findByMaterialId(int materialId);
}
