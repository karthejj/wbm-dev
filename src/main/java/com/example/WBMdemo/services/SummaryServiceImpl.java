package com.example.WBMdemo.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.dto.SummaryDTO;
import com.example.WBMdemo.entity.Material;
import com.example.WBMdemo.repository.MaterialRepository;
import com.example.WBMdemo.repository.TransactionRepository;

@Service
public class SummaryServiceImpl implements SummaryService {
	
    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;
    
	@Autowired
	private MaterialRepository materialRepo;
	
	@Autowired
	TransactionRepository transRepo;
	
	public XSSFWorkbook fetchSummaryList(String startDate, String endDate) {
		
		List<Material> ml1 = materialRepo.findAllByOrderByMaterialNameAsc();
		ml1.removeIf(t -> t.getMaterialName().equals("weightOnly"));
		List<SummaryDTO> summaryIncList = fetchSumIncOutWeiYardByMaterialList("INC", startDate, endDate);
		List<SummaryDTO> summaryOutList = fetchSumIncOutWeiYardByMaterialList("OUT", startDate, endDate);
		List<SummaryDTO> summaryWeighOnlyList = fetchSumIncOutWeiYardByMaterialList("WEIGH", startDate, endDate);
		List<SummaryDTO> materialInYardList = fetchSumIncOutWeiYardByMaterialList("materialInYard", startDate, endDate);
		
		List<SummaryDTO> summaryIncCreationUserList = fetchSumIncOutWeiCreationByUserList("INC", startDate, endDate);
		List<SummaryDTO> summaryOutCreationUserList = fetchSumIncOutWeiCreationByUserList("OUT", startDate, endDate);
		List<SummaryDTO> summaryWeiCreationUserList = fetchSumIncOutWeiCreationByUserList("WEIGH", startDate, endDate);
		
		List<SummaryDTO> summaryIncClosureUserList = fetchSumIncOutClosureByUserList("INC", startDate, endDate);
		List<SummaryDTO> summaryOutClosureUserList = fetchSumIncOutClosureByUserList("OUT", startDate, endDate);
		
        int cancelledTransCount = transRepo.findCancelledTransactionCount(
        		LocalDate.parse(startDate),LocalDate.parse(endDate));
        
   		String query_CancelledbyUser = loadSql("queries/getCancelledTransactionByUser.sql");
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);
	   	List<SummaryDTO> summaryCancelledByUser = jdbcTemplate.query(query_CancelledbyUser, params, (rs, rowNum) -> {
            SummaryDTO dto = new SummaryDTO();
            dto.setCreatedBy(rs.getString("closed_by"));
            dto.setNoOfTransaction(Integer.valueOf(rs.getString("noOfTransaction")));
            return dto;
        });
	   	
   		String query_CancelledIdandUser = loadSql("queries/getCancelledTransactionIdandUser.sql");
        Map<String, Object> paramsCancelled = new HashMap<>();
        paramsCancelled.put("startDate", startDate);
        paramsCancelled.put("endDate", endDate);
	   	List<SummaryDTO> summaryCancelledIdandUser = jdbcTemplate.query(query_CancelledIdandUser, 
	   			paramsCancelled, (rs, rowNum) -> {
            SummaryDTO dto = new SummaryDTO();
            dto.setTransactionId(rs.getString("transactionId"));
            dto.setModifiedBy(rs.getString("closed_by"));
            return dto;
        });
        
        Map<String, List<SummaryDTO>> mapOfSummarLists = new HashMap<String, List<SummaryDTO>>();
        mapOfSummarLists.put("summaryInc", summaryIncList);
        mapOfSummarLists.put("summaryOut", summaryOutList);
        mapOfSummarLists.put("summaryWeigh", summaryWeighOnlyList);
        mapOfSummarLists.put("summaryQuantityInYard", materialInYardList);
        
   		String query = loadSql("queries/getUserNamesFromTransaction.sql"); // Load SQL query from file
        List<String> userList = jdbcTemplate.query(query, new MapSqlParameterSource(), (rs, rowNum) -> rs.getString("userName")); 
        Map<String, List<String>> mapOfUserList = new HashMap<String, List<String>>();
        mapOfUserList.put("mapOfUserList", userList);
        
        mapOfSummarLists.put("summaryIncCreation", summaryIncCreationUserList);
        mapOfSummarLists.put("summaryIncClosure", summaryIncClosureUserList);
        mapOfSummarLists.put("summaryOutCreation", summaryOutCreationUserList);
        mapOfSummarLists.put("summaryOutClosure", summaryOutClosureUserList);
        mapOfSummarLists.put("summaryWeighCreation", summaryWeiCreationUserList);
        mapOfSummarLists.put("summaryCancelledByUser", summaryCancelledByUser);
        mapOfSummarLists.put("summaryCancelledIdandUser", summaryCancelledIdandUser);
        
        Map<String, List<Material>> masterListOfMaterials = new HashMap<String, List<Material>>();
        masterListOfMaterials.put("materialList", ml1);
        
