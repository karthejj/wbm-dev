package com.example.WBMdemo.entity.controllers;

import java.util.List;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
	
	@GetMapping("/material/materialList")
	public List<Material> fetchMaterialList(){
		LOGGER.debug("Inside fetchMaterialList method of MaterialController ");
		return materialService.fetchMaterialList();
	}
}
