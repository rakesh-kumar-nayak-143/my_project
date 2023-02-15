package com.te.flinko.repository.helpandsupport.mongo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.te.flinko.entity.helpandsupport.mongo.CompanyAccountTickets;

public interface CompanyAccountTicketsRepository extends MongoRepository<CompanyAccountTickets, Long> {

	List<CompanyAccountTickets> findByCategoryAndSubCategoryAndCompanyIdAndEmployeeIdAndTicketHistroysDateAndIdentificationNumber(
			String category, String subCategory, Long companyId, String employeeId, LocalDate date,String identificationNumber);
	
	Optional<CompanyAccountTickets> findByCategoryAndCompanyIdAndIdentificationNumberIn(
			String category,Long companyId,List<String> identificationNumber);

	List<CompanyAccountTickets> findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId(String category,
			String subCategory, Long companyId, String employeeId);
	
	
	
	

	Long findByCompanyId(Long companyId);

}
