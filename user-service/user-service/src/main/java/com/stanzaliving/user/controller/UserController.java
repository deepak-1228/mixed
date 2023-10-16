/**
 * 
 */
package com.stanzaliving.user.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import com.stanzaliving.user.entity.UserSessionEntity;
import com.stanzaliving.user.service.SessionService;
import org.apache.commons.collections.CollectionUtils;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.PaginationRequest;
import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.constants.SecurityConstants;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.utils.CSVConverter;
import com.stanzaliving.core.user.acl.dto.AclUserProfileDTO;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.dto.UserManagerAndRoleDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.AddUserAndRoleRequestDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.core.user.request.dto.UpdateDepartmentUserTypeDto;
import com.stanzaliving.core.user.request.dto.UpdateUserRequestDto;
import com.stanzaliving.core.user.request.dto.UserRequestDto;
import com.stanzaliving.core.user.request.dto.UserStatusRequestDto;
import com.stanzaliving.user.acl.service.AclService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

import static com.stanzaliving.core.security.helper.SecurityUtils.extractTokenFromRequest;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@RestController
@RequestMapping("")
public class UserController {

	@Autowired
	private AclService aclService;

	@Autowired
	private UserService userService;

	@Autowired
	private SessionService sessionService;

	@GetMapping("details")
	public ResponseDto<UserProfileDto> getUser(
			@RequestAttribute(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user") String userId) {

		log.info("Fetching User with UserId: " + userId);

		return ResponseDto.success("Found User for User Id", userService.getActiveUserByUserId(userId));
	}

	@GetMapping("profile")
	public ResponseDto<AclUserProfileDTO> getUserProfile(
			@RequestAttribute(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user profile") String userId) {

		log.info("Fetching User Profile with UserId: " + userId);

		return ResponseDto.success("Found User Profile for User Id", UserAdapter.getAclUserProfileDTO(userService.getUserProfile(userId), aclService.getUserDeptLevelRoleNameUrlExpandedDtoFe(userId)));
	}

	@GetMapping("profile/current")
	public ResponseDto<UserProfileDto> getCurrentUserProfile(
			@RequestAttribute(name = SecurityConstants.USER_ID) @NotBlank(message = "User Id is mandatory to get user profile") String userId) {

		log.info("Fetching current User Profile with UserId: " + userId);

		return ResponseDto.success("Found Current User Profile for User Id", userService.getUserProfile(userId));
	}
	
	@PostMapping("update")
	public ResponseDto<UserDto> updateUser(@RequestBody @Valid UpdateUserRequestDto updateUserRequestDto) {

		UserDto userDto = userService.updateUser(updateUserRequestDto);

		log.info("Update user with id: " + userDto.getUuid());

		return ResponseDto.success("User Updated", userDto);
	}

	@PostMapping("add")
	public ResponseDto<UserDto> addUser(@RequestBody @Valid AddUserRequestDto addUserRequestDto) {

		UserDto userDto = userService.addUser(addUserRequestDto);

		log.info("Added new user with id: " + userDto.getUuid());

		return ResponseDto.success("New User Created", userDto);
	}

	@PostMapping("bulk/add")
	public ResponseDto<List<UserDto>> addBulkUserAndRole(@RequestBody List<AddUserAndRoleRequestDto> addUserAndRoleRequestDtoList) {

		List<UserDto> userDtoList = userService.addBulkUserAndRole(addUserAndRoleRequestDtoList);

		return ResponseDto.success("Bulk New Users creation with role assignment are successful", userDtoList);
	}

	@GetMapping("search/{pageNo}/{limit}")
	public ResponseDto<PageResponse<UserProfileDto>> searchUsers(
			@PathVariable(name = "pageNo") @Min(value = 1, message = "Page No must be greater than 0") int pageNo,
			@PathVariable(name = "limit") @Min(value = 1, message = "Limit must be greater than 0") int limit,
			@RequestParam(name = "userIds", required = false) List<String> userIds,
			@RequestParam(name = "mobile", required = false) String mobile,
			@RequestParam(name = "isoCode", required = false) String isoCode,
			@RequestParam(name = "email", required = false) String email,
			@RequestParam(name = "userType", required = false) UserType userType,
			@RequestParam(name = "status", required = false) Boolean status,
			@RequestParam(name = "department", required = false) Department department,
			@RequestParam(name = "name", required = false) String name) {

		log.info("Received User Search Request With Parameters [Page: " + pageNo + ", Limit: " + limit + ", Mobile: " + mobile + ", ISO: " + isoCode + ", Email: " + email + ", UserType: " + userType
				+ ", Status: " + status + ", UserIds: {" + CSVConverter.getCSVString(userIds) + "} ]");

		PaginationRequest paginationRequest = PaginationRequest.builder().pageNo(pageNo).limit(limit).build();
		UserFilterDto userFilterDto = UserFilterDto.builder()
				.userIds(userIds)
				.mobile(mobile)
				.isoCode(isoCode)
				.email(email)
				.userType(userType)
				.status(status)
				.department(department)
				.name(name)
				.pageRequest(paginationRequest)
				.build();
		PageResponse<UserProfileDto> userDtos = userService.searchUser(userFilterDto);

		return ResponseDto.success("Found " + userDtos.getRecords() + " Users for Search Criteria", userDtos);
	}

	@GetMapping("type/list")
	public ResponseDto<List<EnumListing<UserType>>> getUserType() {

		log.info("Received UserType listing request");
		return ResponseDto.success("Found UserType List", UserAdapter.getUserTypeEnumAsListing());
	}

	@PostMapping("update/userStatus")
	public ResponseDto<Boolean> updateUserStatus(@RequestBody UserStatusRequestDto requestDto,HttpServletRequest request) {

		log.info("Received request to deactivate user : {}",requestDto);
		String updatedStatus = requestDto.getStatus() ? "activated" : "deactivated";
		try {
			String token  = extractTokenFromRequest(request);
			if (StringUtils.isNotBlank(token)){
				UserSessionEntity userSessionEntity =sessionService.getUserSessionByToken(token);
				log.info("User Status update request has been initiated by user Id {}  ", userSessionEntity.getUserId());
			}
		}catch(Exception e ){
			log.error("exception occured while user update status :",e);
		}


		return ResponseDto.success("Successfully " + updatedStatus + " user.", userService.updateUserStatus(requestDto.getUserId(), requestDto.getStatus()));
	}

	@GetMapping("details/manager/role")
	public ResponseDto<UserManagerAndRoleDto> getUserWithManagerAndRole(@RequestParam("userId") String userUuid) {

		log.info("Request received for getting user details along with manager and role details");
		UserManagerAndRoleDto userManagerAndRoleDto = userService.getUserWithManagerAndRole(userUuid);

		log.info("Successfully fetched user details along with manager and role details.");
		return ResponseDto.success("Found user Details with manager and role details.", userManagerAndRoleDto);
	}
	
	@PostMapping("update/usertype/department")
	public ResponseDto<UserDto> updateUserTypeAndDepartment(@RequestBody @Valid UpdateDepartmentUserTypeDto updateDepartmentUserTypeDto) {

		boolean response = userService.updateUserTypeAndDepartment(updateDepartmentUserTypeDto);

		return response ? ResponseDto.success("User type and department modified successfully") : ResponseDto.failure("User type and department not modified");
	}

	@PostMapping("search/user")
	public ResponseDto<PageResponse<UserProfileDto>> searchUsersDetail(@RequestBody UserRequestDto userRequestDto) {

		log.info("Post Request recived search user detail UserRequestDto {} ", userRequestDto);

		PaginationRequest paginationRequest = PaginationRequest.builder().pageNo(userRequestDto.getPageNo()).limit(userRequestDto.getLimit()).build();
		UserFilterDto userFilterDto = UserFilterDto.builder()
				.userIds(userRequestDto.getUserIds())
				.mobile(userRequestDto.getMobile())
				.isoCode(userRequestDto.getIsoCode())
				.email(userRequestDto.getEmail())
				.userType(userRequestDto.getUserType())
				.status(userRequestDto.getStatus())
				.department(userRequestDto.getDepartment())
				.name(userRequestDto.getName())
				.pageRequest(paginationRequest)
				.build();
		PageResponse<UserProfileDto> userDtos = userService.searchUser(userFilterDto);

		return ResponseDto.success("Found " + userDtos.getRecords() + " Users for Search Criteria", userDtos);
	}
	
	@PostMapping("search/user/list")
	public ResponseDto<Set<UserProfileDto>> searchUsersDetailAll(@RequestBody UserRequestDto userRequestDto) {

		log.info("Post Request received to search user details for user list UserRequestDto {} ", userRequestDto);

		Set<UserProfileDto> userProfileDtos = new HashSet<UserProfileDto>();

		UserFilterDto userFilterDto = UserFilterDto.builder()
				.userIds(userRequestDto.getUserIds())
				.mobile(userRequestDto.getMobile())
				.isoCode(userRequestDto.getIsoCode())
				.email(userRequestDto.getEmail())
				.userType(userRequestDto.getUserType())
				.status(userRequestDto.getStatus())
				.department(userRequestDto.getDepartment())
				.name(userRequestDto.getName())
				.build();

		userProfileDtos = userService.searchUserList(userFilterDto);

		return ResponseDto.success("Found " + userProfileDtos.size() + " Users for Search Criteria", userProfileDtos);
	}



}