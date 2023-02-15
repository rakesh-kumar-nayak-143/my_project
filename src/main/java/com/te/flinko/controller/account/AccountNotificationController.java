package com.te.flinko.controller.account;

import static com.te.flinko.common.account.AccountConstants.EMPLOYEE_REIMBURSEMENT_DETAILS_FETCHED_SUCCESSFULLY;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.te.flinko.audit.BaseConfigController;
import com.te.flinko.dto.account.ReimbursementInfoByIdDTO;
import com.te.flinko.dto.account.ReimbursementListDTO;
import com.te.flinko.response.SuccessResponse;
import com.te.flinko.service.account.AccountNotificationService;

import lombok.extern.slf4j.Slf4j;
@RestController
@RequestMapping("/api/v1/account")
@CrossOrigin(origins = "*")
@Slf4j
/**
 * 
 * @author Ravindra
 *
 */
public class AccountNotificationController extends BaseConfigController {
	@Autowired
	AccountNotificationService service;	
	
	@GetMapping("/reimbursement")
	public ResponseEntity<SuccessResponse> reimbursement(){
		log.info("reimbursement method execution started");
		ArrayList<ReimbursementListDTO> reimbursement = service.reimbursement(getCompanyId());
		return new ResponseEntity<>(
				new SuccessResponse(false, EMPLOYEE_REIMBURSEMENT_DETAILS_FETCHED_SUCCESSFULLY, reimbursement),
				HttpStatus.OK);
	}
	@GetMapping("/reimbursement-by-id")
	public ResponseEntity<SuccessResponse>reimbursementById(@RequestParam Long reimbursementId){
		log.info("reimbursementById method execution started");
		ReimbursementInfoByIdDTO reimbursementById = service.reimbursementById(getCompanyId(),reimbursementId);
		return new ResponseEntity<>(
				new SuccessResponse(false, EMPLOYEE_REIMBURSEMENT_DETAILS_FETCHED_SUCCESSFULLY, reimbursementById),
				HttpStatus.OK);
	}

	}
	
	


