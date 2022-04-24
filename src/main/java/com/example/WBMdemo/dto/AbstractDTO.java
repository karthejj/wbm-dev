package com.example.WBMdemo.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AbstractDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7842719787267203957L;
	private Map<String, String> extras = new HashMap<String, String>(); 
	
	public Map<String, String> getExtras() {
		return extras;
	}
	public void setExtras(Map<String, String> extras) {
		this.extras = extras;
	}	

}
