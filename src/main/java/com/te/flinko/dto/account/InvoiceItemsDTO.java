package com.te.flinko.dto.account;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceItemsDTO {
	private String productName;
	private String description;
	private Integer quantity;
	private BigDecimal amount;
	private BigDecimal discount;
	private BigDecimal tax;
	private BigDecimal payableAmount;
	private Long purchaseInvoiceId;
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
