package com.example.WBMdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WBMdemo.entity.Transactions;

public interface TransactionRepository extends JpaRepository<Transactions, Long> {

	public Transactions findByTransactionId(long transactionId);
}
