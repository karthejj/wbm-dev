package com.example.WBMdemo.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Data
@Builder
public class StatusMaster {
	
	@Id
	@SequenceGenerator(name="status-seq-gen",sequenceName="status_seq_gen", initialValue=1, allocationSize=1)
	@GeneratedValue(strategy = GenerationType.AUTO, generator="status-seq-gen")
	@Column(name = "STATUS_ID")
	private Integer statusId;

	@Column(name = "STATUS_NAME")
	private String statusName;
}
