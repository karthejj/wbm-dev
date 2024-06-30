package com.example.WBMdemo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum TransferType {
	
	INC("INC"), OUT("OUT"), WEIGH("WEIGH"), VEHICLE("VEHICLE");
	@JsonProperty("text")
	private final String text;

	private TransferType(final String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
}
