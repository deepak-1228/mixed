/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.StanzaConstants;
import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.core.base.exception.AuthException;
import com.stanzaliving.core.base.utils.DateUtil;
import com.stanzaliving.core.base.utils.PhoneNumberUtils;
import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.user.constants.UserErrorCodes.Otp;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.core.user.request.dto.EmailOtpValidateRequestDto;
import com.stanzaliving.core.user.request.dto.EmailVerificationRequestDto;
import com.stanzaliving.core.user.request.dto.LoginRequestDto;
import com.stanzaliving.core.user.request.dto.MobileEmailOtpRequestDto;
import com.stanzaliving.core.user.request.dto.MobileOtpRequestDto;
import com.stanzaliving.core.user.request.dto.OtpValidateRequestDto;
import com.stanzaliving.user.constants.UserConstants;
import com.stanzaliving.user.db.service.OtpDbService;
import com.stanzaliving.user.entity.OtpEntity;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.kafka.service.KafkaUserService;
import com.stanzaliving.user.service.OtpService;
import com.stanzaliving.user.service.PauseOtpService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@Service
public class OtpServiceImpl implements OtpService {

	@Value("${environment.type}")
	private String environment;

	@Value("${test.mobile}")
	private String testMobile;

	@Value("${otp.length:4}")
	private int otpLength;

	@Value("${otp.expiry.minutes:5}")
	private int otpExpiryMinutes;

	@Value("${otp.max.resend.count:5}")
	private int otpMaxResendCount;

	@Value("${otp.resend.enable.seconds:30}")
	private int otpResendEnableSeconds;
	
	@Value("${otp.max.validate.count:5}")
	private int otpMaxValidateCount;

	@Autowired
	private OtpDbService otpDbService;

	@Autowired
	private KafkaUserService kafkaUserService;
	
	@Autowired
	private PauseOtpService blacklistUserService;

	@Override
	public void sendLoginOtp(UserEntity userEntity) {

		OtpEntity currentOtp = otpDbService.getUserOtpByUserId(userEntity.getUuid(), OtpType.LOGIN);
		OtpEntity userOtp;

		if (currentOtp == null
				|| !(userEntity.getMobile().equals(currentOtp.getMobile()) && userEntity.getIsoCode().equals(currentOtp.getIsoCode()))) {
			userOtp = createOtpForUser(userEntity, OtpType.LOGIN);
		} else {
			currentOtp.setResendCount(UserConstants.ZERO);
			currentOtp.setValidateCount(UserConstants.ZERO);
			userOtp = updateUserOtp(currentOtp);
		}

		log.info("Sending OTP: " + userOtp.getOtp() + " for User: " + userOtp.getUserId() + " for login");
		
		if(!blacklistUserService.checkIfNeedToStop(userEntity.getMobile())) {
			kafkaUserService.sendOtpToKafka(userOtp, null);
		}else {
			log.info("Not sending as user is blacklisted");
		}
	}

	private OtpEntity createOtpForUser(UserEntity user, OtpType otpType) {
		OtpEntity userOtp = new OtpEntity();

		userOtp.setUserId(user.getUuid());
		userOtp.setUserType(user.getUserType());

		return setOtpDetailsAndSave(user.getMobile(), user.getIsoCode(), user.getEmail(), otpType, userOtp);
	}

	private OtpEntity setOtpDetailsAndSave(String mobile, String isoCode, String email, OtpType otpType, OtpEntity otpEntity) {

		otpEntity.setMobile(PhoneNumberUtils.normalizeNumber(mobile));
		otpEntity.setIsoCode(isoCode);

		otpEntity.setOtp(generateOtp(otpEntity));
		otpEntity.setOtpType(otpType);

		otpEntity.setEmail(email);
		otpEntity.setValidateCount(UserConstants.ZERO);
		otpEntity.setResendCount(UserConstants.ZERO);
		otpEntity = otpDbService.saveAndFlush(otpEntity);

		return otpEntity;
	}

	/**
	 * Method to generate fix OTP for dev & test environments and configured
	 * mobile numbers and dynamic OTP for rest if the cases
	 * 
	 * @return OTP to send
	 */
	private Integer generateOtp(OtpEntity userOtp) {

		// return StanzaUtils.generateDefaultOtpOfLength(otpLength);
		 return isTestEnvironment() || isTestMobile(userOtp)  ? StanzaUtils.generateDefaultOtpOfLength(otpLength) : StanzaUtils.generateOTPOfLength(otpLength);
	}

