package com.example.WBMdemo.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.example.WBMdemo.controllers.MaterialController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.dto.MaterialDTO;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.errors.DuplicateMaterialException;
import com.example.WBMdemo.repository.MaterialRepository;

@Service
public class MaterialServiceImpl implements MaterialService {

	private final Logger LOGGER = LoggerFactory.getLogger(MaterialController.class);

	@Autowired
	private MaterialRepository materialRepository;
	
	@Override
	public MaterialDTO saveMaterial(MaterialDTO material) throws DuplicateMaterialException {
		// TODO Auto-generated method stub

		MaterialDTO material_dto; 
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
			materialRepository.save(materialDB);
			material_dto = new MaterialDTO(materialDB.getMaterialId(), 
					materialDB.getMaterialName(), materialDB.getVat(), materialDB.getMaterialIncBalePrice(), 
					 materialDB.getMaterialIncLoosePrice(),  
					 materialDB.getMaterialOutBalePrice(), 
					 materialDB.getMaterialOutLoosePrice(),
					 Objects.nonNull(materialDB.getCreatedBy()) ? materialDB.getCreatedBy().toString() : null,
						Objects.nonNull(materialDB.getCreatedDateTime()) ? 
								format_date(materialDB.getCreatedDateTime()) : null);
			return material_dto;
		} else {
			Material materialNew = new Material();
//			Material materialDuplicate = materialRepository.findByMaterialName(material.getMaterialName());
			materialNew.setMaterialName(material.getMaterialName());
			System.out.println(material.getMaterialIncBalePrice());


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
			
			materialRepository.save(materialNew);
			material_dto = new MaterialDTO(materialNew.getMaterialId(), 
					materialNew.getMaterialName(), materialNew.getVat(), materialNew.getMaterialIncBalePrice(), 
					materialNew.getMaterialIncLoosePrice(),  
					materialNew.getMaterialOutBalePrice(), 
					materialNew.getMaterialOutLoosePrice(),
					Objects.nonNull(materialNew.getCreatedBy()) ? materialNew.getCreatedBy().toString() : null,
					Objects.nonNull(materialNew.getCreatedDateTime()) ? 
							format_date(materialNew.getCreatedDateTime()) : null);
			
			return material_dto;
		}
	}

	@SuppressWarnings("null")
	@Override
	public List<MaterialDTO> fetchMaterialList(String sortParam, int order) {
		// TODO Auto-generated method stub
		List<Material> materialList = null;
		List<MaterialDTO> materialDtoList = new ArrayList<MaterialDTO>();
		
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
		for(Material materialNew : materialList) {
			materialDtoList.add(new MaterialDTO(materialNew.getMaterialId(), 
					materialNew.getMaterialName(), materialNew.getVat(), materialNew.getMaterialIncBalePrice(), 
					materialNew.getMaterialIncLoosePrice(),  
					materialNew.getMaterialOutBalePrice(), 
					materialNew.getMaterialOutLoosePrice(),
					Objects.nonNull(materialNew.getCreatedBy()) ? materialNew.getCreatedBy().toString() : null,
					Objects.nonNull(materialNew.getCreatedDateTime()) ? 
							format_date(materialNew.getCreatedDateTime()) : null));
		}
		return materialDtoList;
	}

	@Override
	public MaterialDTO getMaterial(long materialId) {
		// TODO Auto-generated method stub
		Material materialNew = materialRepository.findByMaterialId(materialId);
		return new MaterialDTO(materialNew.getMaterialId(), 
				materialNew.getMaterialName(), materialNew.getVat(), materialNew.getMaterialIncBalePrice(), 
				materialNew.getMaterialIncLoosePrice(),  
				materialNew.getMaterialOutBalePrice(), 
				materialNew.getMaterialOutLoosePrice(),
				Objects.nonNull(materialNew.getCreatedBy()) ? materialNew.getCreatedBy().toString() : null,
				Objects.nonNull(materialNew.getCreatedDateTime()) ? 
						format_date(materialNew.getCreatedDateTime()) : null);
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
	
	private String format_date(LocalDateTime time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d=new Date();
		try {
			d = sdf.parse(time.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String formattedTime = output.format(d);
		return formattedTime;
	}
	
	
}
