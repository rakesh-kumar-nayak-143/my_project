package com.te.flinko.service.helpandsupport.mongo;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.te.flinko.dto.admindept.ProductNameDTO;
import com.te.flinko.dto.helpandsupport.mongo.RaiseTicketDto;
import com.te.flinko.dto.helpandsupport.mongo.ReportingManagerDto;

public interface RaiseTicketService {


	public boolean createTickets(Long companyId, Long employeeInfoId, List<MultipartFile> files,
			RaiseTicketDto raiseTicketDto);

	public List<ReportingManagerDto> getAllReportingManagaer(Long companyId,String department);

	public List<ProductNameDTO> getProducts(Long companyId);




}
