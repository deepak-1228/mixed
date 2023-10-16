package com.stanzaliving.user.db.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.db.service.PauseOtpDbService;
import com.stanzaliving.user.entity.PauseOtpEntity;
import com.stanzaliving.user.repository.PauseOtpRepository;

@Service
public class PauseOtpDbServiceImpl extends AbstractJpaServiceImpl<PauseOtpEntity, Long, PauseOtpRepository> implements PauseOtpDbService {

	@Autowired
	private PauseOtpRepository pauseOtpRepository;

	@Override
	public boolean checkIfMobileExist(String mobile) {
		return getJpaRepository().existsByMobileAndStatus(mobile, true);
	}

	@Override
	public PauseOtpEntity findByMobileNumber(String mobile) {
		return getJpaRepository().findByMobile(mobile);
	}

	@Override
	protected PauseOtpRepository getJpaRepository() {
		return pauseOtpRepository;
	}

}