package com.te.flinko.service.hr;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.te.flinko.beancopy.BeanCopy;
import com.te.flinko.dto.helpandsupport.mongo.TicketHistroy;
import com.te.flinko.dto.hr.EmployeeInformationDTO;
import com.te.flinko.dto.hr.EventManagementDepartmentNameDTO;
import com.te.flinko.dto.hr.UpdateTicketDTO;
import com.te.flinko.dto.hr.mongo.CompanyHrTicketsDTO;
import com.te.flinko.dto.hr.mongo.TicketHistoryDTO;
import com.te.flinko.entity.Department;
import com.te.flinko.entity.admin.CompanyInfo;
import com.te.flinko.entity.employee.EmployeePersonalInfo;
import com.te.flinko.entity.helpandsupport.mongo.CompanyAccountTickets;
import com.te.flinko.entity.helpandsupport.mongo.CompanyAdminDeptTickets;
import com.te.flinko.entity.helpandsupport.mongo.CompanyHrTickets;
import com.te.flinko.entity.helpandsupport.mongo.CompanyItTickets;
import com.te.flinko.exception.InavlidInputException;
import com.te.flinko.exception.admin.EmployeeNotFoundException;
import com.te.flinko.exception.admin.NoDataPresentException;
import com.te.flinko.exception.employee.DataNotFoundException;
import com.te.flinko.repository.admin.CompanyInfoRepository;
import com.te.flinko.repository.admin.DepartmentInfoRepository;
import com.te.flinko.repository.admindept.CompanyItTicketsRepository;
import com.te.flinko.repository.employee.EmployeePersonalInfoRepository;
import com.te.flinko.repository.helpandsupport.mongo.CompanyAccountTicketsRepository;
import com.te.flinko.repository.helpandsupport.mongo.CompanyAdminDeptTicketsRepo;
import com.te.flinko.repository.helpandsupport.mongo.CompanyHrTicketsRepository;

@Service
public class CompanyHrTicketsServiceImpl implements CompanyHrTicketsService {
	@Autowired
	CompanyHrTicketsRepository hrTicketsRepository;
	@Autowired
	CompanyInfoRepository companyInfoRepository;
	@Autowired
	EmployeePersonalInfoRepository employeePersonalInfoRepository;
	@Autowired
	DepartmentInfoRepository departmentInfoRepository;

	@Autowired
	private CompanyAccountTicketsRepository companyAccountTicketsRepository;

	@Autowired
	private CompanyItTicketsRepository companyItTicketsRepository;

