package com.stanzaliving.user.controller.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.user.service.PauseOtpService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/internal/pauseotp")
public class PauseOtpController {

	@Autowired
	private PauseOtpService blacklistUserService;

	@PostMapping("add/{mobile}")
	public ResponseDto<Boolean> addUser(@PathVariable("mobile") String mobile) {

		log.info("Received request to pause OTP for mobile: {}", mobile);

		return ResponseDto.success("OTP Paused for " + mobile, blacklistUserService.pauseOtp(mobile));
	}

	@PostMapping("delete/{mobile}")
	public ResponseDto<Boolean> deleteUser(@PathVariable("mobile") String mobile) {

		log.info("Received request to resume OTP for mobile: {}", mobile);

		return ResponseDto.success("OTP resumned for " + mobile, blacklistUserService.resumeOtp(mobile));
	}

	@GetMapping("check/{mobile}")
	public ResponseDto<Boolean> checkUser(@PathVariable("mobile") String mobile) {

		log.info("Received request to get pause OTP status for mobile: {}", mobile);

		boolean checkIfOtpPaused = blacklistUserService.checkIfNeedToStop(mobile);

		return ResponseDto.success((checkIfOtpPaused ? "OTP currently paused" : "OTP currently going") + " for " + mobile, checkIfOtpPaused);
	}

}