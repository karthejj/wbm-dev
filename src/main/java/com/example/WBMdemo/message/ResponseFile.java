package com.example.WBMdemo.message;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.WBMdemo.entity.Material;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseFile {
	  private String name;
	  private String url;
	  private String type;
	  private long size;

}
