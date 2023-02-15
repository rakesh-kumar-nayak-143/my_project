package com.te.flinko.dto.account;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDetailsDTO {
	private Long companyId;
	private String exciseDuty;
	private LocalDate dueDate;
	private String status;
	private BigDecimal salesCommission;
	private LocalDate invoiceDate;
	private String invoiceOwner;	
	private String dealName;
	private String contactName;
	private String subject;
	private Long vendorInfoId;
	private Long purchaseOrderId;
	private Long purchaseInvoiceId;
	
	
	
	


}
