package com.example.WBMdemo.services;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.entity.Customer;
import com.example.WBMdemo.entity.Vehicle;
import com.example.WBMdemo.errors.DuplicateCustomerException;
import com.example.WBMdemo.errors.DuplicateVehicleException;
import com.example.WBMdemo.repository.CustomerRepository;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private CustomerRepository customerRepository;
	
	@Override
	public Customer saveCustomer(Customer customer) throws DuplicateCustomerException {
		// TODO Auto-generated method stub
		Customer customerDB = customerRepository.findByCustomerName(customer.getCustomerName());
		if(Objects.nonNull(customerDB)) {
			throw new DuplicateCustomerException("Customer "+customer.getCustomerName()+" already exists ");
		}
		return customerRepository.save(customer);
	}

	@Override
	public List<Customer> fetchCustomerList() {
		// TODO Auto-generated method stub
		return customerRepository.findAll();
	}
	
	

}
