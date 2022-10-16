package com.example.WBMdemo.entity;

public enum TransferType {
	
	INC("INC"), OUT("OUT"), VEHICLE("VEHICLE");
	
	private final String text;

	private TransferType(final String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
}
