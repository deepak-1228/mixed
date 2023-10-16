package com.stanzaliving.user.acl.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.transformation.client.cache.TransformationCache;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.entity.ApiEntity;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserDepartmentLevelRoleAdapter {

	public static UserDepartmentLevelEntity getEntityFromRequest(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
		return UserDepartmentLevelEntity.builder()
				.userUuid(addUserDeptLevelRequestDto.getUserUuid())
				.department(addUserDeptLevelRequestDto.getDepartment())
				.accessLevel(addUserDeptLevelRequestDto.getAccessLevel())
				.csvAccessLevelEntityUuid(String.join(",", addUserDeptLevelRequestDto.getAccessLevelEntityListUuid()))
				.build();
	}

	public static UserDeptLevelRoleDto getUserDeptLevelRoleDto(UserDepartmentLevelEntity userDepartmentLevelEntity, List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList) {
		return UserDeptLevelRoleDto.builder()
				.userDeptLevelUuid(userDepartmentLevelEntity.getUuid())
				.userUuid(userDepartmentLevelEntity.getUserUuid())
				.department(userDepartmentLevelEntity.getDepartment())
				.accessLevel(userDepartmentLevelEntity.getAccessLevel())
				.accessLevelEntityListUuid(StanzaUtils.getSplittedListOnComma(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid()))
				.rolesUuid(userDepartmentLevelRoleEntityList.stream().map(entity -> entity.getRoleUuid()).collect(Collectors.toList()))
				.build();
	}

	public static UserDeptLevelRoleNameUrlExpandedDto getUserDeptLevelRoleNameUrlExpandedDto(
			UserDepartmentLevelEntity userDepartmentLevelEntity, List<RoleEntity> roleEntityList, List<ApiEntity> apiEntityList, TransformationCache transformationCache) {

		Map<String, String> roleNameUuidMap = new HashMap<>();
		roleEntityList.forEach(role -> roleNameUuidMap.put(role.getRoleName(), role.getUuid()));
		
		List<String> roleNameList = new ArrayList<>(roleNameUuidMap.keySet());
		List<String> actionUrlList = apiEntityList.stream().map(entity -> entity.getActionUrl()).collect(Collectors.toList());
		List<String> accessLevelEntityListUuid = StanzaUtils.getSplittedListOnComma(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid());
		
		TreeMap<String, String> accessLevelEntityNameUuidMap = new TreeMap<>();
		accessLevelEntityListUuid.forEach(uuid -> accessLevelEntityNameUuidMap.put(transformationCache.getAccessLevelNameByUuid(uuid, userDepartmentLevelEntity.getAccessLevel().toString()), uuid));
		Collections.sort(roleNameList);
		Collections.sort(actionUrlList);

		return UserDeptLevelRoleNameUrlExpandedDto.builder()
				.userUuid(userDepartmentLevelEntity.getUserUuid())
				.department(userDepartmentLevelEntity.getDepartment())
				.accessLevel(userDepartmentLevelEntity.getAccessLevel())
				.accessLevelEntityListUuid(accessLevelEntityListUuid)
				.accessLevelEntityNameUuidMap(accessLevelEntityNameUuidMap)
				.rolesList(roleNameList)
				.roleNameUuidMap(roleNameUuidMap)
				.urlList(actionUrlList)
				.build();
	}
}