	@Autowired
	private CompanyAdminDeptTicketsRepo companyAdminDeptTicketsRepo;

//API For Getting All the tickets raised in a Company
	@Override
	public List<CompanyHrTicketsDTO> hrTicketsInfoList(Long companyId) {
		Optional<CompanyInfo> optionalDetails = companyInfoRepository.findById(companyId);
		List<CompanyHrTicketsDTO> companyHrTicketsDTOList = new ArrayList<>();
		if (!optionalDetails.isPresent()) {
			throw new DataNotFoundException("Company Id Not Present");
		} else {
			List<CompanyHrTickets> hrTicketsList = hrTicketsRepository.findByCompanyId(companyId);
			if (!hrTicketsList.isEmpty()) {

				for (CompanyHrTickets hrTicketInfo : hrTicketsList) {
					CompanyHrTicketsDTO companyHrTicketsDTO = new CompanyHrTicketsDTO();
					companyHrTicketsDTO.setHrTicketId(hrTicketInfo.getHrTicketId());
					companyHrTicketsDTO.setCategory(hrTicketInfo.getCategory());
					TicketHistroy latestTicketHistory = hrTicketInfo.getTicketHistroys()
							.get((hrTicketInfo.getTicketHistroys().size() - 1));
					companyHrTicketsDTO.setStatus(latestTicketHistory.getStatus());
					companyHrTicketsDTO.setTicketObjectId(hrTicketInfo.getTicketObjectId());

					List<EmployeePersonalInfo> createdByPersonalInfo = employeePersonalInfoRepository
							.findByEmployeeInfoIdAndCompanyInfoCompanyId(hrTicketInfo.getCreatedBy(), companyId);

					/*
					 * if (createdByPersonalInfo.isEmpty()) { throw new
					 * NoDataPresentException(" Created By Employee personal Info Details  Not Found"
					 * ); } else {
					 */
					for (EmployeePersonalInfo cretedByPersonInfo : createdByPersonalInfo) {
						if (cretedByPersonInfo.getEmployeeOfficialInfo() != null) {
							EmployeeInformationDTO createdByInformationDTO = new EmployeeInformationDTO(
									cretedByPersonInfo.getEmployeeOfficialInfo().getEmployeeId(),
									cretedByPersonInfo.getEmployeeInfoId(),
									cretedByPersonInfo.getFirstName() + " " + cretedByPersonInfo.getLastName());
							companyHrTicketsDTO.setTicketOwner(createdByInformationDTO);
							companyHrTicketsDTO.setRaisedDate(hrTicketInfo.getCreatedDate().toLocalDate());
							// }
						}
					}

					companyHrTicketsDTOList.add(companyHrTicketsDTO);
				}

				return companyHrTicketsDTOList;
			} else {
				throw new NoDataPresentException("Hr Tickets not found");
			}
		}

	}

//API For Getting the details of the tickets based on ticketId
	@Override
	public CompanyHrTicketsDTO hrTicketsDTOInfo(String hrTicketObjectId, Long employeeInfoId) {

		Optional<CompanyHrTickets> hrTicketsInfoOptional = hrTicketsRepository.findById(hrTicketObjectId);
		if (hrTicketsInfoOptional.isPresent()) {
			CompanyHrTickets hrTicketsInfo = hrTicketsInfoOptional.get();
			Long companyId2 = hrTicketsInfo.getCompanyId();
			// Long companyId = companyInfo != null ? companyInfo.getCompanyId() : null;

			CompanyHrTicketsDTO companyHrTicketsDTO = new CompanyHrTicketsDTO();
			companyHrTicketsDTO.setHrTicketId(hrTicketsInfo.getHrTicketId());
			companyHrTicketsDTO.setCategory(hrTicketsInfo.getCategory());
			companyHrTicketsDTO.setEmployeeId(hrTicketsInfo.getEmployeeId());
			companyHrTicketsDTO.setRatings(hrTicketsInfo.getRating());
			companyHrTicketsDTO.setTicketObjectId(hrTicketsInfo.getTicketObjectId());

			List<EmployeePersonalInfo> personalInfo = employeePersonalInfoRepository
					.findByEmployeeOfficialInfoEmployeeIdAndCompanyInfoCompanyId(hrTicketsInfo.getEmployeeId(),
							companyId2);
			if (!personalInfo.isEmpty()) {
				companyHrTicketsDTO
						.setEmployeeName(personalInfo.get(0).getFirstName() + " " + personalInfo.get(0).getLastName());
			} else {
				throw new NoDataPresentException("Employee personal Info Details Not Found");
			}
			TicketHistroy latestTicketHistory = hrTicketsInfo.getTicketHistroys()
					.get((hrTicketsInfo.getTicketHistroys().size() - 1));
			companyHrTicketsDTO.setStatus(latestTicketHistory.getStatus());
			if(latestTicketHistory.getStatus().equalsIgnoreCase("Created") || latestTicketHistory.getStatus().equalsIgnoreCase("Deligated")) {
				companyHrTicketsDTO.setIsAuthorizedPerson(true);
			}else {
				companyHrTicketsDTO.setIsAuthorizedPerson(Objects.equals(employeeInfoId, latestTicketHistory.getBy()));
			}

			List<EmployeePersonalInfo> reportingPersonalInfo = employeePersonalInfoRepository
					.findByEmployeeOfficialInfoEmployeeIdAndCompanyInfoCompanyId(hrTicketsInfo.getReportingManagerId(),
							companyId2);
			if (!reportingPersonalInfo.isEmpty()) {
				EmployeeInformationDTO managerInformationDTO = new EmployeeInformationDTO(
						reportingPersonalInfo.get(0).getEmployeeOfficialInfo().getEmployeeId(),
						reportingPersonalInfo.get(0).getEmployeeInfoId(),
						reportingPersonalInfo.get(0).getFirstName() + " " + reportingPersonalInfo.get(0).getLastName());

				companyHrTicketsDTO.setReportingManagerId(managerInformationDTO);
			} else {
				throw new NoDataPresentException("Reporting Manager Personal Info Details Not Found");

			}
			companyHrTicketsDTO.setAttachments(hrTicketsInfo.getAttachmentsUrl());
			companyHrTicketsDTO.setDescription(hrTicketsInfo.getDescription());
			List<TicketHistroy> ticketHistroys = hrTicketsInfo.getTicketHistroys();
			List<TicketHistoryDTO> ticketHistroys2 = new ArrayList<>();
			EventManagementDepartmentNameDTO departmentDTO;
			EmployeeInformationDTO employeeInformationDTO;
			for (TicketHistroy ticketHistroy : ticketHistroys) {
				TicketHistoryDTO historyDTO = new TicketHistoryDTO();
				BeanUtils.copyProperties(ticketHistroy, historyDTO);
				historyDTO.setDate(ticketHistroy.getDate());

				List<EmployeePersonalInfo> personalInfo2 = employeePersonalInfoRepository
						.findByEmployeeInfoIdAndCompanyInfoCompanyId(ticketHistroy.getBy(), companyId2);
				if (personalInfo2.isEmpty()) {
					throw new NoDataPresentException("Get by Employee Personal Info Details Not Found");
				} else {
					for (EmployeePersonalInfo ticketHistroy2 : personalInfo2) {
						employeeInformationDTO = new EmployeeInformationDTO(
								ticketHistroy2.getEmployeeOfficialInfo().getEmployeeId(),
								ticketHistroy2.getEmployeeInfoId(),
								ticketHistroy2.getFirstName() + " " + ticketHistroy2.getLastName());

						historyDTO.setBy(employeeInformationDTO);
					}

				}

				String department2 = ticketHistroy.getDepartment();

				List<Department> alldepartmentList = departmentInfoRepository.findAll();

				for (Department department : alldepartmentList) {

					if (department.getDepartmentName().equals(department2)) {
						departmentDTO = new EventManagementDepartmentNameDTO(department.getDepartmentId(),
								department.getDepartmentName());

						historyDTO.setDepartment(departmentDTO);
					}
				}

				ticketHistroys2.add(historyDTO);
			}

			List<EmployeePersonalInfo> createdByPersonalInfo = employeePersonalInfoRepository
					.findByEmployeeInfoIdAndCompanyInfoCompanyId(hrTicketsInfo.getCreatedBy(), companyId2);

			if (createdByPersonalInfo.isEmpty()) {
				throw new NoDataPresentException("Created By Employee Info Details Not Found");
			} else {
				for (EmployeePersonalInfo cretedByPersonInfo : createdByPersonalInfo) {
					EmployeeInformationDTO createdByInformationDTO = new EmployeeInformationDTO(
							cretedByPersonInfo.getEmployeeOfficialInfo().getEmployeeId(),
							cretedByPersonInfo.getEmployeeInfoId(),
							cretedByPersonInfo.getFirstName() + " " + cretedByPersonInfo.getLastName());
					companyHrTicketsDTO.setTicketOwner(createdByInformationDTO);

				}
			}

			TicketHistoryDTO latestticketHistroyDTO = ticketHistroys2.get((ticketHistroys2.size() - 1));

			companyHrTicketsDTO.setTicketRaisedby(latestticketHistroyDTO.getBy());
			companyHrTicketsDTO.setTicketRaisedDate(latestticketHistroyDTO.getDate());

			companyHrTicketsDTO.setRaisedDate(hrTicketsInfo.getCreatedDate().toLocalDate());

			companyHrTicketsDTO.setHistroyList(ticketHistroys2);

			return companyHrTicketsDTO;

		} else
			throw new NoDataPresentException("TicketId Not present");

	}

