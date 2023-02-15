package com.te.flinko.dto.account;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PurchasedOrderDisplayDTO {

	private String purchaseOrderOwner;
	private String purchaseOrderNumber;
	private String status;
	private BigDecimal totalPayableAmount;
	private Long vendorId;
	private String vendorName;
	private String contactName;

}
