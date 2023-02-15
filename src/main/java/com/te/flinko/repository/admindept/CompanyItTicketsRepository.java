package com.te.flinko.repository.admindept;

import java.time.LocalDate;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.te.flinko.entity.helpandsupport.mongo.CompanyItTickets;

public interface CompanyItTicketsRepository extends MongoRepository<CompanyItTickets, String> {

	public List<CompanyItTickets> findByCompanyIdAndIdentificationNumber(Long companyId, String identificationNumber);

	public List<CompanyItTickets> findByCompanyId(Long companyId);

	public List<CompanyItTickets> findByIdentificationNumber(String serialNumber);
	
	
	List<CompanyItTickets> findByCategoryAndSubCategoryAndCompanyIdAndEmployeeIdAndTicketHistroysDate(String category, String subCategory,
			Long companyId, String employeeId,LocalDate date);
}
