package com.example.WBMdemo.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import lombok.Data;

@Data
@Entity
	@Table(name = "files")
	public class FileDB {

	@Id
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	@Column(name = "id") 
	private String id;

	@Column(name = "name")
	 private String name;

	  @Column(name = "type")
	  private String type;

	  @Lob
	  @Column(name = "data")
	  private byte[] data;
	  
	  @Column(name = "created_date")
	  private LocalDateTime createdDateTime;

	  public FileDB() {
	  }

	  public FileDB(String name, String type, byte[] data, LocalDateTime createdDateTime) {
	    this.name = name;
	    this.type = type;
	    this.data = data;
	    this.createdDateTime = createdDateTime;
	  }
}
