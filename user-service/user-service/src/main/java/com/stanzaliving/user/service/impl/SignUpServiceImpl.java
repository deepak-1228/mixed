package com.stanzaliving.user.service.impl;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.stanzaliving.user.service.RedisOperationsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

import com.stanzaliving.core.base.StanzaConstants;
import com.stanzaliving.core.base.exception.AuthException;
import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.user.constants.UserErrorCodes.Otp;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.acl.adapters.SignUpAdapter;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.db.service.SignUpDbService;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.OtpEntity;
import com.stanzaliving.user.entity.SignupEntity;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.kafka.service.KafkaUserService;
import com.stanzaliving.user.service.SignUpService;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class SignUpServiceImpl implements SignUpService {

	@Value("${otp.length:4}")
	private int otpLength;

	@Value("${kafka.resident.detail.topic}")
	private String kafkaResidentDetailTopic;
	
	@Value("${environment.type}")
	private String environment;

	@Value("${test.mobile}")
	private String testMobile;

	@Value("${otp.max.validated.count:2}")
	private int otpMaxValidatedCount;

	@Autowired
	private KafkaUserService kafkaUserService;

	@Autowired
	private SignUpDbService signUpDbService;
	@Autowired
	private UserService userService;

	@Autowired
	private UserDbService userDbService;

	@Autowired
	private RedisOperationsService redisOperationsService;

	@Override
	public String signUpUser(AddUserRequestDto addUserRequestDto) {

		int otp = generateOtp(addUserRequestDto);

		addUserRequestDto.setSignupFlow(true);
		SignupEntity signUp = SignUpAdapter.getSignupEntity(addUserRequestDto, otp);

		signUp = signUpDbService.save(signUp);

		sendOtp(addUserRequestDto, signUp);

		return signUp.getUuid();
	}

	@Override
	public UserProfileDto validateSignUpOtp(String uuid, String otp) {

		SignupEntity signup = signUpDbService.findByUuidAndStatus(uuid, Boolean.TRUE);

		compareOTP(otp, signup);
		UserDto userDto = userService.addUser(signup.getSignupObject());
		if (Objects.isNull(userDto))
			throw new AuthException("user Not created.");

		UserEntity userEntity = userDbService.getUserForMobile(signup.getMobile(), StanzaConstants.INDIA_ISO_CODE);

		return UserAdapter.getUserProfileDto(userEntity);
	}

	private boolean compareOTP(String otp, SignupEntity signup) {

		if (signup == null)
			throw new AuthException("No OTP exists for mobile", Otp.OTP_NOT_FOUND);

		String userType = "";
		if(Objects.nonNull(signup.getSignupObject()) && Objects.nonNull(signup.getSignupObject().getUserType()))
			userType = signup.getSignupObject().getUserType().getTypeName();
		String key = "";
		if(StringUtils.isNotBlank(userType))
			key = "User_" + signup.getMobile() + "_" + userType + "_" + signup.getOtp();
		else
			key = "User_" + signup.getMobile() + "_" + signup.getOtp();

		if (!signup.isValidated() && signup.getOtp().toString().equals(otp)) {
			signup.setValidated(true);
			signUpDbService.saveAndFlush(signup);
			saveInMap(key, "1");
		} else {
			if(isValidationAllowed(key))
				return Boolean.TRUE;
			else
				throw new AuthException("Invalid OTP For User With Mobile " + signup.getMobile(), Otp.INVALID_OTP);
		}

		return Boolean.TRUE;
	}

	private boolean isValidationAllowed(String key) {
		try {
			Object object = redisOperationsService.getFromMap("USER_VALIDATION_COUNT_MAPPING", key);
			if (Objects.isNull(object)) {
				return false;
			}
			int count = Integer.parseInt(String.valueOf(object));
			count++;
			if(count > otpMaxValidatedCount) {
				return false;
			} else {
				saveInMap(key, String.valueOf(count));
				return true;
			}
		} catch (Exception e) {
			log.error("Exception in isValidationAllowed for key {}, Exception is {}", key, e);
			return false;
		}
	}

	private void saveInMap(String key, String value) {
		try {
			redisOperationsService.putToMap("USER_VALIDATION_COUNT_MAPPING", key, value, 10, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("Exception in saveInMap for key {}, Exception is {}", key, e);
		}
	}

	private void sendOtp(AddUserRequestDto addUserRequestDto, SignupEntity signUpOtp) {

		OtpEntity userOtp = OtpEntity.builder().otp(signUpOtp.getOtp()).mobile(signUpOtp.getMobile())
				.otpType(OtpType.MOBILE_VERIFICATION)
				.userType(addUserRequestDto.getUserType()).status(Boolean.TRUE).build();

		log.info("Sending Otp for Signup, {}", userOtp);

		kafkaUserService.sendOtpToKafka(userOtp, null);

	}

	private Integer generateOtp(AddUserRequestDto addUserRequestDto) {
		return isTestEnvironment() || isTestMobile(addUserRequestDto) ? StanzaUtils.generateDefaultOtpOfLength(otpLength) : StanzaUtils.generateOTPOfLength(otpLength);
	}

	private boolean isTestEnvironment() {
		return StringUtils.isNotBlank(environment)
				&& ("dev".equalsIgnoreCase(environment)
						|| "staging".equalsIgnoreCase(environment)
						|| "test".equalsIgnoreCase(environment));
	}

	private boolean isTestMobile(AddUserRequestDto addUserRequestDto) {

		if (StringUtils.isNotBlank(testMobile)
				&& StringUtils.isNotBlank(addUserRequestDto.getMobile())
				&& StringUtils.isNotBlank(addUserRequestDto.getIsoCode())) {

			String[] mobileNos = testMobile.split(",");

			Set<String> mobiles = Arrays.stream(mobileNos).collect(Collectors.toSet());

			return addUserRequestDto.getMobile().startsWith("2") || mobiles.contains(addUserRequestDto.getMobile());
		}

		return false;
	}
	
}
