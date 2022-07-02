package com.example.WBMdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WBMdemo.entity.TransactionsHeader;

public interface TransactionRepository extends JpaRepository<TransactionsHeader, Long> {

	public TransactionsHeader findByTransactionId(long transactionId);
}