	private boolean isTestEnvironment() {
		return StringUtils.isNotBlank(environment)
				&& ("dev".equalsIgnoreCase(environment)
						|| "staging".equalsIgnoreCase(environment)
						|| "test".equalsIgnoreCase(environment));
	}

	private boolean isTestMobile(OtpEntity userOtp) {

		if (StringUtils.isNotBlank(testMobile)
				&& StringUtils.isNotBlank(userOtp.getMobile())
				&& StringUtils.isNotBlank(userOtp.getIsoCode())) {

			String[] mobileNos = testMobile.split(",");

			Set<String> mobiles = Arrays.stream(mobileNos).collect(Collectors.toSet());

			return userOtp.getMobile().startsWith("2") || mobiles.contains(userOtp.getMobile());
		}

		return false;
	}

	private OtpEntity updateUserOtp(OtpEntity userOtp) {
		userOtp.setOtp(generateOtp(userOtp));
		userOtp.setStatus(true);

		log.debug("Updating OTP for User [Id: {}, Mobile: {}]", userOtp.getUserId(), userOtp.getMobile());
		userOtp = otpDbService.updateAndFlush(userOtp);

		return userOtp;
	}

	@Override
	public void validateLoginOtp(OtpValidateRequestDto otpValidateRequestDto) {
		validateMobileOtp(otpValidateRequestDto.getMobile(), otpValidateRequestDto.getIsoCode(), otpValidateRequestDto.getOtp(), OtpType.LOGIN);
	}

	@Override
	public void validateMobileOtp(String mobile, String isoCode, String otp, OtpType otpType) {
		OtpEntity userOtp = getLastActiveOtp(mobile, isoCode, otpType);

		compareOTP(otp, userOtp);

		log.debug("OTP validated for mobile: " + userOtp.getMobile());

		expireOtp(userOtp);
	}

	private OtpEntity getLastActiveOtp(String mobile, String isoCode, OtpType otpType) {

		return otpDbService.getActiveOtpForMobile(
				PhoneNumberUtils.normalizeNumber(mobile), otpType, isoCode);

	}

	private void compareOTP(String otp, OtpEntity userOtp) {
		Date otpTime = new Date(
				LocalDateTime.now(StanzaConstants.IST_TIMEZONEID).atZone(StanzaConstants.IST_TIMEZONEID).toInstant().getEpochSecond() - (otpExpiryMinutes * 60000));

		if (userOtp == null) {
			throw new AuthException("No OTP exists for mobile or email", Otp.OTP_NOT_FOUND);
		}

		if (!userOtp.isStatus()
				|| userOtp.getUpdatedAt() == null
				|| userOtp.getUpdatedAt().before(otpTime)
				|| !userOtp.getOtp().toString().equals(otp)) {

			if (!userOtp.getOtp().toString().equals(otp)) {
				
				if (userOtp.getValidateCount() >= otpMaxValidateCount) {
					throw new AuthException("Oops! Too many failed attempts. Please request for new OTP and try again.",Otp.OTP_VALIDATE_LIMIT_EXHAUSTED);
				} else {
					validateCount(userOtp);
				}
			}
			
			switch (userOtp.getOtpType()) {

			case EMAIL_VERIFICATION:
				throw new ApiValidationException("OTP you've entered is incorrect. Please try again!");

			default:
				throw new AuthException("Invalid OTP For User With Mobile " + userOtp.getMobile(), Otp.INVALID_OTP);
			}
		}
	}

	private void expireOtp(OtpEntity otp) {
		otp.setStatus(false);

		log.debug("Marking OTP Expired for User [Id: {}, Mobile: {}, Email: {}]", otp.getUserId(), otp.getMobile(), otp.getEmail());

		otpDbService.updateAndFlush(otp);
	}

	@Override
	public void resendLoginOtp(LoginRequestDto loginRequestDto) {
		resendMobileOtp(loginRequestDto.getMobile(), loginRequestDto.getIsoCode(), OtpType.LOGIN);
	}

	@Override
	public void resendMobileOtp(String mobile, String isoCode, OtpType otpType) {
		OtpEntity userOtp = getLastActiveOtp(mobile, isoCode, otpType);

		if (userOtp == null) {

			throw new AuthException("No OTP found for Mobile: " + mobile + ", ISOCode: " + isoCode + ", OtpType: " + otpType, Otp.OTP_NOT_FOUND);

		} else {
			checkForOtpResendConditions(userOtp);
		}

		userOtp.setResendCount(userOtp.getResendCount() + 1);
		userOtp.setValidateCount(UserConstants.ZERO);
		
		log.info("Updating OTP for Mobile: {}", userOtp.getMobile());
		userOtp = otpDbService.updateAndFlush(userOtp);

		log.info("Re-Sending OTP: " + userOtp.getOtp() + " for Mobile: " + userOtp.getMobile() + " of Type " + otpType);
		kafkaUserService.sendOtpToKafka(userOtp, null);
	}

