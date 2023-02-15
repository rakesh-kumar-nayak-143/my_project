package com.te.flinko.service.employee;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.te.flinko.dto.employee.EmployeeProjectListDTO;
import com.te.flinko.dto.employee.TimesheetDTO;
import com.te.flinko.dto.employee.mongo.EmployeeTaskListDTO;
import com.te.flinko.dto.employee.mongo.EmployeeTimesheetConfigurationDTO;
import com.te.flinko.dto.employee.mongo.EmployeeTimesheetDetailsDTO;
import com.te.flinko.dto.employee.mongo.Timesheet;
import com.te.flinko.entity.employee.EmployeePersonalInfo;
import com.te.flinko.entity.employee.mongo.EmployeeTimesheetDetails;
import com.te.flinko.entity.project.ProjectDetails;
import com.te.flinko.entity.project.mongo.ProjectTaskDetails;
import com.te.flinko.exception.DataNotFoundException;
import com.te.flinko.exception.EmployeeTimeSheetCannottEditedException;
import com.te.flinko.repository.employee.EmployeePersonalInfoRepository;
import com.te.flinko.repository.employee.mongo.EmployeeTimesheetDetailsRepository;
import com.te.flinko.repository.project.mongo.ProjectTaskDetailsRepository;

@Service
public class EmployeeTimeSheetServiceImpl implements EmployeeTimeSheetService {

	@Autowired
	EmployeeTimesheetDetailsRepository timeSheetRepository;

	@Autowired
	EmployeePersonalInfoRepository personalInfoRepository;

	@Autowired
	ProjectTaskDetailsRepository taskRepository;

	@Override
	public List<EmployeeProjectListDTO> getProjectList(Long employeeInfoId, Long companyId) {

		ArrayList<EmployeeProjectListDTO> projectListDTO = new ArrayList<>();
		List<EmployeePersonalInfo> employeePersonalInfo = personalInfoRepository
				.findByEmployeeInfoIdAndCompanyInfoCompanyId(employeeInfoId, companyId);

		if (!employeePersonalInfo.isEmpty()) {

			List<ProjectDetails> allotedProjectList = employeePersonalInfo.get(0).getAllocatedProjectList();

			if (allotedProjectList.isEmpty()) {

				throw new DataNotFoundException("Project Not Assigned");
			}
			for (ProjectDetails projectDetails : allotedProjectList) {

				EmployeeProjectListDTO DTO = new EmployeeProjectListDTO();
				DTO.setProjectId(projectDetails.getProjectId());
				DTO.setProjectName(projectDetails.getProjectName());

				projectListDTO.add(DTO);
			}
			return projectListDTO;
		} else {
			throw new DataNotFoundException("Employee Not Found");
		}
	}

	@Override
	public ProjectTaskDetails saveProjectTaskDetails(ProjectTaskDetails projectTaskDetails) {

		return taskRepository.save(projectTaskDetails);
	}

	@Override
	public List<EmployeeTaskListDTO> getTaskList(Long employeeInfoId, List<Long> projectIdList, Long companyId) {

		List<EmployeeTaskListDTO> taskListDTO = new ArrayList<>();

		List<EmployeePersonalInfo> personalInfo = personalInfoRepository
				.findByEmployeeInfoIdAndCompanyInfoCompanyId(employeeInfoId, companyId);

		if (personalInfo.isEmpty())
			throw new DataNotFoundException("Employee Not Found");

		if (personalInfo.get(0).getEmployeeOfficialInfo() == null) {
			throw new DataNotFoundException("Employee OfficialInfo Not Found");
		}
		List<ProjectDetails> allocatedProjectList = personalInfo.get(0).getAllocatedProjectList();

		if (allocatedProjectList.isEmpty())
			throw new DataNotFoundException("Project Not Allocated");

		for (ProjectDetails projectDetail : allocatedProjectList) {

			List<ProjectTaskDetails> taskLists = taskRepository.findByProjectId(projectDetail.getProjectId());

			for (ProjectTaskDetails taskDetail : taskLists) {
				if ((taskDetail.getAssignedEmployee() != null) && (taskDetail.getAssignedEmployee())
						.equals((personalInfo.get(0).getEmployeeOfficialInfo().getEmployeeId()))) {
					taskListDTO.add(new EmployeeTaskListDTO(taskDetail.getTaskId(), taskDetail.getTaskName()));
				}
			}
		}
		if (taskListDTO.isEmpty())
			throw new DataNotFoundException("Task Not Assigned");

		return taskListDTO;
	}


