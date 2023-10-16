/**
 * 
 */
package com.stanzaliving.user.service;

import java.util.List;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.user.dto.UserPropertyMappingDto;
import com.stanzaliving.core.user.request.dto.UserPropertyMappingRequestDto;

/**
 * @author naveen
 *
 * @date 13-Oct-2019
 */
public interface UserPropertyMappingService {

	void createUserPropertyMapping(List<UserPropertyMappingRequestDto> mappingRequestDtos);

	List<UserPropertyMappingDto> getUserPropertyMappings(String userId);

	PageResponse<UserPropertyMappingDto> searchUserPropertyMappings(String userId, String propertyId, int pageNo, int limit);

	void deleteMapping(String mappingId);

}