/**
 * 
 */
package com.stanzaliving.user.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.user.entity.OtpEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Repository
public interface OtpRepository extends AbstractJpaRepository<OtpEntity, Long> {

	List<OtpEntity> findByMobileAndOtpTypeAndIsoCode(String mobile, OtpType otpType, String isoCode, Pageable pageable);
	
	List<OtpEntity> findByMobileAndOtpTypeAndIsoCodeAndStatus(String mobile, OtpType otpType, String isoCode, boolean status, Pageable pageable);

	List<OtpEntity> findByUserIdAndOtpType(String userId, OtpType otpType, Pageable pageable);

	List<OtpEntity> findByEmailAndOtpTypeAndUserIdAndStatus(String email, OtpType otpType, String userUuid, boolean status, Pageable pageable);
}