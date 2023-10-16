package com.stanzaliving.user.db.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.db.service.SignUpDbService;
import com.stanzaliving.user.entity.SignupEntity;
import com.stanzaliving.user.repository.SignUpRepository;
@Service
public class SignUpDbServiceImpl extends AbstractJpaServiceImpl<SignupEntity, Long, SignUpRepository>
		implements SignUpDbService {

	@Autowired
	private SignUpRepository signUpRepository;

	@Override
	protected SignUpRepository getJpaRepository() {
		return signUpRepository;
	}

}
