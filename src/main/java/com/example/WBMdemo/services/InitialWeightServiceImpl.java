package com.example.WBMdemo.services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.WBMdemo.dto.InitialWeightDTO;
import com.example.WBMdemo.entity.InitialWeight;
import com.example.WBMdemo.repository.InitialWeightRepository;
import com.fazecast.jSerialComm.SerialPort;


@Service
public class InitialWeightServiceImpl implements InitialWeightService {

	@Autowired
	private InitialWeightRepository rawWeightRepository;
	
	@Override
	public List<InitialWeightDTO> fetchTransactionList(int order) {
		List<InitialWeight> rawWeightList = null;
		if(order==1) {
			rawWeightList = 
					rawWeightRepository.findAll(Sort.by(Sort.Direction.ASC));
		} else {
			rawWeightList = 
					rawWeightRepository.findAll(Sort.by(Sort.Direction.DESC));
		}
		return transactionByField(rawWeightList);
	}
	
	private List<InitialWeightDTO> transactionByField(List<InitialWeight> rawWeightList) {
		List<InitialWeightDTO> transactionDtoList = new ArrayList<InitialWeightDTO>();
		if(Objects.nonNull(rawWeightList.size()) && rawWeightList.size()>0) {
			for(InitialWeight transactionObj : rawWeightList) {
				InitialWeightDTO transDto = new InitialWeightDTO();
				transDto.setId(transactionObj.getRawWeightId());
//				transDto.setTransactionId(transactionObj.getTransactionId().getTransactionId());
				transDto.setWeight(transactionObj.getVehicleWeight());
				transDto.setCreatedDate(Objects.nonNull(transactionObj.getCreatedDate()) ? 
						format_date(transactionObj.getCreatedDate()) : null);
				transactionDtoList.add(transDto);
			}
			return transactionDtoList;
		} else {
			return transactionDtoList;
		}
	}
	

	@Override
	public InitialWeightDTO saveTransaction() {
		InitialWeightDTO rwDTO = new InitialWeightDTO();
		try {
			SerialPort comPort = SerialPort.getCommPorts()[2];
			System.out.println(comPort.getDescriptivePortName());
			System.out.println(comPort.getPortDescription());
			System.out.println(comPort.getSystemPortName());
//			System.out.println(comPort.get);
			comPort.openPort();
			comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
			InputStream in = comPort.getInputStream();
//			ByteArrayInputStream in = new ByteArrayInputStream("US,NT,-113.0254kg\nUS,NT,-113.0254kg\nUS,NT,-113.0254kg\n".getBytes());
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String line;
				while ((line = reader.readLine()) != null) {
//					System.out.println(line);
				    String[] numbers = line.split("[^\\d\\.]+");
				    if(!numbers[1].equals("0") && !numbers[1].equals("0.0")) {
				    	System.out.println(numbers[1]);
				    	rwDTO.setWeight(numbers[1]);
				    	break;
				    }
				}
             InitialWeight rw = new InitialWeight();
     		rw.setCreatedDate(LocalDateTime.now());
     		rw.setVehicleWeight(rwDTO.getWeight());
     		InitialWeight rwRecord = rawWeightRepository.save(rw);
     		rwDTO.setId(rwRecord.getRawWeightId());
     		rwDTO.setCreatedDate(rwRecord.getCreatedDate().toString());
             
             System.out.println(rwDTO.getId());
             System.out.println(rwDTO.getWeight());
             System.out.println(rwDTO.getCreatedDate());
             in.close();
			} catch (Exception e) { e.printStackTrace(); }
			comPort.closePort();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rwDTO;
	}

//
//		RawWeight rw = new RawWeight();
//		rw.setCreatedDate(LocalDateTime.now());
//		rw.setVehicleWeight(dto.getWeight());
//		RawWeight rwRecord = rawWeightRepository.save(rw);
//		dto.setId(rwRecord.getRawWeightId());
//		dto.setCreatedDate(rwRecord.getCreatedDate().toString());
		
	

	@Override
	public InitialWeightDTO getTransactionById(long transactionId) {
		InitialWeightDTO transDto = new InitialWeightDTO();
		InitialWeight transactionObj = rawWeightRepository.findById(transactionId).get(); 
		if(transactionObj!=null) {
			transDto.setId(transactionObj.getRawWeightId());
//			transDto.setTransactionId(transactionObj.getTransactionId().getTransactionId());
			transDto.setWeight(transactionObj.getVehicleWeight());
			transDto.setCreatedDate(Objects.nonNull(transactionObj.getCreatedDate()) ? 
					format_date(transactionObj.getCreatedDate()) : null);
		}
		return transDto;
	}

	@Override
    public InitialWeightDTO findAllByOrderByCreatedDateDesc() {
		//call the save method to update the latest weight then get the latest record
		saveTransaction();
        List<InitialWeight> rawWeightList = rawWeightRepository.findAllByOrderByCreatedDateDesc();
        InitialWeightDTO transDto = new InitialWeightDTO();
		if(Objects.nonNull(rawWeightList.size())) {
			InitialWeight transactionObj = rawWeightList.get(0);
			transDto.setId(transactionObj.getRawWeightId());
//			if(transactionObj.getTransactionId()!=null) {
//				transDto.setTransactionId(transactionObj.getTransactionId().getTransactionId());
//			}
			transDto.setWeight(transactionObj.getVehicleWeight());
			transDto.setCreatedDate(Objects.nonNull(transactionObj.getCreatedDate()) ? 
			format_date(transactionObj.getCreatedDate()) : null);
		}
		return transDto;
	}
	
	private String format_date(LocalDateTime time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		SimpleDateFormat output = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d=new Date();
		try {
			d = sdf.parse(time.toString());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String formattedTime = output.format(d);
		return formattedTime;
	}
}
