package com.example.WBMdemo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.WBMdemo.entity.InitialWeight;
import com.example.WBMdemo.entity.TransactionsHeader;

@Repository
public interface InitialWeightRepository extends JpaRepository<InitialWeight, Long>{

	public InitialWeight findByRawWeightId(long rawWeightId);
	@Query(value = "SELECT * FROM postgreswbm.RAW_WEIGHT th "
			+ "WHERE date_trunc('day', th.CREATED_DATE\\:\\:timestamp) = to_date(:createdDate, 'YYYY-MM-DD') ", nativeQuery = true)
	public List<InitialWeight> findByCreatedDate(LocalDate createdDate);

	public List<InitialWeight> findAllByOrderByCreatedDateDesc();

}
