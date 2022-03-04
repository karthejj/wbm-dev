package com.example.WBMdemo.services;

import java.util.List;

import com.example.WBMdemo.entity.Vehicle;
import com.example.WBMdemo.errors.DuplicateVehicleException;
import com.example.WBMdemo.errors.VehicleNotFoundException;

public interface VehicleService {

	public Vehicle saveVehicle(Vehicle vehicle) throws DuplicateVehicleException;

	public List<Vehicle> fetchVehicleList();

//	public Vehicle getVehicleById(Long vehicleId);// throws DepartmentNotFoundException;

	public void deleteVehicleByNumber(String vehicleNumber);

	public Vehicle updateVehicle(String vehicleNumber, Vehicle vehicle) throws VehicleNotFoundException;

	public Vehicle getVehicleByName(String vehicleName) throws VehicleNotFoundException;
}
