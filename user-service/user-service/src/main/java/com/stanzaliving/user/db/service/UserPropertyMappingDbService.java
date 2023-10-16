/**
 * 
 */
package com.stanzaliving.user.db.service;

import java.util.List;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.entity.UserPropertyMappingEntity;

/**
 * @author naveen
 *
 * @date 13-Oct-2019
 */
public interface UserPropertyMappingDbService extends AbstractJpaService<UserPropertyMappingEntity, Long> {

	List<UserPropertyMappingEntity> getAllUserPropertyMappings(String userId);

	List<UserPropertyMappingEntity> getActiveUserPropertyMappings(String userId);

	boolean isUserPropertyMappingExists(String userId, String propertyId);
}