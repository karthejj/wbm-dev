package com.example.WBMdemo.entity.controllers;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.WBMdemo.entity.Vehicle;
import com.example.WBMdemo.errors.DuplicateVehicleException;
import com.example.WBMdemo.errors.VehicleNotFoundException;
import com.example.WBMdemo.services.VehicleService;


@RestController
public class VehicleController {
	
	@Autowired
	private VehicleService vehicleService;

	private final Logger LOGGER = LoggerFactory.getLogger(VehicleController.class);
	
	@PostMapping("/vehicle")
	public Vehicle saveVehicle(@Valid @RequestBody Vehicle vehicle) throws DuplicateVehicleException {
		LOGGER.debug("Inside saveVehicle method of VehicleController ");
		return vehicleService.saveVehicle(vehicle);
	}
	
	@GetMapping("/vehicle")
	public List<Vehicle> fetchVehicleList(){
		LOGGER.debug("Inside fetchVehicleList method of VehcileController ");
		return vehicleService.fetchVehicleList();
	}
	
	
	@DeleteMapping("vehicle/{number}")
	public String deleteVehicleById(@PathVariable("number")String vehicleNumber) throws VehicleNotFoundException {
		LOGGER.debug("Inside deleteVehicle method of VehicleController ");
		
		Vehicle vehicle = vehicleService.getVehicleByName(vehicleNumber);
		if(Objects.isNull(vehicle)) {
			throw new VehicleNotFoundException("Vehicle not found ");
		}
		vehicleService.deleteVehicleByNumber(vehicleNumber);
		return "Vehicle Deleted Successfully";
	}
	
	@PostMapping("updateVehicle/{number}")
	public Vehicle updateVehicle(@PathVariable("number")String vehicleNumber, 
			@Valid @RequestBody Vehicle vehicle) throws VehicleNotFoundException {
		LOGGER.debug("Inside updateVehicle method of vehicleController ");
		return vehicleService.updateVehicle(vehicleNumber, vehicle);
	}
	
	@GetMapping("vehicle/vehicleNumber/{number}")
	public Vehicle getVehicleByName(@PathVariable("number") String vehicleNumber) throws VehicleNotFoundException {
		LOGGER.debug("Inside getVehicleByName method of VehicleController ");
		return vehicleService.getVehicleByName(vehicleNumber);
	}
	
}
