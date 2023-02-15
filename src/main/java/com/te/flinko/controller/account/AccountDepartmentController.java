package com.te.flinko.controller.account;

import com.te.flinko.audit.BaseConfigController;
import com.te.flinko.dto.account.*;
import com.te.flinko.response.SuccessResponse;
import com.te.flinko.service.account.AccountDepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.te.flinko.common.account.AccountDepartmentConstants.*;
import static com.te.flinko.common.project.ProjectManagementConstants.ADD_UPDATE_BILLING_SHIPPING_ADDRESS;

@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/account")
@RestController
public class AccountDepartmentController extends BaseConfigController {

	private final AccountDepartmentService accountDepartmentService;

	@PostMapping(path = "/create-purchase-order")
	public ResponseEntity<SuccessResponse> createPurchaseOrder(
			@RequestBody @Valid CompanyPurchaseOrderDTO companyPurchaseOrderDTO) {
		log.info("createPurchaseOrder method, execution start");
		return ResponseEntity.status(HttpStatus.OK)
				.body(SuccessResponse.builder().error(Boolean.FALSE).message(PURCHASE_ORDER_SAVED_UPDATED)
						.data(accountDepartmentService.createPurchaseOrder(companyPurchaseOrderDTO, getCompanyId()))
						.build());
	}

	@PostMapping(path = "/create-sales-order")
	public ResponseEntity<SuccessResponse> createSalesOrder(
			@RequestBody @Valid CompanySalesOrderDTO companySalesOrderDTO) {
		log.info("createSalesOrder method, execution start");
		return ResponseEntity.status(HttpStatus.OK)
				.body(SuccessResponse.builder().error(Boolean.FALSE).message(SALES_ORDER_SAVED_UPDATED)
						.data(accountDepartmentService.createSalesOrder(companySalesOrderDTO, getCompanyId())).build());
	}

	@PostMapping(path = "/update-purchase-order/{purchaseOrderId}")
	public ResponseEntity<SuccessResponse> updatePurchaseOrder(
			@RequestBody @Valid CompanyPurchaseOrderDTO companyPurchaseOrderDTO, @PathVariable Long purchaseOrderId) {
		log.info("createPurchaseOrder method, execution start");
		return ResponseEntity.status(HttpStatus.OK)
				.body(SuccessResponse.builder().error(Boolean.FALSE).message(PURCHASE_ORDER_SAVED_UPDATED)
						.data(accountDepartmentService.updatePurchaseOrder(companyPurchaseOrderDTO, purchaseOrderId))
						.build());
	}

	@PostMapping(path = "/update-sales-order/{salesOrderId}")
	public ResponseEntity<SuccessResponse> updateSalesOrder(
			@RequestBody @Valid CompanySalesOrderDTO companySalesOrderDTO, @PathVariable Long salesOrderId) {
		log.info("createSalesOrder method, execution start");
		return ResponseEntity.status(HttpStatus.OK)
				.body(SuccessResponse.builder().error(Boolean.FALSE).message(SALES_ORDER_SAVED_UPDATED)
						.data(accountDepartmentService.updateSalesOrder(companySalesOrderDTO, salesOrderId)).build());
	}

	@PutMapping(path = "/add-purchase-address/{purchaseOrderId}")
	public ResponseEntity<SuccessResponse> addPurchaseBillingShippingAddress(
			@RequestBody @Valid BillingShippingAddressDTO billingShippingAddressDTO,
			@PathVariable Long purchaseOrderId) {
		return ResponseEntity
				.status(HttpStatus.OK).body(SuccessResponse.builder().error(Boolean.FALSE)
						.message(ADD_UPDATE_BILLING_SHIPPING_ADDRESS).data(accountDepartmentService
								.addPurchaseBillingShippingAddress(billingShippingAddressDTO, purchaseOrderId))
						.build());
	}

	@PutMapping(path = "/add-sales-address/{salesOrderId}")
	public ResponseEntity<SuccessResponse> addSalesBillingShippingAddress(
			@RequestBody @Valid BillingShippingAddressDTO billingShippingAddressDTO, @PathVariable Long salesOrderId) {
		return ResponseEntity.status(HttpStatus.OK).body(SuccessResponse.builder().error(Boolean.FALSE)
				.message(ADD_UPDATE_BILLING_SHIPPING_ADDRESS)
				.data(accountDepartmentService.addSalesBillingShippingAddress(billingShippingAddressDTO, salesOrderId))
				.build());
	}

	@PutMapping(path = "/add-purchase-items/{purchaseOrderId}")
	public ResponseEntity<SuccessResponse> addPurchasedItems(@RequestBody @Valid PurchaseItemsDTO purchaseItemsDTO,
			@PathVariable Long purchaseOrderId) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(SuccessResponse.builder().error(Boolean.FALSE).message(ADD_UPDATE_PURCHASE_ITEMS)
						.data(accountDepartmentService.addPurchasedItems(purchaseItemsDTO, purchaseOrderId)).build());
	}

	@GetMapping("/purchase/{purcahseOrderId}")
	public ResponseEntity<SuccessResponse> getPurchaseOrderDetialsById(@PathVariable Long purcahseOrderId) {
		return ResponseEntity.ok(SuccessResponse.builder().error(false).message("PurchaseDetails Fethced")
				.data(accountDepartmentService.getPurcahaseOrderDetailsById(purcahseOrderId)).build());

	}

	@PutMapping(path = "/add-ordered-items/{salesOrderId}")
	public ResponseEntity<SuccessResponse> addOrderedItems(@RequestBody @Valid SalesItemsDTO salesItemsDTO,
			@PathVariable Long salesOrderId) {
		return ResponseEntity.status(HttpStatus.OK)
				.body(SuccessResponse.builder().error(Boolean.FALSE).message(ADD_UPDATE_SALES_ITEMS)
						.data(accountDepartmentService.addOrderedItems(salesItemsDTO, salesOrderId)).build());
	}

	@GetMapping("/purchase")
	public ResponseEntity<SuccessResponse> getAllPurchaseOrder() {
		return ResponseEntity.ok(SuccessResponse.builder().error(Boolean.FALSE).message("Purchase Details List")
				.data(accountDepartmentService.getAllPurchaseOrder(getCompanyId())).build());
	}

	@GetMapping("/sales")
	public ResponseEntity<SuccessResponse> getAllSalesOrder() {
		return ResponseEntity.ok(SuccessResponse.builder().error(Boolean.FALSE).message("All Sales Details")
				.data(accountDepartmentService.getAllSalesDetails(getCompanyId())).build());

	}
	
	@GetMapping("/sales/{salesOrderId}")
	public ResponseEntity<SuccessResponse> getSalesOrderById(@PathVariable Long salesOrderId){
		return ResponseEntity.ok(SuccessResponse.builder().error(Boolean.FALSE).message("Sale Details Fetched Successfully")
				.data(accountDepartmentService.getSalesDetailsById(salesOrderId)).build());
	}
}
