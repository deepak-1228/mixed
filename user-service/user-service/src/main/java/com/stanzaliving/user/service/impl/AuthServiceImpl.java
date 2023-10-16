/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.util.Objects;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.exception.UserValidationException;
import com.stanzaliving.core.bookingservice.dto.request.*;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.client.api.BookingDataControllerApi;
import com.stanzaliving.core.leadservice.client.api.LeadserviceClientApi;
import com.stanzaliving.core.user.enums.Gender;
import com.stanzaliving.core.user.enums.Nationality;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.*;
import com.stanzaliving.user.adapters.Userv2ToUserAdapter;
import com.stanzaliving.user.dto.userv2.UpdateUserDto;
import com.stanzaliving.user.dto.userv2.UserDto;
import com.stanzaliving.user.entity.UserProfileEntity;
import com.stanzaliving.user.feignclient.UserV2FeignService;
import com.stanzaliving.user.feignclient.Userv2HttpService;
import com.stanzaliving.user.service.UserService;
import com.stanzaliving.website.response.dto.LeadDetailEntity;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.exception.AuthException;
import com.stanzaliving.core.kafka.dto.KafkaDTO;
import com.stanzaliving.core.kafka.producer.NotificationProducer;
import com.stanzaliving.core.user.constants.UserErrorCodes;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.service.AuthService;
import com.stanzaliving.user.service.OtpService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@Service
public class AuthServiceImpl implements AuthService {

	@Autowired
	private OtpService otpService;

	@Autowired
	private Userv2HttpService userv2HttpService;

	@Autowired
	private UserV2FeignService userV2FeignService;

	@Autowired
	private UserDbService userDbService;
	
	@Value("${kafka.resident.detail.topic}")
	private String kafkaResidentDetailTopic;
	
	@Autowired
	private NotificationProducer notificationProducer;

	@Autowired
	private LeadserviceClientApi leadserviceClientApi;

	@Autowired
	private UserService userService;

	@Autowired
	private BookingDataControllerApi bookingDataControllerApi;

	@Override
	public void login(LoginRequestDto loginRequestDto) {

		UserEntity userEntity = getActiveUser(loginRequestDto);

		otpService.sendLoginOtp(userEntity);

		log.info("OTP sent for User: " + userEntity.getUuid() + " for Login");

	}

	private UserEntity getActiveUser(LoginRequestDto loginRequestDto) {

		UserDto userv2 = userV2FeignService.getActiveUser(Long.parseLong(loginRequestDto.getMobile()));
		UserEntity userEntity=null;
		if(Objects.nonNull(userv2)) {
			userEntity = Userv2ToUserAdapter.getUserEntityFromUserv2(userv2);
		}
		else {
			userEntity = userDbService.getUserForMobileNotMigrated(loginRequestDto.getMobile(), loginRequestDto.getIsoCode(),false);
		}

		// userEntity = createUserIfUserIsConsumer(loginRequestDto, userEntity);

		try {
			if (Objects.isNull(userEntity)) {

				if (UserType.VENDOR == loginRequestDto.getUserType()) {
					throw new AuthException("No user exists with this number", UserErrorCodes.USER_NOT_EXISTS);
				}

				ResponseDto<LeadDetailEntity> leadDetailResponseDto = leadserviceClientApi.search(loginRequestDto.getMobile(), null);
				if(Objects.isNull(leadDetailResponseDto) || Objects.isNull(leadDetailResponseDto.getData())) {
					throw new AuthException("No user exists with this number", UserErrorCodes.USER_NOT_EXISTS);
				}
				LeadDetailEntity leadDetail = leadDetailResponseDto.getData();
				if (Objects.isNull(leadDetail) || !(leadDetail.getPhone().equals(loginRequestDto.getMobile()))) {
					throw new AuthException("No user exists with this number", UserErrorCodes.USER_NOT_EXISTS);
				} else if(StringUtils.isNotBlank(leadDetail.getLeadTag()) && leadDetail.getLeadTag().equals("GUEST_LEAD")) {
					GuestRequestPayloadDto guestRequestPayloadDto = bookingDataControllerApi.getGuestDetailsByPhone(leadDetail.getPhone()).getData();
					AddUserRequestDto addUserRequestDto = new AddUserRequestDto();
					addUserRequestDto.setMobile(leadDetail.getPhone());
					addUserRequestDto.setFirstName(leadDetail.getFirstName());
					addUserRequestDto.setLastName(leadDetail.getLastName());
					addUserRequestDto.setEmail(leadDetail.getLeadEmail());
					addUserRequestDto.setIsoCode(loginRequestDto.getIsoCode());
					addUserRequestDto.setDepartment(Department.WEB);
					addUserRequestDto.setUserType(UserType.INVITED_GUEST);
					addUserRequestDto.setGender(Gender.valueOf(guestRequestPayloadDto.getGender()));
					addUserRequestDto.setNationality(Nationality.valueOf(guestRequestPayloadDto.getNationality()));
					userService.addUser(addUserRequestDto);

					UserProfileEntity profileEntity = UserAdapter.getUserProfileEntity(addUserRequestDto);

					userEntity = UserEntity.builder().userType(addUserRequestDto.getUserType())
							.isoCode(addUserRequestDto.getIsoCode()).mobile(addUserRequestDto.getMobile()).mobileVerified(false)
							.email(addUserRequestDto.getEmail()).emailVerified(false).userProfile(profileEntity).status(true)
							.department(addUserRequestDto.getDepartment()).build();

					profileEntity.setUser(userEntity);

					userEntity = userDbService.saveAndFlush(userEntity);
				}
			}
		} catch(Exception e){
			log.error("Error in getActiveUser, error is ", e);
		}

		if (Objects.isNull(userEntity)) {
			throw new AuthException("No user exists with this number", UserErrorCodes.USER_NOT_EXISTS);
		}

		if (!userEntity.isStatus()) {
			throw new AuthException("The booking is disabled for this number", UserErrorCodes.USER_ACCOUNT_INACTIVE);
		}

		log.info("Found User: " + userEntity.getUuid() + " for Mobile: " + loginRequestDto.getMobile() + " of Type: " + userEntity.getUserType());

		return userEntity;
	}

