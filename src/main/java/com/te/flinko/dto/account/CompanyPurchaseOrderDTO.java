package com.te.flinko.dto.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.te.flinko.dto.admindept.PurchaseOrderItemsDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CompanyPurchaseOrderDTO {
    private String purchaseOrderOwner;
    private String purchaseOrderNumber;
    private ProductType productType;
    private Long stockGroupId;
    private String stockGroupName;
    private String subject;
    private Long vendorId;
    private String contactName;
    private LocalDate purchaseOrderDate;
    private String requisitionNumber;
    private String trackingNumber;
    private LocalDate dueDate;
    private String carrier;
    private String exciseDuty;
    private BigDecimal salesCommission;
    private String status;
    private String description;
    private List<AddressInformationDTO> addressInformationDTO;
    private List<PurchaseItemDTO> purchaseOrderItemsDTO;
    
    //sales field
    private Long customerNumber;
}
