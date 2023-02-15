package com.te.flinko.repository.helpandsupport.mongo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.te.flinko.entity.helpandsupport.mongo.CompanyAccountTickets;
import com.te.flinko.entity.helpandsupport.mongo.CompanyAdminDeptTickets;

public interface CompanyAdminDeptTicketsRepo extends MongoRepository<CompanyAdminDeptTickets,String> {
	
	Optional<CompanyAdminDeptTickets> findByObjectTicketIdAndTicketHistroysStatusIgnoreCase(String objectTicketId,String status);

	public List<CompanyAdminDeptTickets>  findByCompanyIdAndTicketHistroysDepartment(Long companyId,String department);

	public List<CompanyAdminDeptTickets>  findByCompanyIdAndTicketHistroysStatusIgnoreCase(Long companyId,String status);
	
	public List<CompanyAdminDeptTickets> findByCompanyId(Long companyId);
	
	public List<CompanyAdminDeptTickets> findByCompanyIdAndCategory(Long companyId,String category);
	
	List<CompanyAdminDeptTickets> findByCategoryAndSubCategoryAndCompanyIdAndEmployeeIdAndTicketHistroysDate(String category, String subCategory,
			Long companyId, String employeeId,LocalDate date);
}
