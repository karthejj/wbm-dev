package com.example.WBMdemo.errors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@ResponseStatus
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler{

	@ExceptionHandler(VehicleNotFoundException.class)
	public ResponseEntity<ErrorMessage> departmentNotFoundException(VehicleNotFoundException exception,
			WebRequest request){
		
		ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage());
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message); 
		
	}
	
	@ExceptionHandler(DuplicateVehicleException.class)
	public ResponseEntity<ErrorMessage> duplicateVehicleFoundException(DuplicateVehicleException exception,
			WebRequest request) {
		ErrorMessage message = new ErrorMessage(HttpStatus.FOUND, exception.getMessage());
		return ResponseEntity.status(HttpStatus.FOUND).body(message);
	}
	
	@ExceptionHandler(DuplicateCustomerException.class)
	public ResponseEntity<ErrorMessage> duplicateCustomerFoundException(DuplicateCustomerException exception,
			WebRequest request) {
		ErrorMessage message = new ErrorMessage(HttpStatus.FOUND, exception.getMessage());
		return ResponseEntity.status(HttpStatus.FOUND).body(message);
	}
	
	@ExceptionHandler(DuplicateMaterialException.class)
	public ResponseEntity<ErrorMessage> duplicateMaterialFoundException(DuplicateMaterialException exception,
			WebRequest request) {
		ErrorMessage message = new ErrorMessage(HttpStatus.FOUND, exception.getMessage());
		return ResponseEntity.status(HttpStatus.FOUND).body(message);
	}
	
	@ExceptionHandler(TransactionNotFoundException.class)
	public ResponseEntity<ErrorMessage> transactionNotFoundException(TransactionNotFoundException exception,
			WebRequest request){
		
		ErrorMessage message = new ErrorMessage(HttpStatus.NOT_FOUND, exception.getMessage());
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message); 
		
	}
}

