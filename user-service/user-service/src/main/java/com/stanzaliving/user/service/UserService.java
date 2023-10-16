/**
 * 
 */
package com.stanzaliving.user.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.dto.AccessLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.dto.UserManagerAndRoleDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.dto.UserRoleCacheDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.ActiveUserRequestDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.core.user.request.dto.UpdateDepartmentUserTypeDto;
import com.stanzaliving.core.user.request.dto.UpdateUserRequestDto;
import com.stanzaliving.core.user.request.dto.AddUserAndRoleRequestDto;
import com.stanzaliving.user.entity.UserEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserService {

	UserProfileDto getActiveUserByUserId(String userId);

	UserProfileDto getUserByUserId(String userId);

	UserDto getActiveUserByUuid(String userUuid);

	void assertActiveUserByUserUuid(String userId);

	UserDto addUser(AddUserRequestDto addUserRequestDto);

	List<UserDto> addBulkUserAndRole(List<AddUserAndRoleRequestDto> addUserRequestDtoList);
	List<UserProfileDto> getUserProfileList(List<String> userUuidList);

	UserProfileDto getUserProfile(String userId);

	PageResponse<UserProfileDto> searchUser(UserFilterDto userFilterDto);

	Map<String, UserProfileDto> getUserProfileIn(Map<String, String> userManagerUuidMap);

	boolean updateUserStatus(String userId, Boolean status);

	UserManagerAndRoleDto getUserWithManagerAndRole(String userUuid);

	List<UserProfileDto> getAllUsers();
	
	List<UserProfileDto> getAllActiveUsersByUuidIn(ActiveUserRequestDto activeUserRequestDto);

	List<UserEntity> getUserByEmail(String email);

	boolean updateUserTypeAndDepartment(UpdateDepartmentUserTypeDto updateDepartmentUserTypeDto);

	UserDto updateUser(UpdateUserRequestDto updateUserRequestDto);

	UserDto updateUserMobile(UpdateUserRequestDto updateUserRequestDto);

	boolean updateUserStatus(String mobileNo, UserType userType, Boolean enabled);

	UserDto updateUserType(String mobileNo, String isoCode, UserType userType);

	UserDto getUserForAccessLevelAndRole(AccessLevelRoleRequestDto cityRolesRequestDto);

	List<UserDto> getUsersForRole(AccessLevelRoleRequestDto cityRolesRequestDto);

	boolean createRoleBaseUser(UserType userType);

	Map<String, UserProfileDto> getUserProfileDto(Set<String> mobileNos);
	
	List<String> getUserProfileDto(List<String> mobileNos);

	boolean createRoleBaseUser(List<String> mobiles);

	UserProfileDto getUserDetails(String mobileNo);
    
	Map<String, UserProfileDto> getUserProfileForUserIn(List<String> userUuids);
    
	List<UserRoleCacheDto> getCacheableForRoles(List<String> roleNames);

	UserProfileDto getUserProfileDtoByEmail(String email);

	void saveUserDeptLevelForNewDept(Department newDept, Department refDept);

	void rollBack(Department newDepartment);

	UserDto addUserV3(AddUserRequestDto addUserRequestDto);
	
	List<String> getUserProfileDtoWhoseBirthdayIsToday();

	Set<UserProfileDto> searchUserList(UserFilterDto userFilterDto);

	UserProfileDto getActiveUsersByEmail(String email);
}