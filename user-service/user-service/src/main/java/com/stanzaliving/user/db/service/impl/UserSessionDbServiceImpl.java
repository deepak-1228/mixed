/**
 * 
 */
package com.stanzaliving.user.db.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.db.service.UserSessionDbService;
import com.stanzaliving.user.entity.UserSessionEntity;
import com.stanzaliving.user.repository.UserSessionRepository;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Service
public class UserSessionDbServiceImpl extends AbstractJpaServiceImpl<UserSessionEntity, Long, UserSessionRepository> implements UserSessionDbService {

	@Autowired
	private UserSessionRepository userSessionRepository;

	@Override
	protected UserSessionRepository getJpaRepository() {
		return userSessionRepository;
	}

	@Override
	public UserSessionEntity getUserSessionForToken(String token) {
		return userSessionRepository.findByToken(token);
	}

}