package com.example.WBMdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WBMdemo.entity.ChildTransaction;

public interface ChildTransactionRepository extends JpaRepository<ChildTransaction, Long> {

}
