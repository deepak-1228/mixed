/**
 * 
 */
package com.stanzaliving.user.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.entity.UserSessionEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserSessionDbService extends AbstractJpaService<UserSessionEntity, Long> {

	UserSessionEntity getUserSessionForToken(String token);

}