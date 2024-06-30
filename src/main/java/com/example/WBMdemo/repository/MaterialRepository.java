package com.example.WBMdemo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.WBMdemo.entity.Customer;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.entity.StatusMaster;
import com.example.WBMdemo.entity.TransactionsHeader;

public interface MaterialRepository extends JpaRepository<Material, Integer> {

	public Material findByMaterialName(String materialName);
	public Material findByMaterialLookupId(Long materialId);
	public List<Material> findAllByOrderByMaterialIdAsc();
	public List<Material> findAllByOrderByMaterialNameAsc();
	
	@Query(value = "SELECT max(MATERIAL_LOOKUP_ID) from postgreswbm.MATERIAL ", nativeQuery = true)
	public Long findLastAddedMaterialId();
}
