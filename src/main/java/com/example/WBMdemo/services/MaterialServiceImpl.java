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
	public Material saveMaterial(MaterialDTO material) throws DuplicateMaterialException {
		// TODO Auto-generated method stub
		Material materialDB = null;
		if(material.getMaterialId()!=null) {
			materialDB = materialRepository.findByMaterialId(material.getMaterialId());
		}
		if(Objects.nonNull(materialDB)) {
			materialDB.setMaterialName(material.getMaterialName());
			if(Objects.nonNull(material.getMaterialIncBalePrice())) {
				materialDB.setMaterialIncBalePrice(material.getMaterialIncBalePrice());
			}
			if(Objects.nonNull(material.getMaterialIncLoosePrice())) {
				materialDB.setMaterialIncLoosePrice(material.getMaterialIncLoosePrice());
			}
			if(Objects.nonNull(material.getMaterialOutBalePrice())) {
				materialDB.setMaterialOutBalePrice(material.getMaterialOutBalePrice());
			}
			if(Objects.nonNull(material.getMaterialOutLoosePrice())) {
				materialDB.setMaterialOutLoosePrice(material.getMaterialOutLoosePrice());
			}

//			if(Objects.nonNull(material.getMaterialINC())) {
//				materialDB.setMaterialIncBalePrice(material.getMaterialINC().getBale());
//				materialDB.setMaterialIncLoosePrice(material.getMaterialINC().getLoose());
//			}
//			if(Objects.nonNull(material.getMaterialOUT())) {
//				materialDB.setMaterialOutBalePrice(material.getMaterialOUT().getBale());
//				materialDB.setMaterialOutLoosePrice(material.getMaterialOUT().getLoose());
//			}
			materialDB.setVat(material.getVat());
			return materialRepository.save(materialDB);
		} else {
			Material materialNew = new Material();
//			Material materialDuplicate = materialRepository.findByMaterialName(material.getMaterialName());
			materialNew.setMaterialName(material.getMaterialName());
			if(Objects.nonNull(material.getMaterialIncBalePrice())) {
				materialNew.setMaterialIncBalePrice(material.getMaterialIncBalePrice());
			}
			if(Objects.nonNull(material.getMaterialIncLoosePrice())) {
				materialNew.setMaterialIncLoosePrice(material.getMaterialIncLoosePrice());
			}
			if(Objects.nonNull(material.getMaterialOutBalePrice())) {
				materialNew.setMaterialOutBalePrice(material.getMaterialOutBalePrice());
			}
			if(Objects.nonNull(material.getMaterialOutLoosePrice())) {
				materialNew.setMaterialOutLoosePrice(material.getMaterialOutLoosePrice());
			}
			materialNew.setVat(material.getVat());
			return materialRepository.save(materialNew);
		}
	}

	@Override
	public List<Material> fetchMaterialList(String sortParam, int order) {
		// TODO Auto-generated method stub
		List<Material> materialList = null;
		if(sortParam!=null && order!=0) {
			if(order==1) {
				materialList = 
						materialRepository.findAll(Sort.by(Sort.Direction.ASC, sortParam));
			} else {
				materialList = 
						materialRepository.findAll(Sort.by(Sort.Direction.DESC, sortParam));
			}
		} else {
			materialList = 
					materialRepository.findAll(Sort.by(Sort.Direction.ASC));
		}
		return materialList;
	}

	@Override
	public Material getMaterial(long materialId) {
		// TODO Auto-generated method stub
		return materialRepository.findByMaterialId(materialId);
	}

	@Override
	public String getActualIncBaleMaterialCost(MaterialDTO material) {
		// TODO Auto-generated method stub
		return materialRepository.findByMaterialId(material.getMaterialId())
				.getMaterialIncBalePrice().multiply(material.getMaterialWeight()).toString();
	}

	@Override
	public String getActualIncLooseMaterialCost(MaterialDTO material) {
		// TODO Auto-generated method stub
		return materialRepository.findByMaterialId(material.getMaterialId())
				.getMaterialIncLoosePrice().multiply(material.getMaterialWeight()).toString();
	}

	@Override
	public String getActualOutBaleMaterialCost(MaterialDTO material) {
		// TODO Auto-generated method stub
		return materialRepository.findByMaterialId(material.getMaterialId())
				.getMaterialOutBalePrice().multiply(material.getMaterialWeight()).toString();
	}

	@Override
	public String getActualOutLooseMaterialCost(MaterialDTO material) {
		// TODO Auto-generated method stub
		return materialRepository.findByMaterialId(material.getMaterialId())
				.getMaterialOutLoosePrice().multiply(material.getMaterialWeight()).toString();
	}
	
	
}
