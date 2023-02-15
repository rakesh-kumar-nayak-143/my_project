package com.te.flinko.service.account;

import java.util.ArrayList;

import com.te.flinko.dto.account.AccountSalaryDTO;
import com.te.flinko.dto.account.AccountSalaryInputDTO;
import com.te.flinko.dto.hr.EmployeeSalaryAllDetailsDTO;

public interface AccountSalaryService {

	ArrayList<AccountSalaryDTO> salaryDetailsList(AccountSalaryInputDTO accountSalaryInputDTO);

	EmployeeSalaryAllDetailsDTO salaryDetailsById(Long salaryId, Long companyId);

}
