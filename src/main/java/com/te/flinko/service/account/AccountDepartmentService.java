package com.te.flinko.service.account;

import java.util.List;

import com.te.flinko.dto.account.*;

public interface AccountDepartmentService {
	Long createPurchaseOrder(CompanyPurchaseOrderDTO companyPurchaseOrderDTO, Long companyId);

	Long addPurchaseBillingShippingAddress(BillingShippingAddressDTO billingShippingAddressDTO, Long purchaseOrderId);

	Long addPurchasedItems(PurchaseItemsDTO purchaseItemsDTO, Long purchaseOrderId);

	Long createSalesOrder(CompanySalesOrderDTO companySalesOrderDTO, Long companyId);

	Long addSalesBillingShippingAddress(BillingShippingAddressDTO billingShippingAddressDTO, Long salesOrderId);

	Long addOrderedItems(SalesItemsDTO salesItemsDTO, Long salesOrderId);

	WorkOrderDTO createWorkOrder(WorkOrderDTO workOrderDTO, Long companyId);

	List<PurchasedOrderDisplayDTO> getAllPurchaseOrder(Long companyId);

	Long updatePurchaseOrder(CompanyPurchaseOrderDTO companyPurchaseOrderDTO, Long purchaseOrderId);
	
	Long updateSalesOrder(CompanySalesOrderDTO companySalesOrderDTO, Long salesOrderId);
	
	CompanyPurchaseOrderDTO getPurcahaseOrderDetailsById(Long purcahseOrderId);

	List<PurchasedOrderDisplayDTO> getAllSalesDetails(Long companyId);

	CompanyPurchaseOrderDTO getSalesDetailsById(Long salesOrderId);
}
