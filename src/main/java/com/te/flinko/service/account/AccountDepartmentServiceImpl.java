package com.te.flinko.service.account;

import com.google.common.collect.Lists;
import com.te.flinko.dto.admindept.PurchaseOrderItemsDTO;
import com.te.flinko.entity.account.CompanyPurchaseOrder;
import com.te.flinko.entity.account.CompanySalesOrder;
import com.te.flinko.entity.account.CompanyWorkOrder;
import com.te.flinko.entity.account.PurchaseBillingShippingAddress;
import com.te.flinko.entity.account.PurchaseOrderItems;
import com.te.flinko.entity.account.SalesBillingShippingAddress;
import com.te.flinko.entity.account.SalesOrderItems;
import com.te.flinko.entity.account.WorkOrderResources;
import com.te.flinko.dto.account.*;
import com.te.flinko.entity.account.*;
import com.te.flinko.entity.account.mongo.CompanyVendorInfo;
import com.te.flinko.entity.employee.EmployeePersonalInfo;
import com.te.flinko.entity.sales.CompanyClientInfo;
import com.te.flinko.exception.DataNotFoundException;
import com.te.flinko.exception.account.SubjectNotUniqueException;
import com.te.flinko.repository.account.ClientContactPersonDetailsRepository;
import com.te.flinko.repository.account.CompanyVendorInfoRepository;
import com.te.flinko.repository.account.CompanyWorkOrderRepository;
import com.te.flinko.repository.account.ContactPersonRepository;
import com.te.flinko.repository.account.PurchaseBillingShippingAddressRepository;
import com.te.flinko.repository.account.SalesBillingShippingAddressRepository;
import com.te.flinko.repository.admin.CompanyInfoRepository;
import com.te.flinko.repository.admin.CompanyStockGroupRepository;
import com.te.flinko.repository.admindept.CompanyPurchaseOrderRepository;
import com.te.flinko.repository.admindept.CompanySalesOrderRepository;
import com.te.flinko.repository.employee.EmployeePersonalInfoRepository;
import com.te.flinko.repository.sales.CompanyClientInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.te.flinko.common.account.AccountDepartmentConstants.*;
import static com.te.flinko.dto.account.AddressType.BILLING;
import static com.te.flinko.dto.account.AddressType.SHIPPING;

@Slf4j
@RequiredArgsConstructor
@Service
public class AccountDepartmentServiceImpl implements AccountDepartmentService {

	private final CompanyPurchaseOrderRepository companyPurchaseOrderRepository;
	private final CompanySalesOrderRepository companySalesOrderRepository;
	private final CompanyStockGroupRepository companyStockGroupRepository;
	private final PurchaseBillingShippingAddressRepository purchaseBillingShippingAddressRepository;
	private final SalesBillingShippingAddressRepository salesBillingShippingAddressRepository;
	private final CompanyInfoRepository companyInfoRepository;
	private final CompanyClientInfoRepository companyClientInfoRepository;
	private final CompanyVendorInfoRepository companyVendorInfoRepository;
	private final ClientContactPersonDetailsRepository clientContactPersonDetailsRepository;
	private final CompanyWorkOrderRepository companyWorkOrderRepository;
	private final CompanyClientInfoRepository clientInfoRepository;
	private final EmployeePersonalInfoRepository employeePersonalInfoRepository;
	private final ContactPersonRepository contactPersonRepository;

