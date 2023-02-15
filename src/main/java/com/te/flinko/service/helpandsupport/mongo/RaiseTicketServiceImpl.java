package com.te.flinko.service.helpandsupport.mongo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import com.te.flinko.audit.common.db.DBConstants;
import com.te.flinko.beancopy.BeanCopy;
import com.te.flinko.constants.admin.AdminConstants;
import com.te.flinko.constants.admindept.AdminDeptConstants;
import com.te.flinko.dto.admindept.ProductNameDTO;
import com.te.flinko.dto.helpandsupport.mongo.CompanyTicketDto;
import com.te.flinko.dto.helpandsupport.mongo.RaiseTicketDto;
import com.te.flinko.dto.helpandsupport.mongo.ReportingManagerDto;
import com.te.flinko.dto.helpandsupport.mongo.TicketHistroy;
import com.te.flinko.entity.admin.CompanyInfo;
import com.te.flinko.entity.employee.EmployeeOfficialInfo;
import com.te.flinko.entity.employee.EmployeePersonalInfo;
import com.te.flinko.entity.helpandsupport.mongo.CompanyAccountTickets;
import com.te.flinko.entity.helpandsupport.mongo.CompanyAdminDeptTickets;
import com.te.flinko.entity.helpandsupport.mongo.CompanyHrTickets;
import com.te.flinko.entity.helpandsupport.mongo.CompanyItTickets;
import com.te.flinko.exception.CompanyIdNotFoundException;
import com.te.flinko.exception.admin.NoCompanyPresentException;
import com.te.flinko.exception.employee.EmployeeNotFoundException;
import com.te.flinko.exception.helpandsupport.EmployeeNotActiveException;
import com.te.flinko.exception.helpandsupport.TicketAlreadyRaisedException;
import com.te.flinko.exception.helpandsupport.WrongAttachmentFileException;
import com.te.flinko.exception.hr.CompanyNotFoundException;
import com.te.flinko.repository.admin.CompanyInfoRepository;
import com.te.flinko.repository.admindept.CompanyItTicketsRepository;
import com.te.flinko.repository.employee.EmployeeOfficialInfoRepository;
import com.te.flinko.repository.employee.EmployeePersonelInfoRepository;
import com.te.flinko.repository.helpandsupport.mongo.CompanyAccountTicketsRepository;
import com.te.flinko.repository.helpandsupport.mongo.CompanyAdminDeptTicketsRepo;
import com.te.flinko.repository.helpandsupport.mongo.CompanyHrTicketsRepository;
import com.te.flinko.util.Generator;
import com.te.flinko.util.S3UploadFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class RaiseTicketServiceImpl implements RaiseTicketService {

	private static final String SALES_PURCHASE = "salesPurchase";

	private static final String FOR_EMPLOYEE_ID = " for  ";

	private static final String FOR_SUB_CATEGORY = " in ";

	private static final String FOR_CATEGORY = " for ";

	private static final String ALREADY_PRESENT = "Already ticket raised ";

	private static final String EMPLOYEE_NOT_ACTIVE = "Employee not active";

	private static final String EMPLOYEE_NOT_FOUND = "Employee not found";

	private static final String WRONG_ATTACHMENT_FILE = "Wrong attachment file";

	private static final String CREATED = "Created";

	private Map<String, String> attachment;

	@Autowired
	private CompanyInfoRepository infoRepository;

	@Autowired
	private CompanyAccountTicketsRepository companyAccountTicketsRepository;

	@Autowired
	private CompanyAdminDeptTicketsRepo adminDeptTicketsRepo;

	@Autowired
	private EmployeePersonelInfoRepository employeePersonelInfoRepository;

	@Autowired
	private EmployeeOfficialInfoRepository employeeOfficialInfoRepository;

	@Autowired
	private CompanyItTicketsRepository companyItTicketsRepository;

	@Autowired
	private CompanyHrTicketsRepository companyHrTicketsRepository;

	@Autowired
	private Generator generator;

	@Autowired
	private S3UploadFile uploadFileService;

	@Override
	@Transactional
	public boolean createTickets(Long companyId, Long employeeInfoId, List<MultipartFile> files,
			RaiseTicketDto raiseTicketDto) {

		attachment = new HashMap<>();

		log.info("service method createTickets is called");
		CompanyInfo companyInfo = infoRepository.findByCompanyId(companyId)
				.orElseThrow(() -> new CompanyNotFoundException("Company not found"));

		if (raiseTicketDto.getDepartment().equalsIgnoreCase("ACCOUNT")) {

			return createAccountTicket(companyInfo.getCompanyId(), raiseTicketDto, employeeInfoId, files);

		}
		if (raiseTicketDto.getDepartment().equalsIgnoreCase("ADMIN")) {

			return createAdminTicket(companyInfo.getCompanyId(), raiseTicketDto, employeeInfoId, files);

		}
		if (raiseTicketDto.getDepartment().equalsIgnoreCase("IT")) {

			return saveItTickets(companyInfo.getCompanyId(), employeeInfoId, files, raiseTicketDto);
		}
		if (raiseTicketDto.getDepartment().equalsIgnoreCase("HR")) {

			return saveHrTickets(companyInfo.getCompanyId(), employeeInfoId, files, raiseTicketDto);
		}

		return true;
	}

	private String checkFile(MultipartFile multipartFile) {
		if (attachment.containsKey(multipartFile.getOriginalFilename())) {
			return attachment.get(multipartFile.getOriginalFilename());
		} else {
			String uploadFile = uploadFileService.uploadFile(multipartFile);
			attachment.put(multipartFile.getOriginalFilename(), uploadFile);
			return uploadFile;
		}
	}

	private boolean createAccountTicket(Long companyId, RaiseTicketDto raiseTicketDto, Long employeeInfoId,
			List<MultipartFile> listOfMultipartFile) {

		List<CompanyAccountTickets> accountTicketsList = new ArrayList<>();

		List<String> employees = raiseTicketDto.getCompanyTicketDto().stream().filter(x -> x.getEmployeeId() != null)
				.map(CompanyTicketDto::getEmployeeId).collect(Collectors.toList());

		if (raiseTicketDto.getCompanyTicketDto().stream()
				.noneMatch(x -> x.getCategory().equalsIgnoreCase(SALES_PURCHASE))) {
			List<EmployeeOfficialInfo> employeeIdIn = employeeOfficialInfoRepository.findByEmployeeIdIn(employees);
			if (employeeIdIn.isEmpty() && employeeIdIn.get(0) == null) {
				throw new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND);
			}
		}

		List<String> identificationList = raiseTicketDto.getCompanyTicketDto().stream()
				.filter(x -> x.getCategory().equalsIgnoreCase(SALES_PURCHASE))
				.map(CompanyTicketDto::getIdentificationNumber).collect(Collectors.toList());

		if (!identificationList.isEmpty() && identificationList.size() != new HashSet<>(identificationList).size()) {
			throw new TicketAlreadyRaisedException("Duplicate unique number");
		}

		Optional.of(raiseTicketDto.getCompanyTicketDto().stream()
				.filter(x -> x.getCategory().equalsIgnoreCase(SALES_PURCHASE))
				.map(CompanyTicketDto::getIdentificationNumber).collect(Collectors.toList()))
				.ifPresent(identificationNumberList -> companyAccountTicketsRepository
						.findByCategoryAndCompanyIdAndIdentificationNumberIn(SALES_PURCHASE, companyId,
								identificationNumberList)
						.ifPresent(x -> {
							throw new TicketAlreadyRaisedException(ALREADY_PRESENT + FOR_CATEGORY + SALES_PURCHASE);
						}));

		raiseTicketDto.getCompanyTicketDto().stream().filter(x -> x.getSubCategory() == null)
				.collect(Collectors.toList()).forEach(x -> x.setSubCategory(x.getCategory()));

		for (CompanyTicketDto companyTicketDto2 : raiseTicketDto.getCompanyTicketDto()) {

			CompanyAccountTickets accountTickets = BeanCopy.objectProperties(companyTicketDto2,
					CompanyAccountTickets.class);

			List<CompanyAccountTickets> findByCategoryAndSubCategoryAndCompanyIdAndDepartment = companyAccountTicketsRepository
					.findByCategoryAndSubCategoryAndCompanyIdAndEmployeeIdAndTicketHistroysDateAndIdentificationNumber(
							accountTickets.getCategory(), accountTickets.getSubCategory(), companyId,
							accountTickets.getEmployeeId(), LocalDate.now(), accountTickets.getIdentificationNumber());

			if (!findByCategoryAndSubCategoryAndCompanyIdAndDepartment.isEmpty()) {

				throw new TicketAlreadyRaisedException(ALREADY_PRESENT + FOR_CATEGORY
						+ findByCategoryAndSubCategoryAndCompanyIdAndDepartment.get(0).getCategory() + FOR_SUB_CATEGORY
						+ findByCategoryAndSubCategoryAndCompanyIdAndDepartment.get(0).getSubCategory()
						+ FOR_EMPLOYEE_ID
						+ findByCategoryAndSubCategoryAndCompanyIdAndDepartment.get(0).getEmployeeId());
			}

			List<TicketHistroy> list = List.of(TicketHistroy.builder().by(employeeInfoId).date(LocalDate.now())
					.department(raiseTicketDto.getDepartment()).status(CREATED).build());

			accountTickets.setCompanyId(companyId);
			accountTickets.setAccountTicketId(generator.generateSequence(DBConstants.ACCOUNT_TICKET_SEQUENCE_NAME));
			accountTickets.setTicketHistroys(list);

			if (listOfMultipartFile != null && !listOfMultipartFile.isEmpty()
					&& companyTicketDto2.getAttachmentsUrl() != null
					&& !"".equals(companyTicketDto2.getAttachmentsUrl())) {

				Optional<MultipartFile> relativeFile = listOfMultipartFile.stream()
						.filter(x -> x.getOriginalFilename().equalsIgnoreCase(accountTickets.getAttachmentsUrl()))
						.findFirst();
				if (relativeFile.isEmpty()) {
					throw new WrongAttachmentFileException(WRONG_ATTACHMENT_FILE);
				}

				String uploadFile = checkFile(relativeFile.get());

				accountTickets.setAttachmentsUrl(uploadFile);
			}
			accountTicketsList.add(accountTickets);
		}

		return !companyAccountTicketsRepository.saveAll(accountTicketsList).isEmpty();
	}

	private boolean createAdminTicket(Long companyId, RaiseTicketDto raiseTicketDto, Long employeeInfoId,
			List<MultipartFile> listOfMultipartFile) {

		raiseTicketDto.getCompanyTicketDto().stream().filter(x -> x.getSubCategory() == null)
				.collect(Collectors.toList()).forEach(x -> x.setSubCategory(x.getCategory()));

		List<CompanyAdminDeptTickets> adminDeptTicketsList = new ArrayList<>();
		for (CompanyTicketDto companyTicketDto2 : raiseTicketDto.getCompanyTicketDto()) {

			CompanyAdminDeptTickets adminDeptTicket = BeanCopy.objectProperties(companyTicketDto2,
					CompanyAdminDeptTickets.class);

			List<CompanyAdminDeptTickets> findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId = adminDeptTicketsRepo
					.findByCategoryAndSubCategoryAndCompanyIdAndEmployeeIdAndTicketHistroysDate(
							adminDeptTicket.getCategory(), adminDeptTicket.getSubCategory(), companyId,
							adminDeptTicket.getEmployeeId(), LocalDate.now());

			if (!findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId.isEmpty()) {
				throw new TicketAlreadyRaisedException(ALREADY_PRESENT + FOR_CATEGORY
						+ findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId.get(0).getCategory() + FOR_SUB_CATEGORY
						+ findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId.get(0).getSubCategory()
						+ FOR_EMPLOYEE_ID
						+ findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId.get(0).getEmployeeId());
			}

			List<TicketHistroy> list = List.of(TicketHistroy.builder().by(employeeInfoId).date(LocalDate.now())
					.department(raiseTicketDto.getDepartment()).status(CREATED).build());

			adminDeptTicket.setCompanyId(companyId);
			adminDeptTicket.setTicketHistroys(list);
			adminDeptTicket
					.setAdminTicketId(generator.generateSequence(DBConstants.ADMIN_DEPARTMENT_TICKET_SEQUENCE_NAME));
			if (listOfMultipartFile != null && !listOfMultipartFile.isEmpty()
					&& companyTicketDto2.getAttachmentsUrl() != null
					&& !"".equals(companyTicketDto2.getAttachmentsUrl())) {

				Optional<MultipartFile> relativeFile = listOfMultipartFile.stream()
						.filter(x -> x.getOriginalFilename().equalsIgnoreCase(adminDeptTicket.getAttachmentsUrl()))
						.findFirst();
				if (relativeFile.isEmpty()) {
					throw new WrongAttachmentFileException(WRONG_ATTACHMENT_FILE);
				}

				String uploadFile = checkFile(relativeFile.get());

				adminDeptTicket.setAttachmentsUrl(uploadFile);
			}
			adminDeptTicketsList.add(adminDeptTicket);
		}
		for (CompanyAdminDeptTickets adminDeptTicket1 : adminDeptTicketsList)
			adminDeptTicketsRepo.save(adminDeptTicket1);
		return true;
	}

	private boolean saveItTickets(Long companyId, Long employeeInfoId, List<MultipartFile> listOfMultipartFile,
			RaiseTicketDto raiseTicketDto) {

		raiseTicketDto.getCompanyTicketDto().stream().filter(x -> x.getSubCategory() == null)
				.collect(Collectors.toList()).forEach(x -> x.setSubCategory(x.getCategory()));

		List<CompanyItTickets> companyItTickets1 = new ArrayList<>();

		for (CompanyTicketDto companyTicketDto : raiseTicketDto.getCompanyTicketDto()) {

			List<EmployeePersonalInfo> employeePersonalInfo = employeePersonelInfoRepository
					.findByCompanyInfoCompanyIdAndEmployeeOfficialInfoEmployeeId(companyId,
							companyTicketDto.getEmployeeId());

			if (employeePersonalInfo.isEmpty() && employeePersonalInfo.get(0) == null) {
				throw new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND);
			}

			EmployeePersonalInfo employeePersonalInfo1 = employeePersonalInfo.get(0);

			if (Boolean.FALSE.equals(employeePersonalInfo1.getIsActive())) {
				throw new EmployeeNotActiveException(EMPLOYEE_NOT_ACTIVE);
			}

			CompanyItTickets companyItTickets = BeanCopy.objectProperties(companyTicketDto, CompanyItTickets.class);

			List<CompanyItTickets> companyItTicketsExist = companyItTicketsRepository
					.findByCategoryAndSubCategoryAndCompanyIdAndEmployeeIdAndTicketHistroysDate(
							companyItTickets.getCategory(), companyItTickets.getSubCategory(), companyId,
							companyItTickets.getEmployeeId(), LocalDate.now());

			if (!companyItTicketsExist.isEmpty()) {
				throw new TicketAlreadyRaisedException(
						ALREADY_PRESENT + FOR_CATEGORY + companyItTicketsExist.get(0).getCategory() + FOR_SUB_CATEGORY
								+ companyItTicketsExist.get(0).getSubCategory() + FOR_EMPLOYEE_ID
								+ companyItTicketsExist.get(0).getEmployeeId());
			}

			List<TicketHistroy> ticketHistroys = List.of(TicketHistroy.builder().by(employeeInfoId)
					.date(LocalDate.now()).department(raiseTicketDto.getDepartment()).status(CREATED).build());

			companyItTickets.setTicketHistroys(ticketHistroys);

			companyItTickets.setItTicketId(generator.generateSequence(DBConstants.IT_TICKET_SEQUENCE_NAME));

			companyItTickets.setCompanyId(companyId);

			if (listOfMultipartFile != null && !listOfMultipartFile.isEmpty()
					&& companyTicketDto.getAttachmentsUrl() != null
					&& !"".equals(companyTicketDto.getAttachmentsUrl())) {

				Optional<MultipartFile> relativeFile = listOfMultipartFile.stream()
						.filter(x -> x.getOriginalFilename().equalsIgnoreCase(companyItTickets.getAttachmentsUrl()))
						.findFirst();
				if (relativeFile.isEmpty()) {
					throw new WrongAttachmentFileException(WRONG_ATTACHMENT_FILE);
				}

				String uploadFile = checkFile(relativeFile.get());

				companyItTickets.setAttachmentsUrl(uploadFile);
			}
			companyItTickets1.add(companyItTickets);
		}

		for (CompanyItTickets itTickets : companyItTickets1) {
			companyItTicketsRepository.save(itTickets);
		}

		return true;
	}

	private Boolean saveHrTickets(Long companyId, Long employeeInfoId, List<MultipartFile> listOfMultipartFile,
			RaiseTicketDto raiseTicketDto) {

		raiseTicketDto.getCompanyTicketDto().stream().filter(x -> x.getSubCategory() == null)
				.collect(Collectors.toList()).forEach(x -> x.setSubCategory(x.getCategory()));

		List<CompanyHrTickets> companyHrTicketsList = new ArrayList<>();

		for (CompanyTicketDto companyTicketDto : raiseTicketDto.getCompanyTicketDto()) {

			List<EmployeePersonalInfo> employeePersonalInfo = employeePersonelInfoRepository
					.findByCompanyInfoCompanyIdAndEmployeeOfficialInfoEmployeeId(companyId,
							companyTicketDto.getEmployeeId());

			if (employeePersonalInfo.isEmpty() && employeePersonalInfo.get(0) == null) {
				throw new EmployeeNotFoundException(EMPLOYEE_NOT_FOUND);
			}

			EmployeePersonalInfo employeePersonalInfo1 = employeePersonalInfo.get(0);

			if (Boolean.FALSE.equals(employeePersonalInfo1.getIsActive())) {
				throw new EmployeeNotActiveException(EMPLOYEE_NOT_ACTIVE);
			}

			CompanyHrTickets companyHrTickets = BeanCopy.objectProperties(companyTicketDto, CompanyHrTickets.class);

			List<CompanyHrTickets> findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId = companyHrTicketsRepository
					.findByCategoryAndSubCategoryAndCompanyIdAndEmployeeIdAndTicketHistroysDate(
							companyHrTickets.getCategory(), companyHrTickets.getSubCategory(), companyId,
							companyHrTickets.getEmployeeId(), LocalDate.now());

			if (!findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId.isEmpty()) {
				throw new TicketAlreadyRaisedException(ALREADY_PRESENT + FOR_CATEGORY
						+ findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId.get(0).getCategory() + FOR_SUB_CATEGORY
						+ findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId.get(0).getSubCategory()
						+ FOR_EMPLOYEE_ID
						+ findByCategoryAndSubCategoryAndCompanyIdAndEmployeeId.get(0).getEmployeeId());
			}

			List<TicketHistroy> ticketHistroys = List.of(TicketHistroy.builder().by(employeeInfoId)
					.date(LocalDate.now()).department(raiseTicketDto.getDepartment()).status(CREATED).build());

			companyHrTickets.setTicketHistroys(ticketHistroys);

			companyHrTickets.setHrTicketId(generator.generateSequence(DBConstants.HR_TICKET_SEQUENCE_NAME));

			companyHrTickets.setCompanyId(companyId);

			if (listOfMultipartFile != null && !listOfMultipartFile.isEmpty()
					&& companyTicketDto.getAttachmentsUrl() != null
					&& !"".equals(companyTicketDto.getAttachmentsUrl())) {

				Optional<MultipartFile> relativeFile = listOfMultipartFile.stream()
						.filter(x -> x.getOriginalFilename().equalsIgnoreCase(companyHrTickets.getAttachmentsUrl()))
						.findFirst();

				if (relativeFile.isEmpty()) {
					throw new WrongAttachmentFileException(WRONG_ATTACHMENT_FILE);
				}

				String uploadFile = checkFile(relativeFile.get());

				companyHrTickets.setAttachmentsUrl(uploadFile);
			}

			companyHrTicketsList.add(companyHrTickets);
		}
		for (CompanyHrTickets hrTickets : companyHrTicketsList) {
			companyHrTicketsRepository.save(hrTickets);
		}
		return true;

	}

	@Override
	public List<ReportingManagerDto> getAllReportingManagaer(Long companyId, String department) {

		log.info("service method getAllReportingManagaer is called");
		CompanyInfo companyInfo = infoRepository.findById(companyId)
				.orElseThrow(() -> new NoCompanyPresentException(AdminConstants.NO_COMPANY_PRESENT_WITH_ID));
		ArrayList<ReportingManagerDto> arrayList = new ArrayList<>();
		List<ReportingManagerDto> getfrs = employeePersonelInfoRepository.findByCompanyIdAndDepartment(companyId,
				department);
		if (getfrs != null && companyInfo != null) {

			for (ReportingManagerDto reportingManagerDto : getfrs) {

				arrayList.add(new ReportingManagerDto(reportingManagerDto.getEmployeeId(),
						reportingManagerDto.getFirstName(), reportingManagerDto.getLastName()));
			}
		}
		return arrayList;
	}

	@Override
	public List<ProductNameDTO> getProducts(Long companyId) {
		log.info("Products List with respect to company Id: " + companyId);
		CompanyInfo companyInfo = infoRepository.findById(companyId)
				.orElseThrow(() -> new CompanyIdNotFoundException(AdminDeptConstants.COMPANY_NOT_FOUND));

		return companyInfo.getCompanyPurchaseOrderList().stream()
				.flatMap(x -> x.getPurchaseOrderItemsList().stream().map(y -> {
					ProductNameDTO productNameDTO = new ProductNameDTO();
					productNameDTO.setPurchaseItemId(y.getPurchaseItemId());
					productNameDTO.setProductName(y.getProductName());
					return productNameDTO;
				})).filter(distinctByKey(ProductNameDTO::getProductName)).collect(Collectors.toList());
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}
}
