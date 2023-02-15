package com.te.flinko.repository.account;

import com.te.flinko.entity.account.mongo.CompanyVendorInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyVendorInfoRepository extends MongoRepository<CompanyVendorInfo, Long> {
	
	Optional<CompanyVendorInfo> findByVendorInfoId(Long vendorInfoId);
	
    Optional<CompanyVendorInfo> findByVendorName(String contactName);
    
    List<CompanyVendorInfo> findByCompanyId(Long companyId);

	List<CompanyVendorInfo> findByVendorInfoIdAndCompanyId(Long vendorInfoId, Long companyId);

	List<CompanyVendorInfo> findByVendorInfoIdInAndAndCompanyId(List<Long> venderIds,Long companyId);

	List<CompanyVendorInfo> findByVendorInfoIdAndAndCompanyId(Long vendorId, Long companyId);
}
