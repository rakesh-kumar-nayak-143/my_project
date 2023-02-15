package com.te.flinko.service.account;

import java.util.ArrayList;
import java.util.List;

import com.te.flinko.dto.account.AddressInformationDTO;
import com.te.flinko.dto.account.AdvanceSalaryByIdDTO;
import com.te.flinko.dto.account.AdvanceSalaryDTO;
import com.te.flinko.dto.account.AttachmentsDTO;
import com.te.flinko.dto.account.DescriptionDTO;
import com.te.flinko.dto.account.InvoiceDetailsDTO;
import com.te.flinko.dto.account.InvoiceItemDTO;
import com.te.flinko.dto.account.InvoiceItemsDTO;
import com.te.flinko.dto.account.PurchaseInvoiceDTO;
import com.te.flinko.dto.account.PurchaseInvoiceDetailsByIdDto;
import com.te.flinko.dto.account.SalesOrderDropdownDTO;

public interface AccountInvoiceService {



	InvoiceDetailsDTO invoiceDetails(InvoiceDetailsDTO invoiceDetailsDto);

	List<SalesOrderDropdownDTO> salesOrderDropdown(Long companyId);


	InvoiceItemDTO invoceItems(InvoiceItemDTO invoiceItemsDTO);

	AttachmentsDTO attachments(AttachmentsDTO attachmentsDTO);

	DescriptionDTO description(DescriptionDTO descriptionDto);

	ArrayList<AddressInformationDTO> addressInformation(Long purchaseInvoiceId, Long purchaseOrderId);

	ArrayList<PurchaseInvoiceDTO> purchaseInvoice(Long companyId);

	PurchaseInvoiceDetailsByIdDto purchaseInvoiceById(Long companyId, Long purchaseInvoiceId);

}
