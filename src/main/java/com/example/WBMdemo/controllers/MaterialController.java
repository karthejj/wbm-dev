package com.example.WBMdemo.controllers;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.WBMdemo.dto.MaterialDTO;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.errors.DuplicateMaterialException;
import com.example.WBMdemo.services.MaterialService;

@RestController
public class MaterialController {

	private final Logger LOGGER = LoggerFactory.getLogger(MaterialController.class);
	
	@Autowired
	private MaterialService materialService;

	@PostMapping("/material/savematerial")
	public Material saveMaterial(@Valid @RequestBody Material material) throws DuplicateMaterialException {
		LOGGER.debug("Inside saveMarterial method of MaterialController ");
		return materialService.saveMaterial(material);
	}
	
	@GetMapping("/material/materialList/{sortParam}/{order}")
	public List<Material> fetchMaterialList(@PathVariable("sortParam")String sortParam, 
			@PathVariable("order")int order){
		LOGGER.debug("Inside fetchMaterialList method of MaterialController ");
		return materialService.fetchMaterialList(sortParam, order);
	}
	
	@GetMapping("/material/getMaterial/{materialId}")
	public Material getMaterial(@PathVariable("materialId")long materialId) {
		LOGGER.debug("Inside getMaterial method of MaterialController ");
		return materialService.getMaterial(materialId);
	}

	@PostMapping("/material/getActualMaterialCost")
	public String getActualMaterialCost(@Valid @RequestBody MaterialDTO material) {
		LOGGER.debug("Inside getActualMaterialCost method of MaterialController ");
		return materialService.getActualMaterialCost(material);
	}
	
}