	@Override
	public void sendMobileOtp(MobileOtpRequestDto mobileOtpRequestDto) {

		log.debug("Searching current OTP for Mobile: {}, ISO: {} of Type: {}",
				mobileOtpRequestDto.getMobile(), mobileOtpRequestDto.getIsoCode(), mobileOtpRequestDto.getOtpType());

		OtpEntity currentOtp =
				otpDbService.getOtpForMobile(mobileOtpRequestDto.getMobile(), mobileOtpRequestDto.getOtpType(), mobileOtpRequestDto.getIsoCode());

		OtpEntity mobileOtp;

		if (currentOtp == null) {

			mobileOtp = OtpEntity.builder().userType(mobileOtpRequestDto.getUserType()).build();

			setOtpDetailsAndSave(mobileOtpRequestDto.getMobile(), mobileOtpRequestDto.getIsoCode(), null, mobileOtpRequestDto.getOtpType(), mobileOtp);

		} else {

			currentOtp.setResendCount(0);
			mobileOtp = updateUserOtp(currentOtp);
		}

		log.info("Sending OTP: {} for Mobile: {} for ", mobileOtp.getOtp(), mobileOtp.getMobile(), mobileOtp.getOtpType());
		kafkaUserService.sendOtpToKafka(mobileOtp, null);

	}

	@Override
	public void sendOtp(MobileEmailOtpRequestDto mobileEmailOtpRequestDto) {

		log.debug("Searching current OTP for Mobile: {}, ISO: {} of Type: {}",
				mobileEmailOtpRequestDto.getMobile(), mobileEmailOtpRequestDto.getIsoCode(), mobileEmailOtpRequestDto.getOtpType());

		OtpEntity currentOtp =
				otpDbService.getOtpForMobile(mobileEmailOtpRequestDto.getMobile(), mobileEmailOtpRequestDto.getOtpType(), mobileEmailOtpRequestDto.getIsoCode());

		OtpEntity otpEntity;

		if (currentOtp == null) {

			otpEntity = OtpEntity.builder().userType(mobileEmailOtpRequestDto.getUserType()).build();

			setOtpDetailsAndSave(
					mobileEmailOtpRequestDto.getMobile(), mobileEmailOtpRequestDto.getIsoCode(), mobileEmailOtpRequestDto.getEmail(), mobileEmailOtpRequestDto.getOtpType(), otpEntity);

		} else {

			currentOtp.setResendCount(0);
			otpEntity = updateUserOtp(currentOtp);
		}

		log.info("Sending OTP: {} for Mobile: {} and Email: {} for ", otpEntity.getOtp(), otpEntity.getMobile(), otpEntity.getEmail(), otpEntity.getOtpType());
		kafkaUserService.sendOtpToKafka(otpEntity, null);

	}

	@Override
	public void sendEmailOtp(UserEntity userEntity, String email) {

		OtpType otpType = OtpType.EMAIL_VERIFICATION;
		
		OtpEntity currentOtp = otpDbService.getUserOtpByUserId(userEntity.getUuid(), otpType);
		
		OtpEntity userOtp;

		if (currentOtp == null 
				|| !(email.equals(currentOtp.getEmail()))
				|| !(userEntity.getMobile().equals(currentOtp.getMobile()) && userEntity.getIsoCode().equals(currentOtp.getIsoCode()))) {

			userOtp = new OtpEntity();

			userOtp.setUserId(userEntity.getUuid());
			userOtp.setUserType(userEntity.getUserType());

			userOtp = setOtpDetailsAndSave(userEntity.getMobile(), userEntity.getIsoCode(), email, otpType, userOtp);
			//userOtp.setOtp(4567);
		} else {

			currentOtp.setResendCount(0);
			//currentOtp.setOtp(4567);
			userOtp = updateUserOtp(currentOtp);
		}

		log.info("Sending OTP: " + userOtp.getOtp() + " for User: " + userOtp.getUserId() + " for email verification of email: " + email);
		
		kafkaUserService.sendOtpToKafka(userOtp, userEntity);
	}

