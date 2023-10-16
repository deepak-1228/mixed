package com.stanzaliving.user.controller.internal;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.exception.AuthException;
import com.stanzaliving.core.user.enums.OtpType;
import com.stanzaliving.core.user.request.dto.MobileEmailOtpRequestDto;
import com.stanzaliving.core.user.request.dto.MobileOtpRequestDto;
import com.stanzaliving.core.user.request.dto.MobileOtpValidateRequestDto;
import com.stanzaliving.user.service.OtpService;

import lombok.extern.log4j.Log4j2;

import java.util.Objects;

@Log4j2
@RestController
@RequestMapping("/internal/otp")
public class InternalOtpController {

	@Autowired
	private OtpService otpService;

	@PostMapping("mobile/request")
	public ResponseDto<Void> sendMobileOtp(@RequestBody @Valid MobileOtpRequestDto mobileOtpRequestDto) {

		log.info("Received request to send OTP: {}", mobileOtpRequestDto);

		otpService.sendMobileOtp(mobileOtpRequestDto);

		return ResponseDto.success("OTP sent to mobile");
	}

	@PostMapping("mobile/validate")
	public ResponseDto<Void> validateMobileOtp(@RequestBody @Valid MobileOtpValidateRequestDto mobileOtpValidateRequestDto) {

		log.info("Received request to validate OTP: {}", mobileOtpValidateRequestDto);

		try {

			otpService.validateMobileOtp(
					mobileOtpValidateRequestDto.getMobile(),
					mobileOtpValidateRequestDto.getIsoCode(),
					mobileOtpValidateRequestDto.getOtp(),
					mobileOtpValidateRequestDto.getOtpType());

			return ResponseDto.success("OTP Succefully Validated");

		} catch (AuthException e) {
			log.error(e.getMessage());
			return ResponseDto.failure(e.getMessage());
		}
	}

	@PostMapping("mobile/resend")
	public ResponseDto<Void> resendMobileOtp(@RequestBody @Valid MobileOtpRequestDto mobileOtpRequestDto) {

		log.info("Received request to resend OTP: {}", mobileOtpRequestDto);

		try {

			otpService.resendMobileOtp(mobileOtpRequestDto.getMobile(), mobileOtpRequestDto.getIsoCode(), mobileOtpRequestDto.getOtpType());

			return ResponseDto.success("OTP resent to mobile");

		} catch (AuthException e) {
			log.error(e.getMessage());
			return ResponseDto.failure(e.getMessage());
		}
	}

	@PostMapping("request")
	public ResponseDto<Void> sendMobileAndEmailOtp(@RequestBody @Valid MobileEmailOtpRequestDto mobileEmailOtpRequestDto) {

		log.info("Received request to send OTP: {}", mobileEmailOtpRequestDto);

		otpService.sendOtp(mobileEmailOtpRequestDto);

		return ResponseDto.success("OTP sent to mobile & email");
	}
	
	@GetMapping("getotp")
	public ResponseDto<Integer> getOtp(@RequestParam(value = "mobile", required = true) String mobile,
			@RequestParam(value = "isoCode", required = true, defaultValue = "IN") String isoCode,
			@RequestParam(value = "otpType", defaultValue = "LOGIN") OtpType otpType) {

		return ResponseDto.success("OTP is", otpService.getOtp(mobile, isoCode, otpType));
	}

	@PostMapping("mobile/resent/v2")
	public ResponseDto<Void> resendMobileOtpV2(@RequestBody @Valid MobileOtpRequestDto mobileOtpRequestDto) {

		if (Objects.isNull(mobileOtpRequestDto))
			return ResponseDto.failure("Unable to resend OTP");
		log.info("Received request to resend OTP: {}", mobileOtpRequestDto);
		try {

			otpService.resendMobileOtpV2(mobileOtpRequestDto, mobileOtpRequestDto.getMobile(), mobileOtpRequestDto.getIsoCode(), mobileOtpRequestDto.getOtpType());

			return ResponseDto.success("OTP resent to mobile");

		} catch (AuthException e) {
			log.error("Exception occured {} ",e);
			return ResponseDto.failure(e.getMessage());
		}
	}

}