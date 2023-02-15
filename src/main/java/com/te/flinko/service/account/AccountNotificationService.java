package com.te.flinko.service.account;

import java.util.ArrayList;

import com.te.flinko.dto.account.AdvanceSalaryDTO;
import com.te.flinko.dto.account.ReimbursementInfoByIdDTO;
import com.te.flinko.dto.account.ReimbursementListDTO;

public interface AccountNotificationService {

	ArrayList<ReimbursementListDTO> reimbursement(Long companyId);

	ReimbursementInfoByIdDTO reimbursementById(Long companyId, Long reimbursementId);


}
