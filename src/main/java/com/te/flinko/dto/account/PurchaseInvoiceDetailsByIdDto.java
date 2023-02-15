package com.te.flinko.dto.account;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseInvoiceDetailsByIdDto {
	private String attachment;
	private String description;
	List<InvoiceDetailsDTO> invoiceDetails;
	List<AddressInformationDTO> addressInformation;
	List<InvoiceItemsDTO> invoiceItems;
	

}
