/**
 * 
 */
package com.stanzaliving.user.acl.db.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.acl.db.service.ApiDbService;
import com.stanzaliving.user.acl.entity.ApiEntity;
import com.stanzaliving.user.acl.repository.ApiRepository;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
@Service
public class ApiDbServiceImpl extends AbstractJpaServiceImpl<ApiEntity, Long, ApiRepository> implements ApiDbService {

	@Autowired
	private ApiRepository apiRepository;

	@Override
	protected ApiRepository getJpaRepository() {
		return apiRepository;
	}

	@Override
	public boolean existsByActionUrl(String actionUrl) {
		return getJpaRepository().existsByActionUrl(actionUrl);
	}

	@Override
	public boolean existsByApiName(String apiName) {
		return getJpaRepository().existsByApiName(apiName);
	}

}