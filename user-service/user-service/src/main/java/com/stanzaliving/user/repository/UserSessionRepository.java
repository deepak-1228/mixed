/**
 * 
 */
package com.stanzaliving.user.repository;

import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.entity.UserSessionEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Repository
public interface UserSessionRepository extends AbstractJpaRepository<UserSessionEntity, Long> {

	UserSessionEntity findByToken(String token);
}