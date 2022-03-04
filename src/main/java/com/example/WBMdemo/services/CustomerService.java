package com.example.WBMdemo.services;

import java.util.List;

import com.example.WBMdemo.entity.Customer;
import com.example.WBMdemo.entity.Vehicle;
import com.example.WBMdemo.errors.DuplicateCustomerException;
import com.example.WBMdemo.errors.DuplicateVehicleException;

public interface CustomerService {

	public Customer saveCustomer(Customer customer) throws DuplicateCustomerException;
	
	public List<Customer> fetchCustomerList();

}