	@Transactional
	@Override
	public Long createPurchaseOrder(CompanyPurchaseOrderDTO companyPurchaseOrderDTO, Long companyId) {
		log.info("createPurchaseOrder method execution start, finding if the subject is unique");
		if (companyPurchaseOrderRepository.findBySubject(companyPurchaseOrderDTO.getSubject()).size() > 0)
			throw new SubjectNotUniqueException(NOT_UNIQUE_SUBJECT);
		log.info(
				"createPurchaseOrder method, checking existence of stock group, company, and vendor data, saving the data and returning");
		return companyPurchaseOrderRepository.saveAndFlush(CompanyPurchaseOrder.builder()
				.companyStockGroup(companyStockGroupRepository.findById(companyPurchaseOrderDTO.getStockGroupId())
						.orElseThrow(() -> new DataNotFoundException(STOCK_GROUP_NOT_FOUND)))
				.companyInfo(companyInfoRepository.findByCompanyId(companyId)
						.orElseThrow(() -> new DataNotFoundException(COMPANY_DATA_NOT_FOUND)))
				.vendorContactPersonId(
						String.valueOf(companyVendorInfoRepository.findById(companyPurchaseOrderDTO.getVendorId())
								.orElseThrow(() -> new DataNotFoundException(VENDOR_DATA_NOT_FOUND)).getVendorInfoId()))
				.purchaseOrderNumber(companyPurchaseOrderDTO.getPurchaseOrderNumber())
				.type(companyPurchaseOrderDTO.getProductType().name()).subject(companyPurchaseOrderDTO.getSubject())
				.vendorId(companyPurchaseOrderDTO.getVendorId())
				.requisitionNumber(companyPurchaseOrderDTO.getRequisitionNumber())
				.trackingNumber(companyPurchaseOrderDTO.getTrackingNumber())
				.dueDate(companyPurchaseOrderDTO.getDueDate()).carrier(companyPurchaseOrderDTO.getCarrier())
				.exciseDuty(companyPurchaseOrderDTO.getExciseDuty())
				.salesCommission(companyPurchaseOrderDTO.getSalesCommission())
				.status(companyPurchaseOrderDTO.getStatus()).poDate(companyPurchaseOrderDTO.getPurchaseOrderDate())
				.build()).getPurchaseOrderId();
	}

	@Transactional
	@Override
	public Long createSalesOrder(CompanySalesOrderDTO companySalesOrderDTO, Long companyId) {
		log.info("createSalesOrder method execution start, finding if the subject is unique");
		if (companySalesOrderRepository.findBySubject(companySalesOrderDTO.getSubject()).size() > 0)
			throw new SubjectNotUniqueException(NOT_UNIQUE_SUBJECT);
		log.info(
				"createSalesOrder method, checking existence of stock group, company, company client, client contact person and vendor data, saving the data and returning");
		return companySalesOrderRepository.saveAndFlush(CompanySalesOrder.builder()
				.companyStockGroup(companyStockGroupRepository.findById(companySalesOrderDTO.getStockGroupId())
						.orElseThrow(() -> new DataNotFoundException(STOCK_GROUP_NOT_FOUND)))
				.companyInfo(companyInfoRepository.findByCompanyId(companyId)
						.orElseThrow(() -> new DataNotFoundException(COMPANY_DATA_NOT_FOUND)))
				.companyClientInfo(companyClientInfoRepository.findById(companySalesOrderDTO.getCompanyClientInfoID())
						.orElseThrow(() -> new DataNotFoundException(COMPANY_CLIENT_DATA_NOT_FOUND)))
				.clientContactPersonDetails(
						clientContactPersonDetailsRepository.findById(companySalesOrderDTO.getClientContactPersonID())
								.orElseThrow(() -> new DataNotFoundException(COMPANY_CONTACT_PERSON_DATA_NOT_FOUND)))
				.type(companySalesOrderDTO.getProductType().name()).subject(companySalesOrderDTO.getSubject())
				.purchaseOrder(companySalesOrderDTO.getPurchaseOrder())
				.customerNumber(companySalesOrderDTO.getCustomerNumber()).dueDate(companySalesOrderDTO.getDueDate())
				.pending(companySalesOrderDTO.getPending()).exciseDuty(companySalesOrderDTO.getExciseDuty())
				.carrier(companySalesOrderDTO.getExciseDuty()).status(companySalesOrderDTO.getStatus())
				.salesCommission(companySalesOrderDTO.getSalesCommission()).build()).getSalesOrderId();
	}

