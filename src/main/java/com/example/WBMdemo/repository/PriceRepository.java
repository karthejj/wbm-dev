package com.example.WBMdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.entity.PriceMaster;

public interface PriceRepository extends JpaRepository<PriceMaster, Long> {
	
	public PriceMaster findByMaterial(Material material);

}
