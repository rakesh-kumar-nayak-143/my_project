package com.te.flinko.entity.admin;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PreRemove;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.te.flinko.audit.Audit;
import com.te.flinko.entity.employee.EmployeeOfficialInfo;
import com.te.flinko.util.JsonToStringConverter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fa_company_address_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "companyAddressId")
public class CompanyAddressInfo extends Audit implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "cai_company_address_id", unique = true, nullable = false, precision = 19)
	private Long companyAddressId;
	@Column(name = "cai_address_type", length = 25)
	private String addressType;
	@Column(name = "cai_address_details", length = 255)
	private String addressDetails;
	@Column(name = "cai_city", length = 25)
	private String city;
	@Column(name = "cai_state", length = 25)
	private String state;
	@Column(name = "cai_country", length = 25)
	private String country;
	@Column(name = "cai_pincode", precision = 19)
	private Long pincode;
	@Column(name = "cai_latitude")
	private Double latitude;
	@Column(name = "cai_longitude")
	private Double longitude;
	@Column(name = "cai_radius")
	private Double radius;
	@Convert(converter = JsonToStringConverter.class)
	@Column(name = "cai_geo_fencing_location")
	private Object geoFencingLocation;
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "cai_branch_id")
	private CompanyBranchInfo companyBranchInfo;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "companyAddressInfo")
	private List<EmployeeOfficialInfo> employeeOfficialInfoList;

	@PreRemove
	public void remove() {
		companyBranchInfo = null;
		employeeOfficialInfoList = employeeOfficialInfoList.stream().map(x -> {
			x.setCompanyAddressInfo(null);
			return x;
		}).collect(Collectors.toList());
	}
}
