package com.example.WBMdemo.services;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.entity.PriceMaster;
import com.example.WBMdemo.entity.Vehicle;
import com.example.WBMdemo.errors.VehicleNotFoundException;
import com.example.WBMdemo.repository.PriceRepository;

@Service
public class PriceServiceImpl implements PriceService {

	@Autowired
	private PriceRepository priceRepository;
	
	@Override
	public PriceMaster savePrice(PriceMaster price) {
		// TODO Auto-generated method stub
		
		PriceMaster priceDB = priceRepository.findByMaterial(price.getMaterial());
		if(Objects.isNull(priceDB)) {
			return priceRepository.save(price);			
		}
		if(Objects.nonNull(price.getMaterial())) {
			priceDB.setMaterial(price.getMaterial());
		}
		if(price.getCost()!=null) {
			priceDB.setCost(price.getCost());
		}
		return priceDB;
		
	}
	

}
