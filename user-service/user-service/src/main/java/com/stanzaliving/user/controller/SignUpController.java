package com.stanzaliving.user.controller;

import java.util.Objects;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.base.utils.SecureCookieUtil;
import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.user.acl.service.AclService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.entity.UserSessionEntity;
import com.stanzaliving.user.service.SessionService;
import com.stanzaliving.user.service.SignUpService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("signup")
public class SignUpController {

	@Autowired
	private SignUpService signUpService;
	
	@Autowired
	private SessionService sessionService;
	@Autowired
	AclService aclService;
	
	@PostMapping("signUpUser")
	public ResponseDto<String> signUpUser(@RequestBody @Valid AddUserRequestDto addUserRequestDto) {

		String userUuid = signUpService.signUpUser(addUserRequestDto);

		log.info("Added new user with id: " + userUuid);
		return ResponseDto.success("New User Created", userUuid);

	}

	@GetMapping("validateOtp")
	public ResponseDto<UserDto> validateSignUpOtp(@RequestParam(name = "Uuid", required = true) String uuid,
			@RequestParam(name = "otp", required = true) String otp,HttpServletRequest request, HttpServletResponse response) {

		log.info("OTP Validated for Create User: " + otp);

		UserProfileDto userProfileDto= signUpService.validateSignUpOtp(uuid, otp);
		
		log.info("OTP Successfully Validated for User: " + userProfileDto.getUuid() + ". Creating User Session now");

		String token = StanzaUtils.generateUniqueId();

		UserSessionEntity userSessionEntity = sessionService.createUserSession(userProfileDto, token);

		if (Objects.nonNull(userSessionEntity)) {
			addTokenToResponse(request, response, token);
			return ResponseDto.success("User Login Successfull", UserAdapter.getAclUserDto(userProfileDto, null));
		}
		
		return ResponseDto.failure("Failed to create user session");

	}

	private void addTokenToResponse(HttpServletRequest request, HttpServletResponse response, String token) {

		if (StringUtils.isNotBlank(token)) {

			String frontEnv = request.getHeader(SecurityConstants.FRONT_ENVIRONMENT);
			boolean isLocalFrontEnd = StringUtils.isNotBlank(frontEnv) && SecurityConstants.FRONT_ENVIRONMENT_LOCAL.equals(frontEnv);

			String appEnv = request.getHeader(SecurityConstants.APP_ENVIRONMENT);
			boolean isApp = StringUtils.isNotBlank(appEnv) && SecurityConstants.APP_ENVIRONMENT_TRUE.equals(appEnv);

			response.addCookie(SecureCookieUtil.create(SecurityConstants.TOKEN_HEADER_NAME, token, Optional.of(isLocalFrontEnd), Optional.of(isApp)));
		}
	}
}
