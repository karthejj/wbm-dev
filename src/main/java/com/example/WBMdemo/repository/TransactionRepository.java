package com.example.WBMdemo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.WBMdemo.entity.StatusMaster;
import com.example.WBMdemo.entity.TransactionsHeader;

public interface TransactionRepository extends JpaRepository<TransactionsHeader, Long> {

	public TransactionsHeader findByTransactionId(long transactionId);

	@Query(value = " from TransactionsHeader th "
			+ "where DATE(th.createdDate) =:createdDate ", nativeQuery = true)  
	public List<TransactionsHeader> findByCreatedDate(@Param("createdDate") LocalDate createdDate);
	
	@Query(value = " from TransactionsHeader th "
			+ "where th.status = :status " 
			+" and DATE(th.createdDate) =:createdDate ", nativeQuery = true)  
	public List<TransactionsHeader> findByStatus(@Param("status") StatusMaster status, 
			@Param("createdDate") LocalDate createdDate);
	
	
	
}
