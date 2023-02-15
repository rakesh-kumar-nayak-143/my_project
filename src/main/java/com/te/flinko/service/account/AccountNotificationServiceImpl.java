package com.te.flinko.service.account;

import static com.te.flinko.common.account.AccountConstants.EMPLOYEES_REIMBURSEMENT_INFORMATION_NOT_FOUND;
import static com.te.flinko.common.hr.HrConstants.COMPANY_INFORMATION_NOT_PRESENT;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.te.flinko.dto.account.ReimbursementInfoByIdDTO;
import com.te.flinko.dto.account.ReimbursementListDTO;
import com.te.flinko.entity.admin.CompanyInfo;
import com.te.flinko.entity.employee.EmployeeReimbursementInfo;
import com.te.flinko.exception.CompanyIdNotFoundException;
import com.te.flinko.exception.account.CustomExceptionForAccount;
import com.te.flinko.repository.admin.CompanyInfoRepository;
import com.te.flinko.repository.employee.EmployeeReimbursementInfoRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountNotificationServiceImpl implements AccountNotificationService {
	@Autowired
	private EmployeeReimbursementInfoRepository employeeReimbursementInfoRepo;
	@Autowired
	private CompanyInfoRepository companyInfoRepository;


	@Override
	/**
	 * this method is use to save  the attachments details 
	 * accept the object of companyPurchaseInvoice
	 * 
	 * @param invoiceDetailsDto object
	 * @return updated object of InvoiceDetailsDTO
	 **/
	public ArrayList<ReimbursementListDTO> reimbursement(Long companyId) {
		List<EmployeeReimbursementInfo> reimbursementInfo = employeeReimbursementInfoRepo
				.findByEmployeePersonalInfoCompanyInfoCompanyId(companyId);
		if (reimbursementInfo == null || reimbursementInfo.isEmpty()) {
			log.info(EMPLOYEES_REIMBURSEMENT_INFORMATION_NOT_FOUND);
			throw new CustomExceptionForAccount(EMPLOYEES_REIMBURSEMENT_INFORMATION_NOT_FOUND);
		}
		ArrayList<ReimbursementListDTO> reimbursement = new ArrayList<>();
	
		for (EmployeeReimbursementInfo employeeReimbursementInfo : reimbursementInfo) {
			String status="";
			if(Boolean.TRUE.equals(employeeReimbursementInfo.getIsPaid())) {
				status="Paid";
			}
			else if(!Boolean.TRUE.equals(employeeReimbursementInfo.getIsPaid())) {
				status="Not paid";
			}
			reimbursement.add(new ReimbursementListDTO(employeeReimbursementInfo.getReimbursementId(),
					employeeReimbursementInfo.getCompanyExpenseCategories().getExpenseCategoryName(),
					employeeReimbursementInfo.getExpenseDate(), employeeReimbursementInfo.getAmount(),
					employeeReimbursementInfo.getEmployeePersonalInfo().getFirstName() + " "
							+ employeeReimbursementInfo.getEmployeePersonalInfo().getLastName(),
					status));

		}
		return reimbursement;
	}

	@Override
	public ReimbursementInfoByIdDTO reimbursementById(Long companyId, Long reimbursementId) {
		
		 companyInfoRepository.findById(companyId)
				.orElseThrow(() -> new CompanyIdNotFoundException(COMPANY_INFORMATION_NOT_PRESENT));
		EmployeeReimbursementInfo reimbursementInfo = employeeReimbursementInfoRepo.findById(reimbursementId)
				.orElseThrow(() -> new CustomExceptionForAccount(EMPLOYEES_REIMBURSEMENT_INFORMATION_NOT_FOUND));
		ReimbursementInfoByIdDTO reimbursementInfoByIdDTO = new ReimbursementInfoByIdDTO();
		BeanUtils.copyProperties(reimbursementInfo, reimbursementInfoByIdDTO);
		reimbursementInfoByIdDTO.setExpenseCategoryName(reimbursementInfo.getCompanyExpenseCategories().getExpenseCategoryName());
		reimbursementInfoByIdDTO.setFullName(reimbursementInfo.getEmployeePersonalInfo().getFirstName()+" "+reimbursementInfo.getEmployeePersonalInfo().getLastName());
		String status="";
		if(Boolean.TRUE.equals(reimbursementInfo.getIsPaid())) {
			status="Paid";
		}
		else if(!Boolean.TRUE.equals(reimbursementInfo.getIsPaid())) {
			status=" Not paid";
		}
		reimbursementInfoByIdDTO.setStatus(status);
		return reimbursementInfoByIdDTO;

	}

}
