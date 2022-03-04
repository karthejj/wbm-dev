package com.example.WBMdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WBMdemo.entity.Vehicle;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
	 public Vehicle findByVehicleNumber(String vehicleNumber);
	 public void deleteByVehicleNumber(String vehicleNaumber);
}
