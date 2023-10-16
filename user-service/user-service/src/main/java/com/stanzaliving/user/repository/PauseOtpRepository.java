/**
 * 
 */
package com.stanzaliving.user.repository;

import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.entity.PauseOtpEntity;

@Repository
public interface PauseOtpRepository extends AbstractJpaRepository<PauseOtpEntity, Long> {

	boolean existsByMobileAndStatus(String mobile,boolean status);
	
	PauseOtpEntity findByMobile(String mobile);	
	
}