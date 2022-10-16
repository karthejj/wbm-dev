package com.example.WBMdemo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WBMdemo.entity.ChildTransaction;
import com.example.WBMdemo.entity.TransactionsHeader;

public interface ChildTransactionRepository extends JpaRepository<ChildTransaction, Long> {

	public List<ChildTransaction> findByTransactionsHeader(TransactionsHeader headerId);
}
