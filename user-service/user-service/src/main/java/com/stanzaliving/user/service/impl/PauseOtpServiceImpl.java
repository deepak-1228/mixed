package com.stanzaliving.user.service.impl;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.user.db.service.PauseOtpDbService;
import com.stanzaliving.user.entity.PauseOtpEntity;
import com.stanzaliving.user.service.PauseOtpService;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class PauseOtpServiceImpl implements PauseOtpService {

	@Autowired
	private PauseOtpDbService pauseOtpDbService;

	@Override
	public boolean checkIfNeedToStop(String mobile) {

		log.info("Got request to check blacklist {}", mobile);

		return pauseOtpDbService.checkIfMobileExist(mobile);
	}

	@Override
	public boolean pauseOtp(String mobile) {

		log.info("Got request to add to blacklist {}", mobile);

		PauseOtpEntity pauseOtpEntity = pauseOtpDbService.findByMobileNumber(mobile);

		if (Objects.nonNull(pauseOtpEntity)) {
			pauseOtpEntity.setStatus(true);
		} else {
			pauseOtpEntity = PauseOtpEntity.builder().mobile(mobile).status(true).build();
		}

		pauseOtpEntity = pauseOtpDbService.save(pauseOtpEntity);

		return Objects.nonNull(pauseOtpEntity);
	}

	@Override
	public boolean resumeOtp(String mobile) {

		log.info("Got request to remove from blacklist {}", mobile);

		PauseOtpEntity pauseOtpEntity = pauseOtpDbService.findByMobileNumber(mobile);

		if (Objects.isNull(pauseOtpEntity) || !pauseOtpEntity.isStatus()) {
			throw new ApiValidationException("OTP not paused for " + mobile);
		}

		pauseOtpEntity.setStatus(false);

		pauseOtpEntity = pauseOtpDbService.save(pauseOtpEntity);

		return Objects.nonNull(pauseOtpEntity);
	}

}