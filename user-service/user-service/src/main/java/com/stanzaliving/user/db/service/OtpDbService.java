/**
 * 
 */
package com.stanzaliving.user.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.user.entity.OtpEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface OtpDbService extends AbstractJpaService<OtpEntity, Long> {

	OtpEntity getOtpForMobile(String mobile, OtpType otpType, String isoCode);

	OtpEntity getActiveOtpForMobile(String mobile, OtpType otpType, String isoCode);

	OtpEntity getUserOtpByUserId(String userId, OtpType otpType);

	OtpEntity getActiveOtpByEmailAndUserUuidAndOtpType(String email, String userUuid, OtpType otpType);

}