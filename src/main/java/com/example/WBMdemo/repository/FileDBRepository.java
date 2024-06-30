package com.example.WBMdemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.WBMdemo.entity.FileDB;

@Repository
public interface FileDBRepository extends JpaRepository<FileDB, String> {

}