package com.example.WBMdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WBMdemo.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
	public Customer findByCustomerName(String customerName);

}
