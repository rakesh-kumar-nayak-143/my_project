package com.te.flinko.service.it;

import java.util.List;

import com.te.flinko.dto.admindept.CompanyPCLaptopDTO;
import com.te.flinko.dto.admindept.PcLaptopSoftwareDetailsDTO;

public interface ITSoftwareMaintenanceService {

	public List<CompanyPCLaptopDTO> getITSoftwareMaintenanceDetails(Long companyId);

	public List<PcLaptopSoftwareDetailsDTO> getITSoftwareMaintenanceDetailsList(Long companyId, String serialNumber);

}