	@Override
	public EmployeeTimesheetDetailsDTO saveEmployeeTimesheetDetails(
			EmployeeTimesheetDetailsDTO employeeTimesheetDetailsDTO, Long employeeInfoId, Long companyId) {

		EmployeeTimesheetDetailsDTO employeeTimesheetDetailsDTO2 = new EmployeeTimesheetDetailsDTO();

		EmployeeTimesheetDetails employeeTimesheetDetails = new EmployeeTimesheetDetails();

		List<EmployeePersonalInfo> employeePersonalInfo = personalInfoRepository
				.findByEmployeeInfoIdAndCompanyInfoCompanyId(employeeInfoId, companyId);
		if (employeePersonalInfo.isEmpty()) {
			throw new DataNotFoundException("PersonalInfo Not Found");
		}
		if (employeePersonalInfo.get(0).getEmployeeOfficialInfo() == null) {
			throw new DataNotFoundException("OfficialInfo Not Found");
		}
		if (employeeTimesheetDetailsDTO.getId() != null) {

			employeeTimesheetDetails = timeSheetRepository
					.findByTimesheetObjectIdAndCompanyId(employeeTimesheetDetailsDTO.getId(), companyId).get();
			if (employeeTimesheetDetails.getIsSubmitted().equals(Boolean.TRUE)) {
				throw new DataNotFoundException("Timesheet Cannot be edited");
			}
		} else {
			employeeTimesheetDetails = new EmployeeTimesheetDetails();
		}
		List<Timesheet> dtoTimesheets = employeeTimesheetDetailsDTO.getTimesheets();
		for (int i = 0; i < dtoTimesheets.size(); i++) {
			Timesheet timesheet = dtoTimesheets.get(i);
			timesheet.setId(Integer.toString(i + 1));
		}		
		BeanUtils.copyProperties(employeeTimesheetDetailsDTO, employeeTimesheetDetails);
		employeeTimesheetDetails.setIsApproved(Boolean.FALSE);
		BeanUtils.copyProperties(timeSheetRepository.save(employeeTimesheetDetails), employeeTimesheetDetailsDTO2);
		employeeTimesheetDetailsDTO2.setId(employeeTimesheetDetails.getTimesheetObjectId());
		employeeTimesheetDetailsDTO2.setTimesheetId(employeeTimesheetDetails.getTimesheetId());
		return employeeTimesheetDetailsDTO2;

	}


//New
	@Override
	public EmployeeTimesheetDetailsDTO getTimesheetDetails(
			EmployeeTimesheetConfigurationDTO EmployeeTimesheetConfigurationDTO) {

		List<EmployeePersonalInfo> employeePersonalInfo = personalInfoRepository
				.findByEmployeeInfoIdAndCompanyInfoCompanyId(EmployeeTimesheetConfigurationDTO.getEmployeeInfoId(),
						EmployeeTimesheetConfigurationDTO.getCompanyId());
		if (employeePersonalInfo.isEmpty()) {
			throw new DataNotFoundException("PersonalInfo Not found");
		}

		Optional<EmployeeTimesheetDetails> employeeTimesheetDetails = timeSheetRepository
				.findByCompanyIdAndEmployeeIdAndYearAndMonth(EmployeeTimesheetConfigurationDTO.getCompanyId(),
						employeePersonalInfo.get(0).getEmployeeOfficialInfo().getEmployeeId(),
						EmployeeTimesheetConfigurationDTO.getYear(), EmployeeTimesheetConfigurationDTO.getMonth());

		if (employeeTimesheetDetails.isEmpty()) {
			throw new DataNotFoundException("EmployeeTimesheet Details Not Found");
		}
		EmployeeTimesheetDetailsDTO employeeTimesheetDetailsDTO = new EmployeeTimesheetDetailsDTO();

		List<Timesheet> timesheetDTOList = new ArrayList<>();

		if (EmployeeTimesheetConfigurationDTO.getFrom() != null && EmployeeTimesheetConfigurationDTO.getTo() == null) {
			for (Timesheet timesheet : employeeTimesheetDetails.get().getTimesheets()) {
				if (timesheet.getDate().getDayOfMonth() == EmployeeTimesheetConfigurationDTO.getFrom())
					timesheetDTOList.add(timesheet);
			}
		} else if (EmployeeTimesheetConfigurationDTO.getFrom() != null
				&& EmployeeTimesheetConfigurationDTO.getTo() != null) {
	
			for (Timesheet timesheet : employeeTimesheetDetails.get().getTimesheets()) {
				if (timesheet.getDate().getDayOfMonth() >= EmployeeTimesheetConfigurationDTO.getFrom()
					&& timesheet.getDate().getDayOfMonth() <= EmployeeTimesheetConfigurationDTO.getTo()) {
					timesheetDTOList.add(timesheet);
				
				}
			}
		} else if (EmployeeTimesheetConfigurationDTO.getFrom() == null
				&& EmployeeTimesheetConfigurationDTO.getTo() == null) {

			timesheetDTOList.addAll(employeeTimesheetDetails.get().getTimesheets());

		} else {
			throw new DataNotFoundException("Enter Proper Values for from and To ");
		}

		List<Timesheet> timesheetList = new ArrayList<>();

		for (Timesheet timesheet : timesheetDTOList) {
			Timesheet timesheetDTO = new Timesheet();
			if (!EmployeeTimesheetConfigurationDTO.getProjectNames().isEmpty()) {
				for (String projectName : EmployeeTimesheetConfigurationDTO.getProjectNames()) {
					if (timesheet.getProject().equalsIgnoreCase(projectName)) {
						BeanUtils.copyProperties(timesheet, timesheetDTO);
						timesheetList.add(timesheetDTO);
					}
				}
			} else {
				BeanUtils.copyProperties(timesheet, timesheetDTO);
				timesheetList.add(timesheetDTO);
			}
		}
		BeanUtils.copyProperties(employeeTimesheetDetails.get(), employeeTimesheetDetailsDTO);
		employeeTimesheetDetailsDTO.setId(employeeTimesheetDetails.get().getTimesheetObjectId());
		employeeTimesheetDetailsDTO.setEmployeeId(employeeTimesheetDetails.get().getEmployeeId());
		employeeTimesheetDetailsDTO.setTimesheets(timesheetList);
		return employeeTimesheetDetailsDTO;
	}



	@Override
	public void deleteEmployeeTimeSheet(Long employeeInfoId, String timesheetObjectId, String id, Long companyId) {

		List<EmployeePersonalInfo> employeePersonalInfo = personalInfoRepository
				.findByEmployeeInfoIdAndCompanyInfoCompanyId(employeeInfoId, companyId);

		if (employeePersonalInfo.isEmpty()) {
			throw new DataNotFoundException("PersonalInfo No Found");
		}
		EmployeeTimesheetDetails employeeTimesheetDetails = timeSheetRepository
				.findByTimesheetObjectIdAndCompanyId(timesheetObjectId, companyId).get();
		if (employeeTimesheetDetails == null) {
			throw new DataNotFoundException("Employee Timesheet Details Not Found");
		}
		List<Timesheet> oldTimesheets = employeeTimesheetDetails.getTimesheets();
		List<Timesheet> newTimesheets = new ArrayList();
		for (Timesheet timesheet : oldTimesheets) {
			if (!(timesheet.getId().equals(id))) {
				newTimesheets.add(timesheet);
			}
		}
		employeeTimesheetDetails.setTimesheets(newTimesheets);
		timeSheetRepository.save(employeeTimesheetDetails);
	}

}