	@Override
	public void validateEmailVerificationOtp(EmailOtpValidateRequestDto emailOtpValidateRequestDto) {
		
		OtpType otpType = OtpType.EMAIL_VERIFICATION;

		OtpEntity userOtp = getLastActiveOtpForEmailVerification(emailOtpValidateRequestDto.getEmail(), emailOtpValidateRequestDto.getUserUuid(), otpType);

		if (userOtp == null) {

			throw new AuthException("No OTP found for Email: " + emailOtpValidateRequestDto.getEmail() + ", OtpType: " + otpType, Otp.OTP_NOT_FOUND);
		}
		
		compareOTP(emailOtpValidateRequestDto.getOtp(), userOtp);

		log.debug("OTP verified for email: " + userOtp.getEmail());

		expireOtp(userOtp);
	}

	private OtpEntity getLastActiveOtpForEmailVerification(String email, String userUuid, OtpType otpType) {
		
		return otpDbService.getActiveOtpByEmailAndUserUuidAndOtpType(email, userUuid, otpType);
	}

	@Override
	public void resendEmailVerificationOtp(EmailVerificationRequestDto emailVerificationRequestDto, UserEntity userEntity) {
			
		OtpType otpType = OtpType.EMAIL_VERIFICATION;
		
		OtpEntity userOtp = getLastActiveOtpForEmailVerification(emailVerificationRequestDto.getEmail(), emailVerificationRequestDto.getUserUuid(), otpType);

		if (userOtp == null) {

			throw new ApiValidationException("No OTP found for Email: " + emailVerificationRequestDto.getEmail() + ", OtpType: " + otpType);

		} else {
			checkForOtpResendConditions(userOtp);
		}

		userOtp.setResendCount(userOtp.getResendCount() + 1);
		
		userOtp.setOtp(generateOtp(userOtp));
		
		log.info("Updating OTP for email: {} new OTP is: {}", emailVerificationRequestDto.getEmail(), userOtp.getOtp()) ;
		userOtp = otpDbService.updateAndFlush(userOtp);

		log.info("Re-Sending OTP: " + userOtp.getOtp() + " for email: " + userOtp.getEmail() + " of Type " + otpType);
		kafkaUserService.sendOtpToKafka(userOtp, userEntity);
	}

	private void checkForOtpResendConditions(OtpEntity userOtp) {
		
		if (userOtp.getResendCount() >= otpMaxResendCount) {
			throw new AuthException("Resend OTP can be used maximum " + otpMaxResendCount + " times.", Otp.OTP_RESEND_LIMIT_EXHAUSTED);
		}
		
		LocalDateTime currentTime = LocalDateTime.now(StanzaConstants.IST_TIMEZONEID);
		LocalDateTime otpResendEnableTime = DateUtil.convertToLocalDateTime(userOtp.getUpdatedAt()).plusSeconds(otpResendEnableSeconds);

		if (currentTime.isBefore(otpResendEnableTime)) {

			long secondsRemaining = ChronoUnit.SECONDS.between(currentTime, otpResendEnableTime);
			throw new AuthException("OTP Can be Resent Only After " + secondsRemaining + " Seconds", Otp.OTP_RESEND_NOT_PERMITTED);
		}
	}

	private void validateCount(OtpEntity otp) {

		otp.setValidateCount(otp.getValidateCount() + 1);

		log.debug("Increase validate count for User [Id: {}, Mobile: {}, Email: {}]", otp.getUserId(), otp.getMobile(),
				otp.getEmail());

		otpDbService.updateAndFlush(otp);
	}

	@Override
	public int getOtp(String mobile, String isoCode, OtpType otpType) {

		OtpEntity otpEntity = getLastActiveOtp(mobile, isoCode, otpType);

		if (Objects.isNull(otpEntity)) {
			throw new AuthException("No OTP exists for mobile", Otp.OTP_NOT_FOUND);
		}

		return otpEntity.getOtp();
	}
	@Override
	public void resendMobileOtpV2(MobileOtpRequestDto mobileOtpRequestDto, String mobile, String isoCode, OtpType otpType) {
		OtpEntity userOtp = getLastActiveOtp(mobile, isoCode, otpType);

		if (Objects.isNull(userOtp)) {
			sendMobileOtp(mobileOtpRequestDto);
			return;
		} else {
			checkForOtpResendConditions(userOtp);
		}
		userOtp.setResendCount(userOtp.getResendCount() + 1);
		userOtp.setValidateCount(UserConstants.ZERO);

		log.info("Updating OTP for Mobile: {}", userOtp.getMobile());
		userOtp = otpDbService.updateAndFlush(userOtp);

		log.info("Re-Sending OTP: " + userOtp.getOtp() + " for Mobile: " + userOtp.getMobile() + " of Type " + otpType);
		kafkaUserService.sendOtpToKafka(userOtp, null);
	}

}