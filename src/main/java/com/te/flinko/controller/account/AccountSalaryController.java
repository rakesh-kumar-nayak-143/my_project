package com.te.flinko.controller.account;


import static com.te.flinko.common.account.AccountConstants.EMPLOYEE_SALARY_DETAILS_FETCHED_SUCCESSFULLY;
import static com.te.flinko.common.account.AccountConstants.EMPLOYEES_SALARY_DETAILS_FETCHED_SUCCESSFULLY;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.te.flinko.audit.BaseConfigController;
import com.te.flinko.dto.account.AccountSalaryDTO;
import com.te.flinko.dto.account.AccountSalaryInputDTO;
import com.te.flinko.dto.hr.EmployeeSalaryAllDetailsDTO;
import com.te.flinko.response.SuccessResponse;
import com.te.flinko.service.account.AccountSalaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/account")
@RestController

/**
 * 
 * @author Ravindra
 *
 */
public class AccountSalaryController extends BaseConfigController {
	@Autowired 
	AccountSalaryService service;
	
	
	@GetMapping("/salary")
	public ResponseEntity<SuccessResponse> salaryDetailsList(@RequestBody AccountSalaryInputDTO accountSalaryInputDTO){
		ArrayList<AccountSalaryDTO> salaryDetailsList = service.salaryDetailsList(accountSalaryInputDTO);
		return new ResponseEntity<>(
				new SuccessResponse(false, EMPLOYEES_SALARY_DETAILS_FETCHED_SUCCESSFULLY, salaryDetailsList),
				HttpStatus.OK);
	}
	
@GetMapping("/salary-details")
public ResponseEntity<SuccessResponse> salaryDetailsById(@RequestParam Long salaryId){
	EmployeeSalaryAllDetailsDTO salaryDetailsById = service.salaryDetailsById(salaryId,getCompanyId());
	return new ResponseEntity<>(
			new SuccessResponse(false, EMPLOYEE_SALARY_DETAILS_FETCHED_SUCCESSFULLY, salaryDetailsById),
			HttpStatus.OK);
}

}
