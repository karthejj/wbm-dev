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
	public Material saveMaterial(@Valid @RequestBody MaterialDTO material) throws DuplicateMaterialException {
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

	@PostMapping("/material/getActualIncBaleMaterialCost")
	public String getActualIncBaleMaterialCost(@Valid @RequestBody MaterialDTO material) {
		LOGGER.debug("Inside getActualIncBaleMaterialCost method of MaterialController ");
		return materialService.getActualIncBaleMaterialCost(material);
	}
	
	@PostMapping("/material/getActualIncLooseMaterialCost")
	public String getActualIncLooseMaterialCost(@Valid @RequestBody MaterialDTO material) {
		LOGGER.debug("Inside getActualIncLooseMaterialCost method of MaterialController ");
		return materialService.getActualIncLooseMaterialCost(material);
	}

	@PostMapping("/material/getActualOutBaleMaterialCost")
	public String getActualOutBaleMaterialCost(@Valid @RequestBody MaterialDTO material) {
		LOGGER.debug("Inside getActualOutBaleMaterialCost method of MaterialController ");
		return materialService.getActualOutBaleMaterialCost(material);
	}
	
	@PostMapping("/material/getActualOutLooseMaterialCost")
	public String getActualOutLooseMaterialCost(@Valid @RequestBody MaterialDTO material) {
		LOGGER.debug("Inside getActualOutLooseMaterialCost method of MaterialController ");
		return materialService.getActualOutLooseMaterialCost(material);
	}
}
