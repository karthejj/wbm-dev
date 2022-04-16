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

import com.example.WBMdemo.entity.Customer;
import com.example.WBMdemo.errors.DuplicateCustomerException;
import com.example.WBMdemo.services.CustomerService;

@RestController
public class CustomerController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);
	
	@Autowired
	private CustomerService customerService;

	@PostMapping("/customer/savecustomer")
	public Customer saveCustomer(@Valid @RequestBody Customer customer) throws DuplicateCustomerException {
		LOGGER.debug("Inside saveCustomer method of CustomerController ");
		return customerService.saveCustomer(customer);
	}
	
	@GetMapping("/customer/customerList")
	public List<Customer> fetchCustomerList(){
		LOGGER.debug("Inside fetchCustomerList method of CustomerController ");
		return customerService.fetchCustomerList();
	}
}
