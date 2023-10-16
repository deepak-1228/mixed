/**
 * 
 */
package com.stanzaliving.user.service;

import java.util.List;
import java.util.Map;

import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserManagerMappingType;
import com.stanzaliving.core.user.request.dto.UserManagerMappingRequestDto;

/**
 * @author raj.kumar
 *
 */
public interface UserManagerMappingService {

	void createUserManagerMapping(UserManagerMappingRequestDto userManagerMappingDto);
	
	List<String> getUserIdsMappedWithManagerId(String managerId);
	
	String findManagerNameForUser(String userId);

	UserProfileDto getManagerProfileForUser(String userId);

	UserProfileDto getUserManagerMappingHierarchy(String userId, UserManagerMappingType mappingType);

	Map<String, UserProfileDto> getManagerProfileForUserIn(List<String> userIds);
	
	List<UserProfileDto> getPeopleReportingToManager( String managerId );

	void deleteManagerMapping(String userUuid);
}