	@Transactional
	@Override
	public Long addPurchaseBillingShippingAddress(BillingShippingAddressDTO billingShippingAddressDTO,
			Long purchaseOrderId) {
		log.info(
				"addPurchaseBillingShippingAddress method execution start, retrieving billing and shipping addresses from the dto");
		BillingAddressDTO billingAddressDTO = billingShippingAddressDTO.getBillingAddress();
		ShippingAddressDTO shippingAddressDTO = billingShippingAddressDTO.getShippingAddress();
		log.debug(
				"addPurchaseBillingShippingAddress method, deleting previously saved billing and shipping addresses from the database");
		purchaseBillingShippingAddressRepository.deleteByCompanyPurchaseOrderPurchaseOrderId(purchaseOrderId);
		log.info(
				"addPurchaseBillingShippingAddress method, checking the existence of company purchase order data in the database");
		CompanyPurchaseOrder companyPurchaseOrder = companyPurchaseOrderRepository.findById(purchaseOrderId)
				.orElseThrow(() -> new DataNotFoundException(PURCHASE_ORDER_DATA_NOT_FOUND));
		log.info("addPurchaseBillingShippingAddress method, setting the required data");
		companyPurchaseOrder.setPurchaseBillingShippingAddressList(Lists.newArrayList(
				PurchaseBillingShippingAddress.builder().addressType(BILLING.name()).city(billingAddressDTO.getCity())
						.state(billingAddressDTO.getState()).addressDetails(billingAddressDTO.getAddressDetails())
						.pinCode(billingAddressDTO.getPinCode()).companyPurchaseOrder(companyPurchaseOrder).build(),
				PurchaseBillingShippingAddress.builder().addressType(SHIPPING.name()).city(shippingAddressDTO.getCity())
						.state(shippingAddressDTO.getState()).addressDetails(shippingAddressDTO.getAddressDetails())
						.pinCode(shippingAddressDTO.getPinCode()).companyPurchaseOrder(companyPurchaseOrder).build()));
		log.info("addPurchaseBillingShippingAddress method, saving and returning the data");
		return companyPurchaseOrderRepository.save(companyPurchaseOrder).getPurchaseOrderId();
	}

	@Transactional
	@Override
	public Long addSalesBillingShippingAddress(BillingShippingAddressDTO billingShippingAddressDTO, Long salesOrderId) {
		log.info(
				"addSalesBillingShippingAddress method execution start, retrieving billing and shipping addresses from the dto");
		BillingAddressDTO billingAddressDTO = billingShippingAddressDTO.getBillingAddress();
		ShippingAddressDTO shippingAddressDTO = billingShippingAddressDTO.getShippingAddress();
		log.debug(
				"addSalesBillingShippingAddress method, deleting previously saved billing and shipping addresses from the database");
		salesBillingShippingAddressRepository.deleteByCompanySalesOrderSalesOrderId(salesOrderId);
		log.info(
				"addSalesBillingShippingAddress method, checking the existence of company sales order data in the database");
		CompanySalesOrder companySalesOrder = companySalesOrderRepository.findById(salesOrderId)
				.orElseThrow(() -> new DataNotFoundException(SALES_ORDER_DATA_NOT_FOUND));
		log.info("addSalesBillingShippingAddress method, setting the required data");
		companySalesOrder.setSalesBillingShippingAddressList(Lists.newArrayList(
				SalesBillingShippingAddress.builder().addressType(BILLING.name()).city(billingAddressDTO.getCity())
						.state(billingAddressDTO.getState()).addressDetails(billingAddressDTO.getAddressDetails())
						.pinCode(billingAddressDTO.getPinCode()).companySalesOrder(companySalesOrder).build(),
				SalesBillingShippingAddress.builder().addressType(SHIPPING.name()).city(shippingAddressDTO.getCity())
						.state(shippingAddressDTO.getState()).addressDetails(shippingAddressDTO.getAddressDetails())
						.pinCode(shippingAddressDTO.getPinCode()).companySalesOrder(companySalesOrder).build()));
		log.info("addSalesBillingShippingAddress method, saving and returning the data");
		return companySalesOrderRepository.save(companySalesOrder).getSalesOrderId();
	}