        XSSFWorkbook wbk = writeToXcel(mapOfSummarLists, masterListOfMaterials, cancelledTransCount, mapOfUserList);
        return wbk;
	}
	
   public XSSFWorkbook writeToXcel(Map<String, List<SummaryDTO>> mapOfSummarLists, 
		   Map<String, List<Material>> masterListOfMaterials, int cancelledTransCount,
		   Map<String, List<String>> userList) {
       
	      XSSFWorkbook workbook=new XSSFWorkbook(); 
       	  XSSFSheet sheet=workbook.createSheet("ListByMaterial"); 
          XSSFRow row1=sheet.createRow(0);
          Font boldFont = workbook.createFont();          // Create a bold font
          boldFont.setBold(true);
          CellStyle boldStyle = workbook.createCellStyle();           // Create a cell style with the bold font
          boldStyle.setFont(boldFont);
          
          Cell cell = row1.createCell(0);
          cell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          cell.setCellValue("SNo");
          
          Cell MaterialNameCell = row1.createCell(1);
          MaterialNameCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          MaterialNameCell.setCellValue("MaterialName");
          
          Cell noIncTransCell = row1.createCell(2);
          noIncTransCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          noIncTransCell.setCellValue("Number of Incoming Transactions");
          
          Cell incomingQtyCell = row1.createCell(3);
          incomingQtyCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          incomingQtyCell.setCellValue("Incoming Quantity");
          
          Cell incTransAmtCell = row1.createCell(4);
          incTransAmtCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          incTransAmtCell.setCellValue("Incoming Transaction Amount");
          
          Cell noOutTransCell = row1.createCell(5);
          noOutTransCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          noOutTransCell.setCellValue("Number of OutGoing Transactions");
          
          Cell outQtyCell = row1.createCell(6);
          outQtyCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          outQtyCell.setCellValue("OutGoing Quantity");
          
          Cell outTransAmtCell = row1.createCell(7);
          outTransAmtCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          outTransAmtCell.setCellValue("OutGoing Transaction Amount");
          
          Cell qtyInYardCell = row1.createCell(8);
          qtyInYardCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          qtyInYardCell.setCellValue("Quantity Available in Yard");
          
//          row1.createCell(1).setCellValue("MaterialName");
          
          for(HashMap.Entry entry1:masterListOfMaterials.entrySet()) {
        	  int rowNo=1;
        	  if(entry1.getKey().equals("materialList")){
        		  int sNo=1;
            		int noOfIncTransactionTotal=0;
            		BigDecimal incQuantityTotal = new BigDecimal(0);
            		BigDecimal incMaterialPriceTotal = new BigDecimal(0);
            		int noOfOutTransactionTotal=0;
            		BigDecimal outQuantityTotal = new BigDecimal(0);
            		BigDecimal outMaterialPriceTotal = new BigDecimal(0);
            		BigDecimal qtyYardTotal = new BigDecimal(0);
        		  for(Material dto : (List<Material>) entry1.getValue()) {
    	            	XSSFRow row=sheet.createRow(rowNo); 
    	            	String matName = dto.getMaterialName();
    	            	row.createCell(0).setCellValue(sNo);
    	            	row.createCell(1).setCellValue(matName); 
    	            	for(HashMap.Entry entry:mapOfSummarLists.entrySet()) { 
    	              	  if(entry.getKey().equals("summaryInc")){
		    	              boolean materialAvailableFlagInc = false;
		    	              List<SummaryDTO> summaryIncList = (List<SummaryDTO>) entry.getValue();
		    	              if(summaryIncList.size()!=0) {
		    	            	  for(SummaryDTO summaryDto : summaryIncList) {
		    	              		if(summaryDto.getMaterialName().equals(matName)) {
			    	        	    	materialAvailableFlagInc = true;
			    	        	    	row.createCell(2).setCellValue(summaryDto.getNoOfTransaction().toString());
			    	        	       	noOfIncTransactionTotal+=summaryDto.getNoOfTransaction();
			        	        	    row.createCell(3).setCellValue(summaryDto.getQuantity().toString());
			        	        	    incQuantityTotal=incQuantityTotal.add(summaryDto.getQuantity());
			        	        	    row.createCell(4).setCellValue(summaryDto.getMaterialPrice().toString());
			        	        	    incMaterialPriceTotal=incMaterialPriceTotal.add(summaryDto.getMaterialPrice());
			    	        	     }
		    	        	      }
		    	              } else {
			    	        	     	row.createCell(2).setCellValue("0");
			        	        	    row.createCell(3).setCellValue("0");
			        	        	    row.createCell(4).setCellValue("0");
		    	              }
		    	        	  if(!materialAvailableFlagInc) {
		    	        		  row.createCell(2).setCellValue("0");
				        	      row.createCell(3).setCellValue("0");
				        	      row.createCell(4).setCellValue("0");
				        	  }
    	              	  }
    	              	  if(entry.getKey().equals("summaryOut")){
		     	              	boolean materialAvailableFlagOut = false; 
		     	              	List<SummaryDTO> summaryOutList = (List<SummaryDTO>) entry.getValue();
		     	        	    if(summaryOutList.size()!=0) { 
		     	        	    	for(SummaryDTO summaryDto : summaryOutList) {
		     	        	    		if(summaryDto.getMaterialName().equals(matName)) {
		     	        	    			materialAvailableFlagOut=true;
			 	    	        	       	row.createCell(5).setCellValue(summaryDto.getNoOfTransaction().toString());
			 	    	        	       	noOfOutTransactionTotal+=summaryDto.getNoOfTransaction();
			 	        	        	    row.createCell(6).setCellValue(summaryDto.getQuantity().toString());
			 	        	        	    outQuantityTotal=outQuantityTotal.add(summaryDto.getQuantity());
			 	        	        	    row.createCell(7).setCellValue(summaryDto.getMaterialPrice().toString());
			 	        	        	    outMaterialPriceTotal=outMaterialPriceTotal.add(summaryDto.getMaterialPrice());
			 	    	        	    }
			     	        	    }
		    	              	  } else {
				    	        	   row.createCell(5).setCellValue("0");
				        	           row.createCell(6).setCellValue("0");
				        	           row.createCell(7).setCellValue("0");
		    	              	  }
		     	        	      if(!materialAvailableFlagOut) {
		     	        	    	  row.createCell(5).setCellValue("0");
					        	      row.createCell(6).setCellValue("0");
					        	      row.createCell(7).setCellValue("0");
			     	              }
    	              	  }
    	              	  if(entry.getKey().equals("summaryQuantityInYard")) {
      	              		boolean materialAvailableFlagYard = false;
      	              		List<SummaryDTO> summaryQtyYardList = (List<SummaryDTO>) entry.getValue();
      	              		if(summaryQtyYardList.size()!=0) {
    	              		  for(SummaryDTO summaryYardDto : summaryQtyYardList) {
    	              			if(summaryYardDto.getMaterialName().equals(matName)) {
    	              			materialAvailableFlagYard=true;
      	              			  row.createCell(8).setCellValue(summaryYardDto.getQuantity().toString());
      	              			qtyYardTotal=qtyYardTotal.add(summaryYardDto.getQuantity());
    	              			}
    	              		  }
      	              		} else { 
		    	        	     	row.createCell(8).setCellValue("0");
		    	        	 }
      	              		if(!materialAvailableFlagYard) {
		    	        	     	row.createCell(8).setCellValue("0");
		    	        	 }
    	              	  }
    	            	}
    	            	rowNo++;
    	            	sNo++;
        		  }
        		  XSSFRow rowTotal=sheet.createRow(++rowNo); 
                  Cell totalCell = rowTotal.createCell(1);
                  totalCell.setCellStyle(boldStyle);          
                  totalCell.setCellValue("Total");
                  
                  Cell totalNoIncTransCell = rowTotal.createCell(2);
                  totalNoIncTransCell.setCellStyle(boldStyle);          
                  totalNoIncTransCell.setCellValue(noOfIncTransactionTotal);
                  
                  Cell totalIncQtyCell = rowTotal.createCell(3);
                  totalIncQtyCell.setCellStyle(boldStyle);          
                  totalIncQtyCell.setCellValue(incQuantityTotal.toString());
                  
                  Cell totalIncMatPriceCell = rowTotal.createCell(4);
                  totalIncMatPriceCell.setCellStyle(boldStyle);          
                  totalIncMatPriceCell.setCellValue(incMaterialPriceTotal.toString());
                  
                  Cell totalNoOutTransCell = rowTotal.createCell(5);
                  totalNoOutTransCell.setCellStyle(boldStyle);          
                  totalNoOutTransCell.setCellValue(noOfOutTransactionTotal);
                  
                  Cell totalOutQtyCell = rowTotal.createCell(6);
                  totalOutQtyCell.setCellStyle(boldStyle);          
                  totalOutQtyCell.setCellValue(outQuantityTotal.toString());
                  
                  Cell totalOutMatPriceCell = rowTotal.createCell(7);
                  totalOutMatPriceCell.setCellStyle(boldStyle);          
                  totalOutMatPriceCell.setCellValue(outMaterialPriceTotal.toString());
                  
                  Cell totalQtyYardCell = rowTotal.createCell(8);
                  totalQtyYardCell.setCellStyle(boldStyle);          
                  totalQtyYardCell.setCellValue(qtyYardTotal.toString());
                  
                  rowNo=rowNo+1;
        		  XSSFRow rowWeightOnly=sheet.createRow(++rowNo);
                  Cell weightOnlyCell = rowWeightOnly.createCell(2);
                  weightOnlyCell.setCellStyle(boldStyle);
                  weightOnlyCell.setCellValue("Number Of Weight Only Transaction");
                  
                  Cell weightOnlyTransQtyCell = rowWeightOnly.createCell(3);
                  weightOnlyTransQtyCell.setCellStyle(boldStyle);
                  weightOnlyTransQtyCell.setCellValue("Quantity in Weight Only");
                  
                  Cell weightOnlyTransAmtCell = rowWeightOnly.createCell(4);
                  weightOnlyTransAmtCell.setCellStyle(boldStyle);
                  weightOnlyTransAmtCell.setCellValue("Transaction Amount in Weight Only");
                  
                  for(HashMap.Entry entry:mapOfSummarLists.entrySet()) { 
                	  if(entry.getKey().equals("summaryWeigh")) {
//                		  boolean weighOnlyFlag = false;
                		  List<SummaryDTO> weighDto = (List<SummaryDTO>)entry.getValue();
                		  if(weighDto.size()!=0) {
//                			  weighOnlyFlag = true;
                			  for(SummaryDTO summaryDto : weighDto) {
		                		  XSSFRow rowWeightOnlyContent=sheet.createRow(++rowNo);
		                          Cell weightNotransCell = rowWeightOnlyContent.createCell(2);
		                          weightNotransCell.setCellValue(summaryDto.getNoOfTransaction());
		                          
		                          Cell weightOnlyQtyCell = rowWeightOnlyContent.createCell(3);
		                          weightOnlyQtyCell.setCellValue(summaryDto.getQuantity().toString());
		                          
		                          Cell weightOnlyAmtCell = rowWeightOnlyContent.createCell(4);
		                          weightOnlyAmtCell.setCellValue(summaryDto.getMaterialPrice().toString());
	                		  }
                		  } else {
//                		  if(!weighOnlyFlag) {
                			  XSSFRow rowWeightOnlyContent=sheet.createRow(++rowNo);
	                          Cell weightNotransCell = rowWeightOnlyContent.createCell(2);
	                          weightNotransCell.setCellValue("0");
	                          
	                          Cell weightOnlyQtyCell = rowWeightOnlyContent.createCell(3);
	                          weightOnlyQtyCell.setCellValue("0");
	                          
	                          Cell weightOnlyAmtCell = rowWeightOnlyContent.createCell(4);
	                          weightOnlyAmtCell.setCellValue("0");
                		  }
                	  }
                	    
                  }
                  rowNo=rowNo+1;
        		  XSSFRow rowCancelledTrans=sheet.createRow(++rowNo);
                  Cell cancelledTransCell = rowCancelledTrans.createCell(2);
                  cancelledTransCell.setCellStyle(boldStyle);
                  cancelledTransCell.setCellValue("Number Of Cancelled Transaction");
                  
                  XSSFRow rowCancelledTransCount=sheet.createRow(++rowNo);
                  Cell rowCancelledTransCountCell = rowCancelledTransCount.createCell(2);
                  rowCancelledTransCountCell.setCellValue(cancelledTransCount);
        		  
        	  }
          }
//          FileOutputStream file = null;
//		try {
//			file = new FileOutputStream("C://Users//karth//wbm120524//SummaryReport.xlsx");
//			workbook.write(file);
//			file.close();
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//          System.out.println("Data Copied to Excel");
          
          
       	  XSSFSheet sheet2=workbook.createSheet("ListByUser"); 
          XSSFRow sheet2_row1=sheet2.createRow(0);
//          Font boldFont = workbook.createFont();          // Create a bold font
//          boldFont.setBold(true);
//          CellStyle boldStyle = workbook.createCellStyle();           // Create a cell style with the bold font
//          boldStyle.setFont(boldFont);
          
          Cell sheet2_cell = sheet2_row1.createCell(0);
          sheet2_cell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_cell.setCellValue("SNo");
          
          Cell userNameCell = sheet2_row1.createCell(1);
          userNameCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          userNameCell.setCellValue("UserName");
          
          Cell sheet2_noIncTransCreatedCell = sheet2_row1.createCell(2);
          sheet2_noIncTransCreatedCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_noIncTransCreatedCell.setCellValue("Number of Incoming Transactions Created");
          
          Cell sheet2_incomingQtyCreateCell = sheet2_row1.createCell(3);
          sheet2_incomingQtyCreateCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_incomingQtyCreateCell.setCellValue("Incoming Quantity Created");
          
          Cell sheet2_incTransAmtCreateCell = sheet2_row1.createCell(4);
          sheet2_incTransAmtCreateCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_incTransAmtCreateCell.setCellValue("Incoming Transaction Amount During Creation");
          
          Cell sheet2_noIncTransCloseCell = sheet2_row1.createCell(5);
          sheet2_noIncTransCloseCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_noIncTransCloseCell.setCellValue("Number of Incoming Transactions Closed");
          
          Cell sheet2_incQtyCloseCell = sheet2_row1.createCell(6);
          sheet2_incQtyCloseCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_incQtyCloseCell.setCellValue("Incoming Quantity Closed");
          
          Cell sheet2_incTransAmtClosureCell = sheet2_row1.createCell(7);
          sheet2_incTransAmtClosureCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_incTransAmtClosureCell.setCellValue("Incoming Transaction Amount During Closure");
          
          Cell sheet2_noOutTransCreatedCell = sheet2_row1.createCell(8);
          sheet2_noOutTransCreatedCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_noOutTransCreatedCell.setCellValue("Number of OutGoing Transactions Created");
          
          Cell sheet2_outgoingQtyCreatedCell = sheet2_row1.createCell(9);
          sheet2_outgoingQtyCreatedCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_outgoingQtyCreatedCell.setCellValue("OutGoing Quantity Created");
          
          Cell sheet2_outTransAmtCreationCell = sheet2_row1.createCell(10);
          sheet2_outTransAmtCreationCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_outTransAmtCreationCell.setCellValue("OutGoing Transaction Amount During Creation");
          
          Cell sheet2_noOutTransClosureCell = sheet2_row1.createCell(11);
          sheet2_noOutTransClosureCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_noOutTransClosureCell.setCellValue("Number of OutGoing Transactions Closed");
          
          Cell sheet2_outQtyClosedCell = sheet2_row1.createCell(12);
          sheet2_outQtyClosedCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_outQtyClosedCell.setCellValue("OutGoing Quantity Closed");
          
          Cell sheet2_outTransAmtClosureCell = sheet2_row1.createCell(13);
          sheet2_outTransAmtClosureCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_outTransAmtClosureCell.setCellValue("OutGoing Transaction Amount During Closure");
          
          Cell sheet2_noTransWeiCreatedCell = sheet2_row1.createCell(14);
          sheet2_noTransWeiCreatedCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_noTransWeiCreatedCell.setCellValue("Number of WeighOnly Transactions Created");
          
          Cell sheet2_WeiQtyCreateCell = sheet2_row1.createCell(15);
          sheet2_WeiQtyCreateCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_WeiQtyCreateCell.setCellValue("Quantity Created in WeighOnly Transaction");
          
          Cell sheet2_WeiTransAmtCreateCell = sheet2_row1.createCell(16);
          sheet2_WeiTransAmtCreateCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_WeiTransAmtCreateCell.setCellValue("Transaction Amount in WeighOnly");

          Cell sheet2_CancelledByUserCell = sheet2_row1.createCell(17);
          sheet2_CancelledByUserCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_CancelledByUserCell.setCellValue("Number of Cancelled Transaction");
          
          for(HashMap.Entry entry1:userList.entrySet()) {
        	  int rowNo=1;
        	  if(entry1.getKey().equals("mapOfUserList")){
        		  int sNo=1;
            		int noOfIncTransactionCreateTotal=0;
            		BigDecimal incQuantityCreateTotal = new BigDecimal(0);
            		BigDecimal incMaterialPriceCreateTotal = new BigDecimal(0);
            		int noOfIncTransactionCloseTotal=0;
            		BigDecimal incQuantityCloseTotal = new BigDecimal(0);
            		BigDecimal incMaterialPriceCloseTotal = new BigDecimal(0);
            		
            		int noOfOutTransactionCreateTotal=0;
            		BigDecimal outQuantityCreateTotal = new BigDecimal(0);
            		BigDecimal outMaterialPriceCreateTotal = new BigDecimal(0);
            		int noOfOutTransactionCloseTotal=0;
            		BigDecimal outQuantityCloseTotal = new BigDecimal(0);
            		BigDecimal outMaterialPriceCloseTotal = new BigDecimal(0);
            		
            		int noOfWeiTransactionCreateTotal=0;
            		BigDecimal weiQuantityCreateTotal = new BigDecimal(0);
            		BigDecimal weiMaterialPriceCreateTotal = new BigDecimal(0);
            		int noOfCancelledTransacTotal = 0;
            		
            		int noOfOutTransactionTotal=0;
            		BigDecimal outQuantityTotal = new BigDecimal(0);
            		BigDecimal outMaterialPriceTotal = new BigDecimal(0);
//            		BigDecimal qtyYardTotal = new BigDecimal(0);
            		for(String uName : (List<String>) entry1.getValue()) {
    	            	XSSFRow row=sheet2.createRow(rowNo); 
    	            	row.createCell(0).setCellValue(sNo);
    	            	row.createCell(1).setCellValue(uName);
    	            	for(HashMap.Entry entry:mapOfSummarLists.entrySet()) { 
      	              	  if(entry.getKey().equals("summaryIncCreation")){
//    		    	              boolean materialAvailableFlagInc = false;
      	              		boolean userHasTransactionCreatFlagInc = false;	
    		    	              List<SummaryDTO> summaryIncList = (List<SummaryDTO>) entry.getValue();
    		    	              if(summaryIncList.size()!=0) {
    		    	            	  for(SummaryDTO summaryDto : summaryIncList) {
    		    	              		if(summaryDto.getCreatedBy().equals(uName)) {
//    			    	        	    	materialAvailableFlagInc = true;
    		    	              			userHasTransactionCreatFlagInc = true;
    			    	        	    	row.createCell(2).setCellValue(summaryDto.getNoOfTransaction().toString());
    			    	        	    	noOfIncTransactionCreateTotal+=summaryDto.getNoOfTransaction();
    			        	        	    row.createCell(3).setCellValue(summaryDto.getQuantity().toString());
    			        	        	    incQuantityCreateTotal=incQuantityCreateTotal.add(summaryDto.getQuantity());
    			        	        	    row.createCell(4).setCellValue(summaryDto.getMaterialPrice().toString());
    			        	        	    incMaterialPriceCreateTotal=incMaterialPriceCreateTotal.add(summaryDto.getMaterialPrice());
    			    	        	     }
    		    	        	      }
    		    	              } else {
    			    	        	     	row.createCell(2).setCellValue("0");
    			        	        	    row.createCell(3).setCellValue("0");
    			        	        	    row.createCell(4).setCellValue("0");
    		    	              }
    		    	        	  if(!userHasTransactionCreatFlagInc) {
    		    	        		  row.createCell(2).setCellValue("0");
    				        	      row.createCell(3).setCellValue("0");
    				        	      row.createCell(4).setCellValue("0");
    				        	  }
        	              	  }
      	              	  if(entry.getKey().equals("summaryIncClosure")){
//		    	              boolean materialAvailableFlagInc = false;
  	              		boolean userHasTransactionCloseFlagInc = false;	
		    	              List<SummaryDTO> summaryIncList = (List<SummaryDTO>) entry.getValue();
		    	              if(summaryIncList.size()!=0) {
		    	            	  for(SummaryDTO summaryDto : summaryIncList) {
		    	              		if(summaryDto.getModifiedBy().equals(uName)) {
//			    	        	    	materialAvailableFlagInc = true;
		    	              			userHasTransactionCloseFlagInc = true;
			    	        	    	row.createCell(5).setCellValue(summaryDto.getNoOfTransaction().toString());
			    	        	    	noOfIncTransactionCloseTotal+=summaryDto.getNoOfTransaction();
			        	        	    row.createCell(6).setCellValue(summaryDto.getQuantity().toString());
			        	        	    incQuantityCloseTotal=incQuantityCloseTotal.add(summaryDto.getQuantity());
			        	        	    row.createCell(7).setCellValue(summaryDto.getMaterialPrice().toString());
			        	        	    incMaterialPriceCloseTotal=incMaterialPriceCloseTotal.add(summaryDto.getMaterialPrice());
			    	        	     }
		    	        	      }
		    	              } else {
			    	        	     	row.createCell(5).setCellValue("0");
			        	        	    row.createCell(6).setCellValue("0");
			        	        	    row.createCell(7).setCellValue("0");
		    	              }
		    	        	  if(!userHasTransactionCloseFlagInc) {
		    	        		  row.createCell(5).setCellValue("0");
				        	      row.createCell(6).setCellValue("0");
				        	      row.createCell(7).setCellValue("0");
				        	  }
    	              	  }
      	              	  if(entry.getKey().equals("summaryOutCreation")){
//		    	              boolean materialAvailableFlagInc = false;
  	              		boolean userHasTransactionCreateFlagOut = false;	
		    	              List<SummaryDTO> summaryIncList = (List<SummaryDTO>) entry.getValue();
		    	              if(summaryIncList.size()!=0) {
		    	            	  for(SummaryDTO summaryDto : summaryIncList) {
		    	              		if(summaryDto.getCreatedBy().equals(uName)) {
//			    	        	    	materialAvailableFlagInc = true;
		    	              			userHasTransactionCreateFlagOut = true;
			    	        	    	row.createCell(8).setCellValue(summaryDto.getNoOfTransaction().toString());
			    	        	    	noOfOutTransactionCreateTotal+=summaryDto.getNoOfTransaction();
			        	        	    row.createCell(9).setCellValue(summaryDto.getQuantity().toString());
			        	        	    outQuantityCreateTotal=outQuantityCreateTotal.add(summaryDto.getQuantity());
			        	        	    row.createCell(10).setCellValue(summaryDto.getMaterialPrice().toString());
			        	        	    outMaterialPriceCreateTotal=outMaterialPriceCreateTotal.add(summaryDto.getMaterialPrice());
			    	        	     }
		    	        	      }
		    	              } else {
			    	        	     	row.createCell(8).setCellValue("0");
			        	        	    row.createCell(9).setCellValue("0");
			        	        	    row.createCell(10).setCellValue("0");
		    	              }
		    	        	  if(!userHasTransactionCreateFlagOut) {
		    	        		  row.createCell(8).setCellValue("0");
				        	      row.createCell(9).setCellValue("0");
				        	      row.createCell(10).setCellValue("0");
				        	  }
    	              	  }
      	              	  if(entry.getKey().equals("summaryOutClosure")){
//		    	              boolean materialAvailableFlagInc = false;
  	              		boolean userHasTransactionCloseFlagOut = false;	
		    	              List<SummaryDTO> summaryIncList = (List<SummaryDTO>) entry.getValue();
		    	              if(summaryIncList.size()!=0) {
		    	            	  for(SummaryDTO summaryDto : summaryIncList) {
		    	              		if(summaryDto.getModifiedBy().equals(uName)) {
//			    	        	    	materialAvailableFlagInc = true;
		    	              			userHasTransactionCloseFlagOut = true;
			    	        	    	row.createCell(11).setCellValue(summaryDto.getNoOfTransaction().toString());
			    	        	    	noOfOutTransactionCloseTotal+=summaryDto.getNoOfTransaction();
			        	        	    row.createCell(12).setCellValue(summaryDto.getQuantity().toString());
			        	        	    outQuantityCloseTotal=outQuantityCloseTotal.add(summaryDto.getQuantity());
			        	        	    row.createCell(13).setCellValue(summaryDto.getMaterialPrice().toString());
			        	        	    outMaterialPriceCloseTotal=outMaterialPriceCloseTotal.add(summaryDto.getMaterialPrice());
			    	        	     }
		    	        	      }
		    	              } else {
			    	        	     	row.createCell(11).setCellValue("0");
			        	        	    row.createCell(12).setCellValue("0");
			        	        	    row.createCell(13).setCellValue("0");
		    	              }
		    	        	  if(!userHasTransactionCloseFlagOut) {
		    	        		  row.createCell(11).setCellValue("0");
				        	      row.createCell(12).setCellValue("0");
				        	      row.createCell(13).setCellValue("0");
				        	  }
    	              	  }
      	              	  if(entry.getKey().equals("summaryWeighCreation")){
//		    	              boolean materialAvailableFlagInc = false;
  	              		boolean userHasTransactionCloseFlagWei = false;	
		    	              List<SummaryDTO> summaryIncList = (List<SummaryDTO>) entry.getValue();
		    	              if(summaryIncList.size()!=0) {
		    	            	  for(SummaryDTO summaryDto : summaryIncList) {
		    	              		if(summaryDto.getCreatedBy().equals(uName)) {
//			    	        	    	materialAvailableFlagInc = true;
		    	              			userHasTransactionCloseFlagWei = true;
			    	        	    	row.createCell(14).setCellValue(summaryDto.getNoOfTransaction().toString());
			    	        	    	noOfWeiTransactionCreateTotal+=summaryDto.getNoOfTransaction();
			        	        	    row.createCell(15).setCellValue(summaryDto.getQuantity().toString());
			        	        	    weiQuantityCreateTotal=weiQuantityCreateTotal.add(summaryDto.getQuantity());
			        	        	    row.createCell(16).setCellValue(summaryDto.getMaterialPrice().toString());
			        	        	    weiMaterialPriceCreateTotal=weiMaterialPriceCreateTotal.add(summaryDto.getMaterialPrice());
			    	        	     }
		    	        	      }
		    	              } else {
			    	        	     	row.createCell(14).setCellValue("0");
			        	        	    row.createCell(15).setCellValue("0");
			        	        	    row.createCell(16).setCellValue("0");
		    	              }
		    	        	  if(!userHasTransactionCloseFlagWei) {
		    	        		  row.createCell(14).setCellValue("0");
				        	      row.createCell(15).setCellValue("0");
				        	      row.createCell(16).setCellValue("0");
				        	  }
    	              	  }
      	              	  if(entry.getKey().equals("summaryCancelledByUser")){
      	              		  boolean userHasCancelledTransaction = false;	
		    	              List<SummaryDTO> summaryIncList = (List<SummaryDTO>) entry.getValue();
		    	              if(summaryIncList.size()!=0) {
		    	            	  for(SummaryDTO summaryDto : summaryIncList) {
		    	              		if(summaryDto.getCreatedBy().equals(uName)) {
		    	              			userHasCancelledTransaction = true;
			    	        	    	row.createCell(17).setCellValue(summaryDto.getNoOfTransaction().toString());
			    	        	    	noOfCancelledTransacTotal+=summaryDto.getNoOfTransaction();
			    	        	     }
		    	        	      }
		    	              } else {
			    	        	     	row.createCell(17).setCellValue("0");

		    	              }
		    	        	  if(!userHasCancelledTransaction) {
		    	        		  row.createCell(17).setCellValue("0");
				        	  }
    	              	  }
    	            	}
    	            	rowNo++;
    	            	sNo++;
        		  }
          		  XSSFRow rowTotal=sheet2.createRow(++rowNo); 
                  Cell totalCell = rowTotal.createCell(1);
                  totalCell.setCellStyle(boldStyle);          
                  totalCell.setCellValue("Total");
                  
                  Cell totalNoIncTransCreateCell = rowTotal.createCell(2);
                  totalNoIncTransCreateCell.setCellStyle(boldStyle);          
                  totalNoIncTransCreateCell.setCellValue(noOfIncTransactionCreateTotal);
                  
                  Cell totalIncQtyCreateCell = rowTotal.createCell(3);
                  totalIncQtyCreateCell.setCellStyle(boldStyle);          
                  totalIncQtyCreateCell.setCellValue(incQuantityCreateTotal.toString());
                  
                  Cell totalIncCreatePriceCell = rowTotal.createCell(4);
                  totalIncCreatePriceCell.setCellStyle(boldStyle);          
                  totalIncCreatePriceCell.setCellValue(incMaterialPriceCreateTotal.toString());
                  
                  Cell totalNoIncTransCloseCell = rowTotal.createCell(5);
                  totalNoIncTransCloseCell.setCellStyle(boldStyle);          
                  totalNoIncTransCloseCell.setCellValue(noOfIncTransactionCloseTotal);
                  
                  Cell totalIncQtyCloseCell = rowTotal.createCell(6);
                  totalIncQtyCloseCell.setCellStyle(boldStyle);          
                  totalIncQtyCloseCell.setCellValue(incQuantityCloseTotal.toString());
                  
                  Cell totalIncClosePriceCell = rowTotal.createCell(7);
                  totalIncClosePriceCell.setCellStyle(boldStyle);          
                  totalIncClosePriceCell.setCellValue(incMaterialPriceCloseTotal.toString());
                  
                  Cell totalNoOutTransCreateCell = rowTotal.createCell(8);
                  totalNoOutTransCreateCell.setCellStyle(boldStyle);          
                  totalNoOutTransCreateCell.setCellValue(noOfOutTransactionCreateTotal);
                  
                  Cell totalOutQtyCreateCell = rowTotal.createCell(9);
                  totalOutQtyCreateCell.setCellStyle(boldStyle);          
                  totalOutQtyCreateCell.setCellValue(outQuantityCreateTotal.toString());
                  
                  Cell totalOutCreatePriceCell = rowTotal.createCell(10);
                  totalOutCreatePriceCell.setCellStyle(boldStyle);          
                  totalOutCreatePriceCell.setCellValue(outMaterialPriceCreateTotal.toString());
                  
                  Cell totalNoOutTransCloseCell = rowTotal.createCell(11);
                  totalNoOutTransCloseCell.setCellStyle(boldStyle);          
                  totalNoOutTransCloseCell.setCellValue(noOfOutTransactionCloseTotal);
                  
                  Cell totalOutQtyCloseCell = rowTotal.createCell(12);
                  totalOutQtyCloseCell.setCellStyle(boldStyle);          
                  totalOutQtyCloseCell.setCellValue(outQuantityCloseTotal.toString());
                  
                  Cell totalOutClosePriceCell = rowTotal.createCell(13);
                  totalOutClosePriceCell.setCellStyle(boldStyle);          
                  totalOutClosePriceCell.setCellValue(outMaterialPriceCloseTotal.toString());
                  
                  Cell totalNoWeiTransCreateCell = rowTotal.createCell(14);
                  totalNoWeiTransCreateCell.setCellStyle(boldStyle);          
                  totalNoWeiTransCreateCell.setCellValue(noOfWeiTransactionCreateTotal);
                  
                  Cell totalWeiQtyCreateCell = rowTotal.createCell(15);
                  totalWeiQtyCreateCell.setCellStyle(boldStyle);          
                  totalWeiQtyCreateCell.setCellValue(weiQuantityCreateTotal.toString());
                  
                  Cell totalWeiCreatePriceCell = rowTotal.createCell(16);
                  totalWeiCreatePriceCell.setCellStyle(boldStyle);          
                  totalWeiCreatePriceCell.setCellValue(weiMaterialPriceCreateTotal.toString());
                  
                  Cell totalCancelledTransacCell = rowTotal.createCell(17);
                  totalCancelledTransacCell.setCellStyle(boldStyle);          
                  totalCancelledTransacCell.setCellValue(noOfCancelledTransacTotal);
    	            	}
            		}	
          
          XSSFSheet sheet3=workbook.createSheet("CancelledList"); 
          XSSFRow sheet3_row1=sheet3.createRow(0);
          
          Cell sheet2_noIncTransCancelledCell = sheet3_row1.createCell(0);
          sheet2_noIncTransCancelledCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          sheet2_noIncTransCancelledCell.setCellValue("Cancelled Transactions Id");
          
          Cell userCancelledCell = sheet3_row1.createCell(1);
          userCancelledCell.setCellStyle(boldStyle);          // Apply the bold style to the cell
          userCancelledCell.setCellValue("UserName");
          
      	  for(HashMap.Entry entry:mapOfSummarLists.entrySet()) { 
        	  if(entry.getKey().equals("summaryCancelledIdandUser")){
        		  List<SummaryDTO> summaryList = (List<SummaryDTO>) entry.getValue();
            	  int rowNo=1;
	              if(summaryList.size()!=0) {
	            	  for(SummaryDTO summaryDto : summaryList) {
	                    XSSFRow row=sheet3.createRow(rowNo); 
	            		row.createCell(0).setCellValue(summaryDto.getTransactionId());
      	        	    row.createCell(1).setCellValue(summaryDto.getModifiedBy());
      	        	    rowNo++;
	            	  }
	              }
        	  }
      	  }
          

          
          return workbook;
   }     
	
   private List<SummaryDTO> fetchSumIncOutWeiYardByMaterialList(String summaryType, String startDate, String endDate){
	   
	   	List<SummaryDTO> dtoResult = null;
	   	if(summaryType.equals("INC") || summaryType.equals("OUT"))  {
	   		String query = loadSql("queries/summary_Incoming_Outgoing_query.sql"); // Load SQL query from file
	        Map<String, Object> params = new HashMap<>();
	        params.put("summaryType", summaryType);
	        params.put("startDate", startDate);
	        params.put("endDate", endDate);
	        dtoResult = jdbcTemplate.query(query, params, (rs, rowNum) -> {
	            SummaryDTO dto = new SummaryDTO();
	            dto.setMaterialName(rs.getString("materialName"));
	            dto.setNoOfTransaction(Integer.valueOf(rs.getString("noOfTransaction")));
	            dto.setQuantity(rs.getBigDecimal("quantity"));
	            dto.setMaterialPrice(rs.getBigDecimal("materialPrice"));
	            return dto;
	        });
	   	} else if (summaryType.equals("WEIGH")) {
	   		String query = loadSql("queries/summary_weighOnly.sql"); // Load SQL query from file
	        Map<String, Object> params = new HashMap<>();
	        params.put("startDate", startDate);
	        params.put("endDate", endDate);
	        dtoResult = jdbcTemplate.query(query, params, (rs, rowNum) -> {
	            SummaryDTO dto = new SummaryDTO();
	            dto.setNoOfTransaction(Integer.valueOf(rs.getString("noOfTransaction")));
	            dto.setQuantity(rs.getBigDecimal("quantity"));
	            dto.setMaterialPrice(rs.getBigDecimal("materialPrice"));
	            return dto;
	        });
	   	} else if(summaryType.equals("materialInYard")) {
	   		String materialInYardQuery = loadSql("queries/summary_Material_Yard.sql");
	        Map<String, Object> materialInYardQueryParam = new HashMap<>();
	        materialInYardQueryParam.put("startDate", startDate);
	        materialInYardQueryParam.put("endDate", endDate);
	        dtoResult = jdbcTemplate.query(materialInYardQuery, materialInYardQueryParam, (rs, rowNum) -> {
	            SummaryDTO dto = new SummaryDTO();
	            dto.setMaterialName(rs.getString("materialName"));
	            dto.setQuantity(rs.getBigDecimal("quantity"));
	            return dto;
	        });
	   	}
		
        return dtoResult;
   }
   
   private List<SummaryDTO> fetchSumIncOutWeiCreationByUserList(String summaryType, String startDate, String endDate){
	   
	   	List<SummaryDTO> dtoResult = null;
	   	if(summaryType.equals("INC") || summaryType.equals("OUT"))  {
	   		String query = loadSql("queries/summary_created_user_incoming_outgoing.sql"); // Load SQL query from file
	        Map<String, Object> params = new HashMap<>();
	        params.put("summaryType", summaryType);
	        params.put("startDate", startDate);
	        params.put("endDate", endDate);
	        dtoResult = jdbcTemplate.query(query, params, (rs, rowNum) -> {
	            SummaryDTO dto = new SummaryDTO();
	            dto.setCreatedBy(rs.getString("created_by"));
	            dto.setNoOfTransaction(Integer.valueOf(rs.getString("noOfTransaction")));
	            dto.setQuantity(rs.getBigDecimal("quantity"));
	            dto.setMaterialPrice(rs.getBigDecimal("materialPrice"));
	            return dto;
	        });
	   	} else if (summaryType.equals("WEIGH")) {
	   		String query = loadSql("queries/summary_weighonly_user.sql"); // Load SQL query from file
	        Map<String, Object> params = new HashMap<>();
	        params.put("startDate", startDate);
	        params.put("endDate", endDate);
	        dtoResult = jdbcTemplate.query(query, params, (rs, rowNum) -> {
	            SummaryDTO dto = new SummaryDTO();
	            dto.setNoOfTransaction(Integer.valueOf(rs.getString("noOfTransaction")));
	            dto.setQuantity(rs.getBigDecimal("quantity"));
	            dto.setMaterialPrice(rs.getBigDecimal("materialPrice"));
	            return dto;
	        });
	   	} 
	   	
       return dtoResult;
  }
   
   private List<SummaryDTO> fetchSumIncOutClosureByUserList(String summaryType, String startDate, String endDate){
	   
	   	List<SummaryDTO> dtoResult = null;
	   	if(summaryType.equals("INC") || summaryType.equals("OUT"))  {
	   		String query = loadSql("queries/summary_modified_user_incoming_outgoing.sql"); // Load SQL query from file
	        Map<String, Object> params = new HashMap<>();
	        params.put("summaryType", summaryType);
	        params.put("startDate", startDate);
	        params.put("endDate", endDate);
	        dtoResult = jdbcTemplate.query(query, params, (rs, rowNum) -> {
	            SummaryDTO dto = new SummaryDTO();
	            dto.setModifiedBy(rs.getString("modified_by"));
	            dto.setNoOfTransaction(Integer.valueOf(rs.getString("noOfTransaction")));
	            dto.setQuantity(rs.getBigDecimal("quantity"));
	            dto.setMaterialPrice(rs.getBigDecimal("materialPrice"));
	            return dto;
	        });
	   	}  
	   	return dtoResult;
   	}
   
    private String loadSql(String fileName) {
        try {
            Resource resource = new ClassPathResource(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            reader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL file: " + fileName, e);
        }
    }
}
