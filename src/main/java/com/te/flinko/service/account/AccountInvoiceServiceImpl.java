package com.te.flinko.service.account;

import static com.te.flinko.common.account.AccountConstants.PURCHASE_ADDRESS_DETAILS_FETCHED_SUCCESSFULLY;
import static com.te.flinko.common.account.AccountConstants.PURCHASE_ADDRESS_DETAILS_NOT_EXISTS;
import static com.te.flinko.common.account.AccountConstants.PURCHASE_ATTACHMENT_DETAILS_SAVED_SUCCESSFULLY;
import static com.te.flinko.common.account.AccountConstants.PURCHASE_DESCRIPTION_DETAILS_SAVED_SUCCESSFULLY;
import static com.te.flinko.common.account.AccountConstants.PURCHASE_INVOICE_DETAILS_SAVED_SUCCESSFULLY;
import static com.te.flinko.common.hr.HrConstants.COMPANY_INFORMATION_NOT_PRESENT;
import static com.te.flinko.constant.admindept.AdminDeptConstants.PURCHASE_INVOCE_DETAILS_NOT_EXIXTS;
import static com.te.flinko.constant.admindept.AdminDeptConstants.PURCHASE_ORDER_DOES_NOT_EXISTS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.te.flinko.dto.account.AddressInformationDTO;
import com.te.flinko.dto.account.AttachmentsDTO;
import com.te.flinko.dto.account.DescriptionDTO;
import com.te.flinko.dto.account.InvoiceDetailsDTO;
import com.te.flinko.dto.account.InvoiceItemDTO;
import com.te.flinko.dto.account.InvoiceItemsDTO;
import com.te.flinko.dto.account.PurchaseInvoiceDTO;
import com.te.flinko.dto.account.PurchaseInvoiceDetailsByIdDto;
import com.te.flinko.dto.account.SalesOrderDropdownDTO;
import com.te.flinko.entity.account.CompanyPurchaseInvoice;
import com.te.flinko.entity.account.CompanyPurchaseOrder;
import com.te.flinko.entity.account.PurchaseBillingShippingAddress;
import com.te.flinko.entity.account.PurchaseInvoiceItems;
import com.te.flinko.entity.account.PurchaseOrderItems;
import com.te.flinko.entity.account.mongo.CompanyVendorInfo;
import com.te.flinko.entity.admin.CompanyInfo;
import com.te.flinko.exception.CompanyIdNotFoundException;
import com.te.flinko.exception.account.CustomExceptionForAccount;
import com.te.flinko.exception.admindept.PurchaseIdNotPresentException;
import com.te.flinko.exception.admindept.PurchaseOrderDoesNotExistsException;
import com.te.flinko.repository.account.CompanyVendorInfoRepository;
import com.te.flinko.repository.account.PurchaseBillingShippingAddressRepository;
import com.te.flinko.repository.admin.CompanyInfoRepository;
import com.te.flinko.repository.admin.CompanyPurchaseInvoiceRepository;
import com.te.flinko.repository.admin.PurchaseInvoiceItemsRepository;
import com.te.flinko.repository.admindept.CompanyPurchaseOrderRepository;
import com.te.flinko.repository.admindept.PurchaseOrderItemsRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountInvoiceServiceImpl implements AccountInvoiceService {
	@Autowired
	private CompanyInfoRepository companyInfoRepo;
	@Autowired
	private PurchaseOrderItemsRepository purchaseOrderItemsRepo;
	@Autowired
	private CompanyPurchaseInvoiceRepository companyPurchaseInvoiceRepo;
	@Autowired
	private CompanyPurchaseOrderRepository companyPurchaseOrderRepo;
	@Autowired
	private CompanyVendorInfoRepository companyVendorInfoRepo;
	@Autowired
	private PurchaseBillingShippingAddressRepository purchaseBillingShippingAddressRepo;
	@Autowired
	private PurchaseInvoiceItemsRepository purchaseInvoiceItemsRepo;

	/**
	 * this method is use to save the invoice details accept the whole object of
	 * CompanyPurchaseInvoice
	 * 
	 * @param invoiceDetailsDto object
	 * @return updated object of InvoiceDetailsDTO
	 **/
	@Override
	@Transactional
	public InvoiceDetailsDTO invoiceDetails(InvoiceDetailsDTO invoiceDetailsDto) {
		InvoiceDetailsDTO invoiceDetailDTO = new InvoiceDetailsDTO();
		CompanyInfo companyInfo = companyInfoRepo.findById(invoiceDetailsDto.getCompanyId())
				.orElseThrow(() -> new CompanyIdNotFoundException(COMPANY_INFORMATION_NOT_PRESENT));
		log.info("company information verified successfully");
		List<CompanyPurchaseOrder> companyPurchaseOrderInfo = companyPurchaseOrderRepo
				.findByVendorIdAndSubject(invoiceDetailsDto.getVendorInfoId(), invoiceDetailsDto.getSubject());
		if (companyPurchaseOrderInfo == null || companyPurchaseOrderInfo.isEmpty()) {
			log.error(PURCHASE_ORDER_DOES_NOT_EXISTS);
			throw new PurchaseOrderDoesNotExistsException(PURCHASE_ORDER_DOES_NOT_EXISTS);
		}

		CompanyPurchaseInvoice companyPurchaseInvoice = new CompanyPurchaseInvoice();
		BeanUtils.copyProperties(invoiceDetailsDto, companyPurchaseInvoice);
		companyPurchaseInvoice.setCompanyInfo(companyInfo);
		companyPurchaseInvoice.setCompanyPurchaseOrder(companyPurchaseOrderInfo.get(0));
		CompanyPurchaseInvoice companyPurchaseInvoiceDetails = companyPurchaseInvoiceRepo.save(companyPurchaseInvoice);
		BeanUtils.copyProperties(companyPurchaseInvoiceDetails, invoiceDetailDTO);
		invoiceDetailDTO.setPurchaseOrderId(companyPurchaseOrderInfo.get(0).getPurchaseOrderId());
		invoiceDetailDTO.setPurchaseInvoiceId(companyPurchaseInvoiceDetails.getPurchaseInvoiceId());
		invoiceDetailDTO.setCompanyId(companyInfo.getCompanyId());

		log.info(PURCHASE_INVOICE_DETAILS_SAVED_SUCCESSFULLY);
		return invoiceDetailDTO;
	}

	/**
	 * this method is use to save the address Information accept the object of
	 * PurchaseBillingShippingAddress
	 * 
	 * @param AddressInformationDTO objects list
	 * @return updated objects list of AddressInformationDTO
	 **/
	@Transactional
	@Override
	public ArrayList<AddressInformationDTO> addressInformation(Long purchaseInvoiceId, Long purchaseOrderId) {
		CompanyPurchaseInvoice purchaseInvoice = companyPurchaseInvoiceRepo.findById(purchaseInvoiceId)
				.orElseThrow(() -> new CustomExceptionForAccount(PURCHASE_INVOCE_DETAILS_NOT_EXIXTS));

		List<PurchaseBillingShippingAddress> billingAddress = purchaseBillingShippingAddressRepo
				.findByCompanyPurchaseOrderPurchaseOrderId(purchaseOrderId);
		if (billingAddress == null || billingAddress.isEmpty()) {
			throw new CustomExceptionForAccount(PURCHASE_ADDRESS_DETAILS_NOT_EXISTS);
		}
		ArrayList<AddressInformationDTO> address = new ArrayList<>();
		for (PurchaseBillingShippingAddress purchaseBillingShippingAddress : billingAddress) {
			AddressInformationDTO addressInformationDTO = new AddressInformationDTO();
			BeanUtils.copyProperties(purchaseBillingShippingAddress, addressInformationDTO);
			addressInformationDTO.setPurchaseOrderId(purchaseOrderId);
			addressInformationDTO.setPurchaseInvoiceId(purchaseInvoice.getPurchaseInvoiceId());
			address.add(addressInformationDTO);
		}
		log.info(PURCHASE_ADDRESS_DETAILS_FETCHED_SUCCESSFULLY);
		return address;
	}

	/**
	 * this method is use to save the attachments details accept the object of
	 * companyPurchaseInvoice
	 * 
	 * @param invoiceDetailsDto object
	 * @return updated object of InvoiceDetailsDTO
	 **/
	@Transactional
	@Override
	public AttachmentsDTO attachments(AttachmentsDTO attachmentsDTO) {
		CompanyPurchaseInvoice purchaseInvoice = companyPurchaseInvoiceRepo
				.findById(attachmentsDTO.getPurchaseInvoiceId())
				.orElseThrow(() -> new PurchaseOrderDoesNotExistsException(PURCHASE_INVOCE_DETAILS_NOT_EXIXTS));
		log.info("purchase invoice details fetched successfully");
		purchaseInvoice.setAttachment(attachmentsDTO.getAttachment());
		CompanyPurchaseInvoice companyPurchaseInvoice = companyPurchaseInvoiceRepo.save(purchaseInvoice);
		AttachmentsDTO attachmentsDtos = new AttachmentsDTO();
		attachmentsDtos.setPurchaseInvoiceId(companyPurchaseInvoice.getPurchaseInvoiceId());
		attachmentsDtos.setAttachment(purchaseInvoice.getAttachment());
		log.info(PURCHASE_ATTACHMENT_DETAILS_SAVED_SUCCESSFULLY);
		return attachmentsDtos;
	}

	/**
	 * this method created for getting all employee purchase order info and vender
	 * info which is sales order drop down in specific company
	 * 
	 * @param companyId
	 * @return list of SalesOrderDropdownDTO
	 **/
	@Override
	public List<SalesOrderDropdownDTO> salesOrderDropdown(Long companyId) {
		List<CompanyPurchaseOrder> purchaseOrder = companyPurchaseOrderRepo
				.findByCompanyInfoCompanyId(companyId);
//		List<CompanyPurchaseOrder> purchaseOrder = companyPurchaseOrderRepo.findByCompanyIdQuery(companyId);
		List<CompanyVendorInfo> vendorDetails = companyVendorInfoRepo.findByCompanyId(companyId);
		return purchaseOrder.stream().map(s -> {
			SalesOrderDropdownDTO dropdown = new SalesOrderDropdownDTO();

			for (CompanyVendorInfo vendorInfo : vendorDetails) {

				if (Objects.equals(s.getVendorId(), vendorInfo.getVendorInfoId())) {
					dropdown.setSubject(s.getSubject());

					dropdown.setContactPersons(vendorInfo.getContactPersons());
					dropdown.setVendorInfoId(vendorInfo.getVendorInfoId());
					dropdown.setVendorName(vendorInfo.getVendorName());
					dropdown.setVendorInfoId(vendorInfo.getVendorInfoId());
				}

			}

			return dropdown;
			// here we apply filter to does not collect null object
		}).filter(x -> x != null && x.getVendorInfoId() != null).collect(Collectors.toList());

	}

	/**
	 * this method is use to save the description details accept the object of
	 * companyPurchaseInvoice
	 * 
	 * @param InvoiceItemsDTO objects list
	 * @return updated object of DescriptionDTO
	 **/
	@Override
	@Transactional
	public DescriptionDTO description(DescriptionDTO descriptionDto) {
		CompanyPurchaseInvoice purchaseInvoice = companyPurchaseInvoiceRepo
				.findById(descriptionDto.getPurchaseInvoiceId())
				.orElseThrow(() -> new PurchaseIdNotPresentException(PURCHASE_INVOCE_DETAILS_NOT_EXIXTS));
		purchaseInvoice.setDescription(descriptionDto.getDescription());
		CompanyPurchaseInvoice companyPurchaseInvoice = companyPurchaseInvoiceRepo.save(purchaseInvoice);
		DescriptionDTO descriptionsDto = new DescriptionDTO();
		descriptionsDto.setDescription(companyPurchaseInvoice.getDescription());
		descriptionsDto.setPurchaseInvoiceId(companyPurchaseInvoice.getPurchaseInvoiceId());
		log.info(PURCHASE_DESCRIPTION_DETAILS_SAVED_SUCCESSFULLY);
		return descriptionsDto;

	}

	/**
	 * this method is use to save the invoceItems details accept the object of
	 * companyPurchaseInvoice
	 * 
	 * @param InvoiceItemsDTO objects list
	 * @return updated objects list of InvoiceItemsDTO
	 **/
	@Override
	@Transactional

	public InvoiceItemDTO invoceItems(InvoiceItemDTO invoiceItemsDTO) {
		List<InvoiceItemsDTO> invoiceList = new ArrayList<>();

		List<InvoiceItemsDTO> invoiceDetailsDTOs = invoiceItemsDTO.getInvoiceDetailsDTOs();
		CompanyPurchaseInvoice purchaseInvoice = companyPurchaseInvoiceRepo
				.findById(invoiceItemsDTO.getPurchaseInvoiceId())
				.orElseThrow(() -> new PurchaseIdNotPresentException(PURCHASE_INVOCE_DETAILS_NOT_EXIXTS));
		BeanUtils.copyProperties(invoiceItemsDTO, purchaseInvoice);
		CompanyPurchaseInvoice save = companyPurchaseInvoiceRepo.save(purchaseInvoice);
		InvoiceItemDTO invoiceItemDTO = new InvoiceItemDTO();
		BeanUtils.copyProperties(save, invoiceItemDTO);
		List<PurchaseOrderItems> purchaseOrderId = purchaseOrderItemsRepo
				.findByCompanyPurchaseOrderPurchaseOrderId(invoiceItemsDTO.getPurchaseOrderId());
		for (InvoiceItemsDTO invoiceItemsDetailsDTO : invoiceDetailsDTOs) {

			PurchaseInvoiceItems purchaseInvoiceItems = new PurchaseInvoiceItems();
			BeanUtils.copyProperties(invoiceItemsDetailsDTO, purchaseInvoiceItems);
			purchaseInvoiceItems.setCompanyPurchaseInvoice(purchaseInvoice);
			purchaseInvoiceItems.setPurchaseOrderItems(purchaseOrderId.get(0));
			PurchaseInvoiceItems invoiceItems = purchaseInvoiceItemsRepo.save(purchaseInvoiceItems);
			InvoiceItemsDTO invoiceItemsDTOs = new InvoiceItemsDTO();
			BeanUtils.copyProperties(invoiceItems, invoiceItemsDTOs);
			invoiceList.add(new InvoiceItemsDTO(invoiceItems.getPurchaseOrderItems().getProductName(),invoiceItems.getDescription(), invoiceItems.getQuantity(),
					invoiceItems.getAmount(), invoiceItems.getDiscount(), invoiceItems.getTax(),
					invoiceItems.getPayableAmount(), purchaseInvoice.getPurchaseInvoiceId()));

		}
		invoiceItemDTO.setInvoiceDetailsDTOs(invoiceList);
		invoiceItemDTO.setPurchaseOrderId(save.getCompanyPurchaseOrder().getPurchaseOrderId());
		invoiceItemDTO.setCompanyId(save.getCompanyInfo().getCompanyId());
		log.info(PURCHASE_INVOICE_DETAILS_SAVED_SUCCESSFULLY);
		return invoiceItemDTO;
	}

	@Override
	public ArrayList<PurchaseInvoiceDTO> purchaseInvoice(Long companyId) {
		List<CompanyPurchaseInvoice> invoiceDetialList = companyPurchaseInvoiceRepo
				.findByCompanyInfoCompanyId(companyId);
		List<Long> venderIds = invoiceDetialList.stream().map(a -> a.getCompanyPurchaseOrder().getVendorId())
				.collect(Collectors.toList());
		List<CompanyVendorInfo> vendorInfoList = companyVendorInfoRepo.findByVendorInfoIdInAndAndCompanyId(venderIds,
				companyId);
		ArrayList<PurchaseInvoiceDTO> purchaseInvoiceList = new ArrayList<>();

		for (CompanyPurchaseInvoice companyPurchaseInvoice : invoiceDetialList) {
			PurchaseInvoiceDTO purchaseInvoiceDTO = new PurchaseInvoiceDTO();
			purchaseInvoiceDTO.setInvoiceDate(companyPurchaseInvoice.getInvoiceDate());
			purchaseInvoiceDTO.setInvoiceOwner(companyPurchaseInvoice.getCreatedBy());
			purchaseInvoiceDTO.setPurchaseInvoiceId(companyPurchaseInvoice.getPurchaseInvoiceId());
			purchaseInvoiceDTO.setStatus(companyPurchaseInvoice.getStatus());
			purchaseInvoiceDTO.setSubject(companyPurchaseInvoice.getCompanyPurchaseOrder().getSubject());
			for (CompanyVendorInfo vendorDetails : vendorInfoList) {
				if (companyPurchaseInvoice.getCompanyPurchaseOrder().getVendorId()
						.equals(vendorDetails.getVendorInfoId())) {
					purchaseInvoiceDTO.setVendorName(vendorDetails.getVendorName());
				}

			}

			purchaseInvoiceList.add(purchaseInvoiceDTO);
		}
		return purchaseInvoiceList;
	}

	@Override
	public PurchaseInvoiceDetailsByIdDto purchaseInvoiceById(Long companyId, Long purchaseInvoiceId) {

		List<CompanyPurchaseInvoice> purchaseInfo = companyPurchaseInvoiceRepo
				.findByCompanyInfoCompanyIdAndPurchaseInvoiceId(companyId, purchaseInvoiceId);
		if (purchaseInfo == null || purchaseInfo.isEmpty()) {
			throw new PurchaseIdNotPresentException(PURCHASE_INVOCE_DETAILS_NOT_EXIXTS);
		}
		CompanyPurchaseInvoice companyPurchaseInvoice = purchaseInfo.get(0);
		List<CompanyVendorInfo> vendorInfoList = companyVendorInfoRepo.findByVendorInfoIdAndAndCompanyId(
				companyPurchaseInvoice.getCompanyPurchaseOrder().getVendorId(), companyId);
		if (vendorInfoList == null || vendorInfoList.isEmpty()) {
			throw new CustomExceptionForAccount("Vendor details not found");
		}
		PurchaseInvoiceDetailsByIdDto purchaseInvoiceDetailsByIdDto = new PurchaseInvoiceDetailsByIdDto();
		purchaseInvoiceDetailsByIdDto.setAttachment(companyPurchaseInvoice.getAttachment());
		purchaseInvoiceDetailsByIdDto.setDescription(companyPurchaseInvoice.getDescription());

		InvoiceDetailsDTO invoiceDetailDTO = new InvoiceDetailsDTO();
		BeanUtils.copyProperties(companyPurchaseInvoice, invoiceDetailDTO);
		invoiceDetailDTO.setInvoiceOwner(companyPurchaseInvoice.getCreatedBy() + "");
		invoiceDetailDTO.setDealName(vendorInfoList.get(0).getVendorName());
		invoiceDetailDTO.setContactName(vendorInfoList.get(0).getContactPersons().get(0).getContactPersonName());
		invoiceDetailDTO.setSubject(companyPurchaseInvoice.getCompanyPurchaseOrder().getSubject());
		invoiceDetailDTO.setVendorInfoId(vendorInfoList.get(0).getVendorInfoId());
		invoiceDetailDTO.setPurchaseOrderId(companyPurchaseInvoice.getCompanyPurchaseOrder().getPurchaseOrderId());
		BeanUtils.copyProperties(invoiceDetailDTO, invoiceDetailDTO);
		ArrayList<InvoiceDetailsDTO> invoiceDetailList = new ArrayList<>();
		invoiceDetailList.add(invoiceDetailDTO);
		purchaseInvoiceDetailsByIdDto.setInvoiceDetails(invoiceDetailList);
		List<PurchaseBillingShippingAddress> addressInfo = purchaseInfo.get(0).getCompanyPurchaseOrder()
				.getPurchaseBillingShippingAddressList();
		if (addressInfo == null || addressInfo.isEmpty()) {
			throw new CustomExceptionForAccount("billing adress not found");
		}

		ArrayList<AddressInformationDTO> addressInfoList = new ArrayList<>();
		for (PurchaseBillingShippingAddress purchaseBillingShippingAddress : addressInfo) {
			AddressInformationDTO addressInformationDTO = new AddressInformationDTO();
			BeanUtils.copyProperties(purchaseBillingShippingAddress, addressInformationDTO);
			addressInfoList.add(addressInformationDTO);
		}
		purchaseInvoiceDetailsByIdDto.setAddressInformation(addressInfoList);
		ArrayList<InvoiceItemsDTO> invoiceList = new ArrayList<>();
		List<PurchaseInvoiceItems> purchaseInvoiceList = purchaseInfo.get(0).getPurchaseInvoiceItemsList();
		if (purchaseInvoiceList == null || purchaseInvoiceList.isEmpty()) {
			throw new CustomExceptionForAccount("invoice details not found");
		}
		for (PurchaseInvoiceItems purchaseInvoiceItems : purchaseInvoiceList) {
			InvoiceItemsDTO invoiceItemsDTO = new InvoiceItemsDTO();
			BeanUtils.copyProperties(purchaseInvoiceItems, invoiceItemsDTO);
			invoiceItemsDTO.setProductName(purchaseInvoiceItems.getPurchaseOrderItems().getProductName());
			invoiceList.add(invoiceItemsDTO);
			purchaseInvoiceDetailsByIdDto.setInvoiceItems(invoiceList);

		}
		log.info(PURCHASE_ADDRESS_DETAILS_FETCHED_SUCCESSFULLY);
		return purchaseInvoiceDetailsByIdDto;
	}
}