	@Override
	public UserProfileDto validateOtp(OtpValidateRequestDto otpValidateRequestDto) {

		UserEntity userEntity = getActiveUser(otpValidateRequestDto);

		otpService.validateLoginOtp(otpValidateRequestDto);

		log.info("OTP verification completed for User: " + userEntity.getUuid());

		userEntity.setMobileVerified(true);

		//call userv2 to update
		if(Objects.nonNull(userDbService.findByUuidAndStatus(userEntity.getUuid(),true))) {
			userDbService.update(userEntity);
		}
		userV2FeignService.updateUser(UpdateUserDto.builder()
						.mobile(Long.parseLong(otpValidateRequestDto.getMobile()))
						.mobileVerified(true)
				.build());

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public void resendOtp(LoginRequestDto loginRequestDto) {

		otpService.resendLoginOtp(loginRequestDto);
	}

	@Override
	public void  sendEmailOtp(EmailVerificationRequestDto emailVerificationRequestDto) {

		UserEntity userEntity = getActiveUserByUuid(emailVerificationRequestDto.getUserUuid());

		otpService.sendEmailOtp(userEntity, emailVerificationRequestDto.getEmail());

		log.info("OTP sent for User: " + userEntity.getUuid() + " for Email Verification To Email id: " + emailVerificationRequestDto.getEmail());
	}

	private UserEntity getActiveUserByUuid(String userUuid) {

		//UserEntity userEntity = userDbService.findByUuid(userUuid);
		UserDto users=userV2FeignService.getUserByUuid(userUuid);
		UserEntity userEntity=null;
		if(Objects.nonNull(users)) {
			userEntity = Userv2ToUserAdapter.getUserEntityFromUserv2(users);
		}

		if(Objects.isNull(userEntity)){
			userEntity = userDbService.findByUuidNotMigrated(userUuid,false);
		}

		if (Objects.isNull(userEntity)) {
			throw new UserValidationException("User Not Found with Uuid: " + userUuid);
		}

		if (!userEntity.isStatus()) {
			throw new UserValidationException("User Account is Disabled for Uuid " + userUuid);
		}
		
		log.info("Found User: " + userEntity.getUuid() + " of Type: " + userEntity.getUserType());
		
		return userEntity;
	}

	@Override
	public UserEntity validateEmailVerificationOtpAndUpdateUserDetails(EmailOtpValidateRequestDto emailOtpValidateRequestDto) {

		UserEntity userEntity = getActiveUserByUuid(emailOtpValidateRequestDto.getUserUuid());

		otpService.validateEmailVerificationOtp(emailOtpValidateRequestDto);

		log.info("OTP verification completed for User: " + userEntity.getUuid());

		userEntity.setEmail(emailOtpValidateRequestDto.getEmail());
		
		userEntity.setEmailVerified(true);
		
		if (Objects.nonNull(emailOtpValidateRequestDto.getFirstName()))
			userEntity.getUserProfile().setFirstName(emailOtpValidateRequestDto.getFirstName());

		if (Objects.nonNull(emailOtpValidateRequestDto.getLastName()))
			userEntity.getUserProfile().setLastName(emailOtpValidateRequestDto.getLastName());

		if(UserType.INVITED_GUEST.equals(userEntity.getUserType())){
			bookingDataControllerApi.emailVerifiedUpdate(userEntity.getMobile());
		}

		//call user v2 http service to update
//		userEntity = userDbService.update(userEntity);
		if(userEntity.isMigrated()) {
			userV2FeignService.updateUser(UpdateUserDto.builder()
					.emailVerified(true)
					.firstName(emailOtpValidateRequestDto.getFirstName())
					.lastName(emailOtpValidateRequestDto.getLastName())
					.build());
		}
		else{
			userEntity=userDbService.update(userEntity);
		}
		
		UserProfileDto userProfileDto = UserAdapter.getUserProfileDto(userEntity);
		
		KafkaDTO kafkaDTO = new KafkaDTO();
		kafkaDTO.setData(userProfileDto);

		notificationProducer.publish(kafkaResidentDetailTopic, KafkaDTO.class.getName(), kafkaDTO);


		return userEntity;
	}

	@Override
	public void resendEmailOtp(EmailVerificationRequestDto emailVerificationRequestDto) {
		
		UserEntity userEntity = getActiveUserByUuid(emailVerificationRequestDto.getUserUuid());
		
		otpService.resendEmailVerificationOtp(emailVerificationRequestDto, userEntity);
	}
}
