package com.te.flinko.service.it;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.te.flinko.constants.admindept.AdminDeptConstants;
import com.te.flinko.dto.admindept.CompanyPCLaptopDTO;
import com.te.flinko.dto.admindept.PcLaptopSoftwareDetailsDTO;
import com.te.flinko.entity.it.CompanyPcLaptopDetails;
import com.te.flinko.entity.it.PcLaptopSoftwareDetails;
import com.te.flinko.exception.admindept.CompanyPCLaptopDetailsNotFoundException;
import com.te.flinko.repository.it.ITPcLaptopRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ITSoftwareMaintenanceServiceImpl implements ITSoftwareMaintenanceService {

	@Autowired
	ITPcLaptopRepository pcLaptopRepository;

	private static final String WORKING = "Working";
	private static final String NOT_WORKING = "Not Working";

	/* Software Maintenance */
	
	@Override
	public List<CompanyPCLaptopDTO> getITSoftwareMaintenanceDetails(Long companyId) {
		log.info("Get the list of software maintenance details against comapnyId:: ", companyId);
		List<CompanyPcLaptopDetails> softwareMaintenanceDetails = pcLaptopRepository
				.findByCompanyInfoCompanyId(companyId);
		if (softwareMaintenanceDetails.isEmpty()) {
			return Collections.emptyList();
		}
		CompanyPcLaptopDetails companyPcLaptopDetails = new CompanyPcLaptopDetails();
		softwareMaintenanceDetails.add(companyPcLaptopDetails);

		return softwareMaintenanceDetails.stream().filter(x -> Boolean.TRUE.equals(x.getCpldIsWorking())).map(x -> {
			CompanyPCLaptopDTO companyPCLaptopDTO = new CompanyPCLaptopDTO();
			BeanUtils.copyProperties(x, companyPCLaptopDTO);
			companyPCLaptopDTO.setStatus(Boolean.TRUE.equals(x.getCpldIsWorking()) ? WORKING : NOT_WORKING);

			boolean isRenewalPending = x.getPcLaptopSoftwareDetailsList().stream()
					.filter(i -> i.getExpirationDate().isAfter(LocalDate.now()))
					.anyMatch(i -> Boolean.TRUE.equals(i.getIsRenewed()));

			long count = x.getPcLaptopSoftwareDetailsList().stream().count();
			companyPCLaptopDTO.setNoOfSoftwareInstalled(count);
			companyPCLaptopDTO.setIsRenewalPending(isRenewalPending);
			return companyPCLaptopDTO;
		}).filter(x -> x.getStatus().equalsIgnoreCase(WORKING)).collect(Collectors.toList());
	}

	@Override
	public List<PcLaptopSoftwareDetailsDTO> getITSoftwareMaintenanceDetailsList(Long companyId, String serialNumber) {
		log.info("Get the list of software maintenance details against comapnyId:: ",
				companyId + " and Serial Number::" + serialNumber);
		CompanyPcLaptopDetails companyPcLaptopDetails = pcLaptopRepository
				.findBySerialNumberAndCompanyInfoCompanyId(serialNumber, companyId)
				.orElseThrow(() -> new CompanyPCLaptopDetailsNotFoundException(
						AdminDeptConstants.COMPANY_PC_LAPTOP_DETAILS_NOT_FOUND));

		List<PcLaptopSoftwareDetails> pcLaptopSoftwareDetailsList = companyPcLaptopDetails
				.getPcLaptopSoftwareDetailsList();

		List<PcLaptopSoftwareDetailsDTO> collect = pcLaptopSoftwareDetailsList.stream()
				.filter(i -> i.getExpirationDate().isAfter(LocalDate.now())).map(x -> {
					PcLaptopSoftwareDetailsDTO pcLaptopSoftwareDetailsDTO = new PcLaptopSoftwareDetailsDTO();
					BeanUtils.copyProperties(x, pcLaptopSoftwareDetailsDTO);
					pcLaptopSoftwareDetailsDTO.setIsRenewalPending(Boolean.TRUE.equals(x.getIsRenewed()));
					return pcLaptopSoftwareDetailsDTO;
				}).collect(Collectors.toList());
		return collect;
	}

}
