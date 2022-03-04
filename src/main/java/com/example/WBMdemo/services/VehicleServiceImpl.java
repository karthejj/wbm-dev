package com.example.WBMdemo.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.aop.ThrowsAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.WBMdemo.entity.Vehicle;
import com.example.WBMdemo.errors.DuplicateVehicleException;
import com.example.WBMdemo.errors.VehicleNotFoundException;
import com.example.WBMdemo.repository.VehicleRepository;

@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

	@Autowired
	private VehicleRepository vehicleRepository;
	
	@Override
	public Vehicle saveVehicle(Vehicle vehicle) throws DuplicateVehicleException {
		// TODO Auto-generated method stub
		Vehicle vehicleDB = vehicleRepository.findByVehicleNumber(vehicle.getVehicleNumber());
		if(Objects.nonNull(vehicleDB)) {
			throw new DuplicateVehicleException("Vehicle "+vehicle.getVehicleNumber()+" already exists ");
		}
		return vehicleRepository.save(vehicle);
	}

	@Override
	public List<Vehicle> fetchVehicleList() {
		// TODO Auto-generated method stub
		return vehicleRepository.findAll();
	}


	@Override
	public void deleteVehicleByNumber(String vehicleNumber) {
		// TODO Auto-generated method stub
		vehicleRepository.deleteByVehicleNumber(vehicleNumber);
		
	}

	@Override
	public Vehicle updateVehicle(String vehicleNumber, Vehicle vehicle) throws VehicleNotFoundException {
		// TODO Auto-generated method stub
		Vehicle vehicleDB = vehicleRepository.findByVehicleNumber(vehicleNumber);
		if(Objects.isNull(vehicleDB)) {
			throw new VehicleNotFoundException("Vehicle Not Found ");
		}
		if(Objects.nonNull(vehicle) && !"".equalsIgnoreCase(vehicle.getVehicleNumber())) {
			vehicleDB.setVehicleNumber(vehicle.getVehicleNumber());
		}
		if(Objects.nonNull(vehicle) && (vehicle.getVehicleWeight()!=null)) {
			vehicleDB.setVehicleWeight(vehicle.getVehicleWeight());
		}
		if(Objects.nonNull(vehicle) && (vehicle.getDriverCount()!=0)) {
			vehicleDB.setVehicleNumber(vehicle.getVehicleNumber());
		}
		return vehicleRepository.save(vehicleDB);
		
	}

	@Override
	public Vehicle getVehicleByName(String vehicleNumber) throws VehicleNotFoundException {
		// TODO Auto-generated method stub
		Vehicle vehicleDB =  vehicleRepository.findByVehicleNumber(vehicleNumber);
		if(Objects.isNull(vehicleDB)) {
			throw new VehicleNotFoundException("Vehicle Not Found ");
		}
		return vehicleDB;
	}
}
