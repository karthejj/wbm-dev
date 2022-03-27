package com.example.WBMdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.WBMdemo.entity.StatusMaster;

public interface StatusMasterRepository extends JpaRepository<StatusMaster, Integer> {

	public StatusMaster findByStatusId(Integer statusId);
}
