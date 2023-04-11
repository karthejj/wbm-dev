package com.example.WBMdemo.common;

public enum YesNoEnum {
	Y("Y"), N("N");
	
	private final String text;
	
	private YesNoEnum(final String text) {
		this.text = text;
	}
	
	public String toString() {
		return text;
	}
	
}