	@Transactional
	@Override
	public Long addPurchasedItems(PurchaseItemsDTO purchaseItemsDTO, Long purchaseOrderId) {
		log.info("addPurchasedItems method, checking the existence of company purchase order data in the database");
		CompanyPurchaseOrder companyPurchaseOrder = companyPurchaseOrderRepository.findById(purchaseOrderId)
				.orElseThrow(() -> new DataNotFoundException(PURCHASE_ORDER_DATA_NOT_FOUND));
		log.info(
				"addPurchasedItems method, adding and setting the all the purchase order items to the purchase order items list");
		purchaseItemsDTO.getPurchaseItems().forEach(purchaseItemDTO -> {
			companyPurchaseOrder.getPurchaseOrderItemsList().add(PurchaseOrderItems.builder()
					.productName(purchaseItemDTO.getProductName()).quantity(purchaseItemDTO.getQuantity())
					.amount(purchaseItemDTO.getAmount()).discount(purchaseItemDTO.getDiscount())
					.tax(purchaseItemDTO.getTax()).payableAmount(purchaseItemDTO.getPayableAmount())
					.description(purchaseItemDTO.getDescription()).companyPurchaseOrder(companyPurchaseOrder).build());
		});
		log.info("addPurchasedItems method, saving and returning the required data");
		return companyPurchaseOrderRepository.save(companyPurchaseOrder).getPurchaseOrderId();
	}

	@Transactional
	@Override
	public Long addOrderedItems(SalesItemsDTO salesItemsDTO, Long salesOrderId) {
		log.info("addOrderedItems method, checking the existence of company sales order data in the database");
		CompanySalesOrder companySalesOrder = companySalesOrderRepository.findById(salesOrderId)
				.orElseThrow(() -> new DataNotFoundException(SALES_ORDER_DATA_NOT_FOUND));
		log.info(
				"addOrderedItems method, adding and setting the all the sales order items to the sales order items list");
		salesItemsDTO.getSalesItems().forEach(salesItemDTO -> {
			companySalesOrder.getSalesOrderItemsList()
					.add(SalesOrderItems.builder().productName(salesItemDTO.getProductName())
							.quantity(salesItemDTO.getQuantity()).amount(salesItemDTO.getAmount())
							.discount(salesItemDTO.getDiscount()).tax(salesItemDTO.getTax())
							.receivableAmount(salesItemDTO.getPayableAmount())
							.description(salesItemDTO.getDescription()).companySalesOrder(companySalesOrder).build());
		});
		log.info("addOrderedItems method, saving and returning the required data");
		return companySalesOrderRepository.save(companySalesOrder).getSalesOrderId();
	}

	@Transactional
	@Override
	public Long updatePurchaseOrder(CompanyPurchaseOrderDTO companyPurchaseOrderDTO, Long purchaseOrderId) {
		log.info("updatePurchaseOrder method, execution start");
		log.info(
				"updatePurchaseOrder method, checking the existence of purchase order, stock group, vendor data in the database and setting the required data");
		CompanyPurchaseOrder companyPurchaseOrder = companyPurchaseOrderRepository.findById(purchaseOrderId)
				.orElseThrow(() -> new DataNotFoundException(PURCHASE_ORDER_DATA_NOT_FOUND));
		companyPurchaseOrder
				.setCompanyStockGroup(companyStockGroupRepository.findById(companyPurchaseOrderDTO.getStockGroupId())
						.orElseThrow(() -> new DataNotFoundException(STOCK_GROUP_NOT_FOUND)));
		companyPurchaseOrder.setVendorContactPersonId(
				String.valueOf(companyVendorInfoRepository.findById(companyPurchaseOrderDTO.getVendorId())
						.orElseThrow(() -> new DataNotFoundException(VENDOR_DATA_NOT_FOUND)).getVendorInfoId()));
		companyPurchaseOrder.setPurchaseOrderNumber(companyPurchaseOrderDTO.getPurchaseOrderNumber());
		companyPurchaseOrder.setType(companyPurchaseOrderDTO.getProductType().name());
		// TODO: logic to save the subject
		// companyPurchaseOrder.setSubject(companyPurchaseOrderDTO.getSubject());
		companyPurchaseOrder.setVendorId(companyPurchaseOrderDTO.getVendorId());
		companyPurchaseOrder.setRequisitionNumber(companyPurchaseOrderDTO.getRequisitionNumber());
		companyPurchaseOrder.setTrackingNumber(companyPurchaseOrderDTO.getTrackingNumber());
		companyPurchaseOrder.setDueDate(companyPurchaseOrderDTO.getDueDate());
		companyPurchaseOrder.setCarrier(companyPurchaseOrderDTO.getCarrier());
		companyPurchaseOrder.setExciseDuty(companyPurchaseOrderDTO.getExciseDuty());
		companyPurchaseOrder.setSalesCommission(companyPurchaseOrderDTO.getSalesCommission());
		companyPurchaseOrder.setStatus(companyPurchaseOrderDTO.getStatus());
		companyPurchaseOrder.setPoDate(companyPurchaseOrderDTO.getPurchaseOrderDate());
		log.info("updatePurchaseOrder method, updating and returning the data");
		return companyPurchaseOrderRepository.save(companyPurchaseOrder).getPurchaseOrderId();
	}