	// API for Actions in tickets
	/*
	 * @Override public CompanyHrTicketsDTO updateTicketHistory(String status, Long
	 * employeeInfoId, String hrTicketObjectId) {
	 * 
	 * Optional<CompanyHrTickets> ticketOptional =
	 * hrTicketsRepository.findById(hrTicketObjectId); if
	 * (!ticketOptional.isPresent()) { throw new
	 * DataNotFoundException("Ticket Not Found"); } CompanyHrTickets ticket =
	 * ticketOptional.get(); TicketHistroy historyDTO = new TicketHistroy();
	 * historyDTO.setDate(LocalDate.now()); historyDTO.setBy(employeeInfoId);
	 * historyDTO.setDepartment(employeePersonalInfoRepository.findById(
	 * employeeInfoId).get().getEmployeeOfficialInfo() .getDepartment());
	 * historyDTO.setStatus(status); ticket.getTicketHistroys().add(historyDTO);
	 * 
	 * // repository.save(ticket); CompanyHrTicketsDTO dto = new
	 * CompanyHrTicketsDTO();
	 * BeanUtils.copyProperties(hrTicketsRepository.save(ticket), dto); return dto;
	 * 
	 * }
	 */

	@Override
	@Transactional
	public CompanyHrTicketsDTO updateTicketHistory(UpdateTicketDTO updateTicketDTO, Long employeeInfoId) {

		Optional<CompanyHrTickets> ticketOptional = hrTicketsRepository.findById(updateTicketDTO.getTicketObjectId());
		if (!ticketOptional.isPresent()) {
			throw new DataNotFoundException("Ticket Not Found");
		}
		CompanyHrTickets ticket = ticketOptional.get();
		TicketHistroy historyDTO = new TicketHistroy();

		BeanUtils.copyProperties(updateTicketDTO, historyDTO);
		EmployeePersonalInfo employee = employeePersonalInfoRepository.findById(employeeInfoId)
				.orElseThrow(() -> new EmployeeNotFoundException("Employee Not Found"));
		historyDTO.setDepartment(
				employee.getEmployeeOfficialInfo() == null ? null : employee.getEmployeeOfficialInfo().getDepartment());
		historyDTO.setDate(LocalDate.now());
		historyDTO.setBy(employeeInfoId);
		ticket.getTicketHistroys().add(historyDTO);

		CompanyHrTicketsDTO dto = new CompanyHrTicketsDTO();
		BeanUtils.copyProperties(hrTicketsRepository.save(ticket), dto);
		if (updateTicketDTO.getStatus().equalsIgnoreCase("Deligated")) {
			switch (updateTicketDTO.getDepartment()) {
			case "ACCOUNTS": {
				companyAccountTicketsRepository.save(BeanCopy.objectProperties(ticket, CompanyAccountTickets.class));
				break;
			}

			case "IT": {
				companyItTicketsRepository.save(BeanCopy.objectProperties(ticket, CompanyItTickets.class));
				break;
			}

			case "ADMIN": {
				companyAdminDeptTicketsRepo.save(BeanCopy.objectProperties(ticket, CompanyAdminDeptTickets.class));
				break;
			}

			default:
				throw new InavlidInputException("Department does not Exists");

			}
			hrTicketsRepository.deleteById(updateTicketDTO.getTicketObjectId());
		}
		return dto;

	}
}
