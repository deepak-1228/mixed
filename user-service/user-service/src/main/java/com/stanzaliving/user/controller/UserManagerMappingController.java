/**
 * 
 */
package com.stanzaliving.user.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.stanzaliving.core.user.request.dto.UserUuidRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.user.dto.UserManagerProfileRequestDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserManagerMappingType;
import com.stanzaliving.core.user.request.dto.UserManagerMappingRequestDto;
import com.stanzaliving.user.service.UserManagerMappingService;

import lombok.extern.log4j.Log4j2;

/**
 * @author raj.kumar
 *
 */
@Log4j2
@RestController
@RequestMapping("usermanagermapping")
public class UserManagerMappingController {

	@Autowired
	private UserManagerMappingService userManagerMappingService;

	@PostMapping("create")
	public ResponseDto<Void> createMapping(@RequestBody @Valid UserManagerMappingRequestDto userManagerMappingRequestDto) {
		log.info(" Create userId and managerId mapping for : " + userManagerMappingRequestDto.getUserId() + " " + userManagerMappingRequestDto.getManagerId());

		userManagerMappingService.createUserManagerMapping(userManagerMappingRequestDto);

		return ResponseDto.success("Mapping Created Successfully ");
	}

	@GetMapping("{managerId}")
	public ResponseDto<List<String>> getUserIdsByManagerId(@PathVariable(name = "managerId") @NotBlank(message = "Manager Id is Mandatory") String managerId) {
		log.info(" Get mapped userIds by managerId " + managerId);

		List<String> userIds = userManagerMappingService.getUserIdsMappedWithManagerId(managerId);
		return ResponseDto.success("Mapping Created Successfully", userIds);
	}

	@GetMapping("/managername/{userId}")
	public ResponseDto<String> getManagerNameByUserID(@PathVariable(name = "userId") @NotBlank(message = "User Id is Mandatory") String userId) {
		log.info(" Get manager name by " + userId);

		String managerName = userManagerMappingService.findManagerNameForUser(userId);
		return ResponseDto.success("Manager Name Found!", managerName);
	}

	@GetMapping("/managerprofile/{userId}")
	public ResponseDto<UserProfileDto> getManagerProfieByUserID(@PathVariable(name = "userId") @NotBlank(message = "User Id is Mandatory") String userId) {
		log.info(" Get manager profile by " + userId);

		UserProfileDto managerProfile = userManagerMappingService.getManagerProfileForUser(userId);

		if (managerProfile == null)
			return ResponseDto.failure("Manager Profile Not Found");

		return ResponseDto.success("Manager Profile Found!", managerProfile);
	}

	@PostMapping("/managerprofiles")
	public ResponseDto<Map<String, UserProfileDto>> getManagerProfileByUserID(@RequestBody UserManagerProfileRequestDto profileRequestDto) {
		log.info(" Get manager profiles by " + profileRequestDto.getUserUuids());

		Map<String, UserProfileDto> userManagerMap = userManagerMappingService.getManagerProfileForUserIn(profileRequestDto.getUserUuids());

		if (userManagerMap == null)
			return ResponseDto.failure("Manager Profiles Not Found");

		return ResponseDto.success("Manager Profile Found!", userManagerMap);
	}

	@GetMapping("/userprofiles/{managerId}")
	public ResponseDto<List<UserProfileDto>> getUserProfilesByManagerID(@PathVariable("managerId") String managerId) {
		log.info(" Get user profiles by " + managerId);

		List<UserProfileDto> userManagerList = userManagerMappingService.getPeopleReportingToManager(managerId);

		if (CollectionUtils.isEmpty(userManagerList))
			return ResponseDto.failure("User Profiles Not Found");

		return ResponseDto.success("User Profile Found!", userManagerList);
	}

	@GetMapping("/managerprofile/{userId}/{managertype}")
	public ResponseDto<UserProfileDto> getManagerProfieByUserID(@PathVariable(name = "userId") @NotBlank(message = "User Id is Mandatory") String userId,
			@PathVariable(name = "managertype") @NotBlank(message = "Manager Type cannot be blank") UserManagerMappingType userManagerMappingType) {

		UserProfileDto managerProfile = userManagerMappingService.getUserManagerMappingHierarchy(userId, userManagerMappingType);

		if (managerProfile == null)
			return ResponseDto.failure("Manager Mapping Not Found");

		return ResponseDto.success("Manager Mapping Found!", managerProfile);
	}

	@PostMapping("remove/")
	public ResponseDto<Void> removeManager(@RequestBody UserUuidRequestDto userUuidRequestDto) {
		userManagerMappingService.deleteManagerMapping(userUuidRequestDto.getUserUuid());
		return ResponseDto.success("Manager removed successfully.");
	}
}