	@Transactional
	@Override
	public Long updateSalesOrder(CompanySalesOrderDTO companySalesOrderDTO, Long salesOrderId) {
		log.info("updateSalesOrder method, execution start");
		log.info(
				"updateSalesOrder method, checking the existence of sales order, stock group, company client, contact person data in the database and setting the required data");
		CompanySalesOrder companySalesOrder = companySalesOrderRepository.findById(salesOrderId)
				.orElseThrow(() -> new DataNotFoundException(SALES_ORDER_DATA_NOT_FOUND));
		companySalesOrder
				.setCompanyStockGroup(companyStockGroupRepository.findById(companySalesOrderDTO.getStockGroupId())
						.orElseThrow(() -> new DataNotFoundException(STOCK_GROUP_NOT_FOUND)));
		companySalesOrder.setCompanyClientInfo(
				companyClientInfoRepository.findById(companySalesOrderDTO.getCompanyClientInfoID())
						.orElseThrow(() -> new DataNotFoundException(COMPANY_CLIENT_DATA_NOT_FOUND)));
		companySalesOrder.setClientContactPersonDetails(
				clientContactPersonDetailsRepository.findById(companySalesOrderDTO.getClientContactPersonID())
						.orElseThrow(() -> new DataNotFoundException(COMPANY_CONTACT_PERSON_DATA_NOT_FOUND)));
		companySalesOrder.setType(companySalesOrderDTO.getProductType().name());
		// TODO: logic to save the subject
		// companySalesOrder.setSubject(companySalesOrderDTO.getSubject());
		companySalesOrder.setPurchaseOrder(companySalesOrderDTO.getPurchaseOrder());
		companySalesOrder.setCustomerNumber(companySalesOrderDTO.getCustomerNumber());
		companySalesOrder.setDueDate(companySalesOrderDTO.getDueDate());
		companySalesOrder.setPending(companySalesOrderDTO.getPending());
		companySalesOrder.setExciseDuty(companySalesOrderDTO.getExciseDuty());
		companySalesOrder.setCarrier(companySalesOrderDTO.getExciseDuty());
		companySalesOrder.setStatus(companySalesOrderDTO.getStatus());
		companySalesOrder.setSalesCommission(companySalesOrderDTO.getSalesCommission());
		log.info("updateSalesOrder method, updating and returning the data");
		return companySalesOrderRepository.save(companySalesOrder).getSalesOrderId();
	}

	@Override
	public WorkOrderDTO createWorkOrder(WorkOrderDTO workOrderDTO, Long companyId) {
		CompanyClientInfo clientInformation = clientInfoRepository
				.findByClientIdAndCompanyInfoCompanyId(workOrderDTO.getCompanyClientInfoId(), companyId)
				.orElseThrow(() -> new DataNotFoundException("The client information is not present"));
		EmployeePersonalInfo employeePersonalInfo = employeePersonalInfoRepository
				.findByEmployeeInfoIdAndCompanyInfoCompanyId(workOrderDTO.getRequestTo(), companyId).get(0);
		CompanyWorkOrder companyWorkOrder = new CompanyWorkOrder();
		BeanUtils.copyProperties(workOrderDTO, companyWorkOrder);
		if (Boolean.FALSE.equals(workOrderDTO.getIsCostEstimated()))
			companyWorkOrder.setEstimatedCost(null);
		companyWorkOrder.setCompanyClientInfo(clientInformation);
		companyWorkOrder.setEmployeePersonalInfo(employeePersonalInfo);
		companyWorkOrder.setCompanyInfo(
				companyInfoRepository.findById(companyId).orElseThrow(() -> new DataNotFoundException(null)));
		List<WorkOrderResources> workOrderResourcesList = new ArrayList<>();
		workOrderDTO.getWorkOrderResources().forEach((i) -> {
			WorkOrderResources workOrderResources = new WorkOrderResources();
			BeanUtils.copyProperties(i, workOrderResources);
			workOrderResourcesList.add(workOrderResources);
		});
		companyWorkOrder.setWorkOrderResourcesList(workOrderResourcesList);
		CompanyWorkOrder save = companyWorkOrderRepository.save(companyWorkOrder);
		WorkOrderDTO responseDTO = new WorkOrderDTO();
		List<WorkOrderResourcesDTO> workOrderResourcesDTOList = new ArrayList<>();
		BeanUtils.copyProperties(save, responseDTO);
		save.getWorkOrderResourcesList().forEach((i) -> {
			WorkOrderResourcesDTO workOrderResources = new WorkOrderResourcesDTO();
			BeanUtils.copyProperties(i, workOrderResources);
			workOrderResourcesDTOList.add(workOrderResources);
		});
		responseDTO.setWorkOrderResources(workOrderResourcesDTOList);
		return responseDTO;
	}

