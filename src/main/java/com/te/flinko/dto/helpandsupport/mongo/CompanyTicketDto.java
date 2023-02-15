package com.te.flinko.dto.helpandsupport.mongo;

import java.io.Serializable;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SuppressWarnings("serial")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(value = Include.NON_DEFAULT)
public class CompanyTicketDto implements Serializable {
	private Long accountTicketId;
	private String category;
	private String subCategory;
	private String description;
	private String employeeId;
	private String department;
	private String attachmentsUrl;
	private String reportingManagerId;
	private List<TicketHistroy> ticketHistroys;
	private String feedback;
	private Integer rating;
	private Long companyId;
	private String objectTicketId;
	private Long adminTicketId;
	private String ticketObjectId;
	private Long hrTicketId;
	private Long createdBy;
	private LocalDate createdDate;
	private Long itTicketId;
	private Long productId;
	private String productName;
	private String identificationNumber;
}
