/**
 * 
 */
package com.stanzaliving.user.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.user.dto.UserPropertyMappingDto;
import com.stanzaliving.core.user.request.dto.UserPropertyMappingRequestDto;
import com.stanzaliving.user.service.UserPropertyMappingService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 13-Oct-2019
 */
@Log4j2
@RestController
@RequestMapping("mapping")
public class UserPropertyMappingController {

	@Autowired
	private UserPropertyMappingService userPropertyMappingService;

	@PostMapping("add")
	public ResponseDto<Void> getUser(@RequestBody List<@Valid UserPropertyMappingRequestDto> mappingRequestDtos) {

		log.info("Received request to create " + mappingRequestDtos.size() + " User-Property mappings");

		userPropertyMappingService.createUserPropertyMapping(mappingRequestDtos);

		return ResponseDto.success("Created User-Property Mappings");
	}

	@DeleteMapping("{mappingId}")
	public ResponseDto<Void> deleteMapping(
			@PathVariable(name = "mappingId") @NotBlank(message = "Mapping Id is mandatory to delete") String mappingId) {

		log.info("Delete mapping requested with id: " + mappingId);

		userPropertyMappingService.deleteMapping(mappingId);

		return ResponseDto.success("Deleted User-Property Mapping");
	}

	@GetMapping("user/{userId}")
	public ResponseDto<List<UserPropertyMappingDto>> getUserMappings(
			@PathVariable(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user mappings") String userId) {

		log.info("Fetching Mapped Properties with UserId: " + userId);

		List<UserPropertyMappingDto> userPropertyMappings = userPropertyMappingService.getUserPropertyMappings(userId);

		return ResponseDto.success("Found " + userPropertyMappings.size() + " Properties mapped to User", userPropertyMappings);
	}

	@GetMapping("search/{pageNo}/{limit}")
	public ResponseDto<PageResponse<UserPropertyMappingDto>> searchUsers(
			@PathVariable(name = "pageNo") @Min(value = 1, message = "Page No must be greater than 0") int pageNo,
			@PathVariable(name = "limit") @Min(value = 1, message = "Limit must be greater than 0") int limit,
			@RequestParam(name = "userId", required = false) String userId,
			@RequestParam(name = "properyId", required = false) String propertyId) {

		log.info("Received User Search Request With Parameters [Page: " + pageNo + ", Limit: " + limit + ", UserId: " + userId + ", PropertyId: " + propertyId + "]");

		PageResponse<UserPropertyMappingDto> mappingDtos = userPropertyMappingService.searchUserPropertyMappings(userId, propertyId, pageNo, limit);

		return ResponseDto.success("Found " + mappingDtos.getRecords() + " Users-Property mappings for Search Criteria", mappingDtos);
	}

}