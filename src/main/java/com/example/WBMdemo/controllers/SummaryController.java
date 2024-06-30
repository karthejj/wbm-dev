package com.example.WBMdemo.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.WBMdemo.dto.SummaryDTO;
import com.example.WBMdemo.dto.TransactionDto;
import com.example.WBMdemo.services.SummaryService;

@RestController
public class SummaryController {
	private final Logger LOGGER = LoggerFactory.getLogger(SummaryController.class);
	
	@Autowired
	private SummaryService summaryService;
	
	@PostMapping("/summary/downloadSummaryReport")
    public ResponseEntity<ByteArrayResource> downloadSummaryReportExcel(
    		@RequestBody SummaryDTO dto) throws IOException {
//    		@RequestParam String param1, @RequestParam String param2) throws IOException {
		LOGGER.debug("Inside downloadSummaryReportExcel method of SummaryController ");
		XSSFWorkbook wbk = summaryService.fetchSummaryList(dto.getStartDate(), dto.getEndDate());
//		XSSFWorkbook wbk = summaryService.fetchSummaryList(param1, param2); 
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        wbk.write(outputStream);
        wbk.close();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "summaryReport.xlsx");
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(outputStream.size())
                .body(new ByteArrayResource(outputStream.toByteArray()));
    }
	

}
