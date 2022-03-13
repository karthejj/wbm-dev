package com.example.WBMdemo.dto;

public class CodeAndDescriptionDto {


	private int id;
	private String code;
	private String description;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString() {
		return "CodeAndDescriptionDto [id=" + id + ", code=" + code + ", description=" + description + "]";
	}
	public CodeAndDescriptionDto(int id, String code, String description) {
		super();
		this.id = id;
		this.code = code;
		this.description = description;
	}
	public CodeAndDescriptionDto() {
		super();
		// TODO Auto-generated constructor stub
	}
	public CodeAndDescriptionDto(int id) {
		super();
		this.id = id;
	}
	
	
}
