package com.example.WBMdemo.services;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public interface SummaryService {
	public XSSFWorkbook fetchSummaryList(String fromDate, String toDate);
}
