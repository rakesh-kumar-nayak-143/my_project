package com.te.flinko.controller.account;

import static com.te.flinko.common.account.AccountConstants.EMPLOYEE_ADVANCE_SALARY_SUCCESSFULLY_FETCHED;
import static com.te.flinko.common.hr.HrConstants.SALARY_RECORDS_NOT_FOUND;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.te.flinko.dto.account.AccountPaySlipInputDTO;
import com.te.flinko.dto.account.AccountPaySlipListDTO;
import com.te.flinko.response.SuccessResponse;
import com.te.flinko.service.account.AccountPaySlipService;

import lombok.extern.slf4j.Slf4j;
@CrossOrigin(origins = "*")
@Slf4j
@RequestMapping(path = "/api/v1/account")
@RestController

/**
 * 
 * @author Ravindra
 *
 */
public class AccountPaySlipController {
	@Autowired
	AccountPaySlipService service;
	
	@GetMapping("/pay-slip")
	public ResponseEntity<SuccessResponse> paySlip(@RequestBody AccountPaySlipInputDTO accountPaySlipInputDTO ){
		ArrayList<AccountPaySlipListDTO> paySlip = service.paySlip(accountPaySlipInputDTO);
		if(paySlip==null || paySlip.isEmpty()) {
			return new ResponseEntity<>(
					new SuccessResponse(false, SALARY_RECORDS_NOT_FOUND, paySlip),
					HttpStatus.OK);
		}
		return new ResponseEntity<>(
				new SuccessResponse(false, EMPLOYEE_ADVANCE_SALARY_SUCCESSFULLY_FETCHED, paySlip),
				HttpStatus.OK);
	}
	
	

}