	@Override
	public CompanyPurchaseOrderDTO getPurcahaseOrderDetailsById(Long purcahseOrderId) {
		CompanyPurchaseOrder purchaseOrderDetails = companyPurchaseOrderRepository.findById(purcahseOrderId)
				.orElseThrow(() -> new DataNotFoundException("Purchase order details not found"));
		CompanyPurchaseOrderDTO companyPurchaseOrderDTO = new CompanyPurchaseOrderDTO();
		BeanUtils.copyProperties(purchaseOrderDetails, companyPurchaseOrderDTO);
		companyPurchaseOrderDTO.setProductType(ProductType.valueOf(purchaseOrderDetails.getType().toUpperCase()));
		companyPurchaseOrderDTO.setStockGroupName(purchaseOrderDetails.getCompanyStockGroup().getStockGroupName());
//		companyPurchaseOrderDTO.setContactName(contactPersonRepository
//				.findById(purchaseOrderDetails.getVendorContactPersonId())
//				.orElseThrow(() -> new DataNotFoundException("Vendor details not found")).getContactPersonName());
		companyPurchaseOrderDTO.setPurchaseOrderDate(purchaseOrderDetails.getPoDate());

		List<AddressInformationDTO> listOfAddress = new ArrayList<>();
		purchaseOrderDetails.getPurchaseBillingShippingAddressList().forEach((i) -> {
			AddressInformationDTO addressInformationDTO = new AddressInformationDTO();
			BeanUtils.copyProperties(i, addressInformationDTO);
			listOfAddress.add(addressInformationDTO);
		});
		companyPurchaseOrderDTO.setAddressInformationDTO(listOfAddress);

		List<PurchaseItemDTO> listOfPurchaseDetails = new ArrayList<>();
		purchaseOrderDetails.getPurchaseOrderItemsList().forEach((i) -> {
			PurchaseItemDTO purchaseItemDTO = new PurchaseItemDTO();
			BeanUtils.copyProperties(i, purchaseItemDTO);
			listOfPurchaseDetails.add(purchaseItemDTO);
		});
		companyPurchaseOrderDTO.setPurchaseOrderItemsDTO(listOfPurchaseDetails);

		companyPurchaseOrderDTO.setDescription(purchaseOrderDetails.getPurchaseOrderDescription());
		return companyPurchaseOrderDTO;
	}

	public List<PurchasedOrderDisplayDTO> getAllPurchaseOrder(Long companyId) {
		List<CompanyPurchaseOrder> companyPurchaseOrderList = companyPurchaseOrderRepository
				.findByCompanyInfoCompanyId(companyId);
		if (companyPurchaseOrderList.isEmpty())
			throw new DataNotFoundException("Purchase order details not present");
		List<PurchasedOrderDisplayDTO> companyPurcahseOrderDTOList = new ArrayList<>();
		companyPurchaseOrderList.forEach((i) -> {
			PurchasedOrderDisplayDTO purchasedOrderDisplayDTO = new PurchasedOrderDisplayDTO();
			BeanUtils.copyProperties(i, purchasedOrderDisplayDTO);
			EmployeePersonalInfo employeePersonalInfo = employeePersonalInfoRepository
					.findByEmployeeInfoIdAndCompanyInfoCompanyId(i.getCreatedBy(), companyId).get(0);
			purchasedOrderDisplayDTO.setPurchaseOrderOwner(employeePersonalInfo.getFirstName());
			if (i.getVendorId() != null) {
				CompanyVendorInfo vendorInfo = companyVendorInfoRepository.findByVendorInfoId(i.getVendorId())
						.orElseThrow(() -> new DataNotFoundException("Vendor information not found"));
				purchasedOrderDisplayDTO.setVendorName(vendorInfo.getVendorName());
			}
			companyPurcahseOrderDTOList.add(purchasedOrderDisplayDTO);
		});
		return companyPurcahseOrderDTOList;
	}

