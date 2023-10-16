package com.stanzaliving.user.acl.service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.acl.dto.AddUserAndRoleDto;
import com.stanzaliving.core.user.acl.dto.CityMicromarketDropdownResponseDto;
import com.stanzaliving.core.user.acl.dto.MicromarketAndResidencesDropdownRequestDto;
import com.stanzaliving.core.user.acl.dto.MicromarketAndResidencesDropdownResponseDto;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.UpdateAccessModuleAccessLevelRequestDto;
import com.stanzaliving.core.user.acl.dto.UserAccessLevelIdsByRoleNameWithFiltersDto;
import com.stanzaliving.core.user.acl.dto.UserAccessModuleDto;
import com.stanzaliving.core.user.acl.dto.UserDepartmentLevelAccessModulesDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleListDto;
import com.stanzaliving.core.user.acl.dto.UsersByAccessModulesAndCitiesRequestDto;
import com.stanzaliving.core.user.acl.dto.UsersByAccessModulesAndCitiesResponseDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleByEmailRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.response.UserContactDetailsResponseDto;
import com.stanzaliving.transformations.pojo.CityMetadataDto;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AclUserService {
	void addRole(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto);

	void revokeAllRolesOfDepartment(String userUuid, Department department);

	List<UserDeptLevelRoleDto> getActiveUserDeptLevelRole(String userUuid);

	List<UserDeptLevelRoleDto> getUserDeptLevelRole(String userUuid);

	List<RoleDto> getUserRoles(String userUuid);

	void revokeAllRolesOfDepartmentOfLevel(String userUuid, Department department, AccessLevel accessLevel);

	void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto);

	void revokeRolesForDepartmentOfLevel(UserDeptLevelRoleListDto userDeptLevelRoleListDto);

    Map<String, List<String>> getUsersForRoles(Department department,String roleName,List<String> accessLevelEntity);

	Map<String, List<String>> getActiveUsersForRoles(Department department,String roleName,List<String> accessLevelEntity);

	Map<String, List<String>> getActiveUsersForRole(String roleName,Set<String> accessLevelEntity);

	List<UserContactDetailsResponseDto> getUserContactDetails(Department department, String roleName, List<String> accessLevelEntity);

	void bulkAddRole(AddUserDeptLevelRoleByEmailRequestDto addUserDeptLevelRoleByEmailRequestDto);

	Set<String> getAccessLevelIds(Department department, String roleName);

	Map<String, List<String>> getUsersForRolesWithFilters(UserAccessLevelIdsByRoleNameWithFiltersDto userAccessLevelIdsByRoleNameWithFiltersDto);

	List<UserAccessModuleDto> getUserAccessModulesByUserUuid(String userUuid);

	List<CityMetadataDto> getCitiesByUserAcessAndDepartment(String userUuid, Department department);

	List<UsersByAccessModulesAndCitiesResponseDto> getUsersByAccessModulesAndCitites(UsersByAccessModulesAndCitiesRequestDto usersByAccessModulesAndCitiesRequestDto,
																					 String userUuid);

	List<UserDepartmentLevelAccessModulesDto> getUserDepartmentLevelAccessModules(String userUuid, Department department);

	void updateUserAccessModuleAccessLevel(UpdateAccessModuleAccessLevelRequestDto updateAccessModuleAccessLevelRequestDto);

	AddUserAndRoleDto addUserAndRole (AddUserAndRoleDto addUserAndRoleDto);

	List<MicromarketAndResidencesDropdownResponseDto> getMicromarketAndResidenceDropdown(MicromarketAndResidencesDropdownRequestDto requestDto);

	List<CityMicromarketDropdownResponseDto> getCityMicromarketDropdown(MicromarketAndResidencesDropdownRequestDto requestDto);

	void addRoleV2(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto);
}
