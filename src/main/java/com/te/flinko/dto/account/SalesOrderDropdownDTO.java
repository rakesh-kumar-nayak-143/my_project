package com.te.flinko.dto.account;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.te.flinko.dto.account.mongo.ContactPerson;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT) 
public class SalesOrderDropdownDTO {
	private Long vendorInfoId;
	private String vendorName;
	private List<ContactPerson> contactPersons;
	private String subject;
}