	@Override
	public List<PurchasedOrderDisplayDTO> getAllSalesDetails(Long companyId) {
		List<CompanySalesOrder> salesOrderDetailsList = companySalesOrderRepository
				.findByCompanyInfoCompanyId(companyId);
		List<PurchasedOrderDisplayDTO> displayDetailsList = Lists.newArrayList();
		salesOrderDetailsList.forEach((i) -> {
			PurchasedOrderDisplayDTO salesOrderDisplayDTO = new PurchasedOrderDisplayDTO();
			BeanUtils.copyProperties(i, salesOrderDisplayDTO);
			salesOrderDisplayDTO.setPurchaseOrderOwner(employeePersonalInfoRepository.findById(i.getCreatedBy())
					.orElseThrow(() -> new DataNotFoundException("Owner details not found")).getFirstName());
			salesOrderDisplayDTO.setTotalPayableAmount(i.getTotalReceivableAmount());
			salesOrderDisplayDTO.setContactName(i.getClientContactPersonDetails().getFirstName());
			salesOrderDisplayDTO.setVendorName(i.getCompanyClientInfo().getClientName());
			displayDetailsList.add(salesOrderDisplayDTO);
		});
		return displayDetailsList;
	}

	@Override
	public CompanyPurchaseOrderDTO getSalesDetailsById(Long salesOrderId) {
		CompanySalesOrder saleOrderDetails = companySalesOrderRepository.findById(salesOrderId)
				.orElseThrow(() -> new DataNotFoundException("Sales Details Not Found"));
		List<AddressInformationDTO> listOfAddress = Lists.newArrayList();
		saleOrderDetails.getSalesBillingShippingAddressList().forEach((i)->{
			AddressInformationDTO addressInformationDTO = new AddressInformationDTO();
			BeanUtils.copyProperties(i, addressInformationDTO);
			listOfAddress.add(addressInformationDTO);
		});
		List<PurchaseItemDTO> listOfSaleItem = Lists.newArrayList();
		saleOrderDetails.getSalesOrderItemsList().forEach((i)->{
			PurchaseItemDTO salesItemDTO = new PurchaseItemDTO();
			BeanUtils.copyProperties(i, salesItemDTO);
			salesItemDTO.setPayableAmount(i.getReceivableAmount());
			listOfSaleItem.add(salesItemDTO);
		});
		return  CompanyPurchaseOrderDTO.builder()
				.purchaseOrderOwner(employeePersonalInfoRepository.findById(saleOrderDetails.getCreatedBy())
						.orElseThrow(() -> new DataNotFoundException("Sales Owner Details Not Found")).getFirstName())
				.productType(ProductType.valueOf(Optional.ofNullable(saleOrderDetails.getType().toUpperCase()).orElseThrow()))
				.stockGroupName(saleOrderDetails.getCompanyStockGroup().getStockGroupName())
				.subject(saleOrderDetails.getSubject()).customerNumber(saleOrderDetails.getCustomerNumber())
				.dueDate(saleOrderDetails.getDueDate())
				.contactName(saleOrderDetails.getClientContactPersonDetails().getFirstName())
				.exciseDuty(saleOrderDetails.getExciseDuty()).carrier(saleOrderDetails.getCarrier())
				.status(saleOrderDetails.getStatus()).salesCommission(saleOrderDetails.getSalesCommission())
				.addressInformationDTO(listOfAddress).purchaseOrderItemsDTO(listOfSaleItem)
				.build();
		
		

	}
}
