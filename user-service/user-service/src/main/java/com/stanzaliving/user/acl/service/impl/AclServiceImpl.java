/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import com.stanzaliving.core.transformation.client.cache.TransformationCache;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.feignclient.UserV2FeignService;
import com.stanzaliving.user.feignclient.Userv2HttpService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelRoleAdapter;
import com.stanzaliving.user.acl.db.service.ApiDbService;
import com.stanzaliving.user.acl.db.service.RoleAccessDbService;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.ApiEntity;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.AclService;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen.kumar
 *
 * @date 23-Oct-2019
 *
 **/
@Log4j2
@Service
public class AclServiceImpl implements AclService {

	@Autowired
	private UserService userService;

	@Autowired
	private ApiDbService apiDbService;

	@Autowired
	private RoleDbService roleDbService;

	@Autowired
	private RoleAccessDbService roleAccessDbService;

	@Autowired
	private UserDepartmentLevelDbService userDepartmentLevelDbService;

	@Autowired
	private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

	@Autowired
	private TransformationCache transformationCache;

	@Autowired
	private UserV2FeignService userV2FeignService;

	@Override
	public boolean isAccessible(String userId, String url) {

		Set<String> accessibleUrlList = getAccessibleUrlList(userId);
		return accessibleUrlList.contains(url);
	}

	@Override
	public List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDtoFe(String userUuid) {

		List<UserDeptLevelRoleNameUrlExpandedDto> userDeptLevelRoleNameUrlExpandedDtoList = getUserDeptLevelRoleNameUrlExpandedDto(userUuid);

		for (UserDeptLevelRoleNameUrlExpandedDto userDeptLevelRoleNameUrlExpandedDto : userDeptLevelRoleNameUrlExpandedDtoList) {
			userDeptLevelRoleNameUrlExpandedDto.setUrlList(new ArrayList<>());
		}

		return userDeptLevelRoleNameUrlExpandedDtoList;
	}

	@Override
	public List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDtoBe(String userUuid) {

		log.info("Fetching User Department role name for user: {}", userUuid);

		return getUserDeptLevelRoleNameUrlExpandedDto(userUuid);
	}

	@Override
	public Set<String> getAccessibleUrlList(String userUuid) {
		List<UserDeptLevelRoleNameUrlExpandedDto> userDeptLevelRoleNameUrlExpandedDtoList = getUserDeptLevelRoleNameUrlExpandedDto(userUuid);
		Set<String> accessibleUrlList = new HashSet<>();
		for (UserDeptLevelRoleNameUrlExpandedDto userDeptLevelRoleNameUrlExpandedDto : userDeptLevelRoleNameUrlExpandedDtoList) {
			accessibleUrlList.addAll(userDeptLevelRoleNameUrlExpandedDto.getUrlList());
		}
		return accessibleUrlList;
	}

	@Override
	public List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDtoFeFromEmail(String email) {
		List<UserEntity> userEntityList = userService.getUserByEmail(email.trim());
		if (CollectionUtils.isEmpty(userEntityList)) {
			return Collections.emptyList();
		}
		List<UserDeptLevelRoleNameUrlExpandedDto> userDeptLevelRoleNameUrlExpandedDtoList = new ArrayList<>();
		for (UserEntity userEntity : userEntityList) {
			try {
				userDeptLevelRoleNameUrlExpandedDtoList.addAll(getUserDeptLevelRoleNameUrlExpandedDtoFe(userEntity.getUuid()));
			} catch (Exception e) {
				log.error("Exception while getting user dept level role name", e);
			}
		}
		return userDeptLevelRoleNameUrlExpandedDtoList;
	}

	private List<UserDeptLevelRoleNameUrlExpandedDto> getUserDeptLevelRoleNameUrlExpandedDto(String userUuid) {

		userService.assertActiveUserByUserUuid(userUuid);

		List<UserDeptLevelRoleNameUrlExpandedDto> userDeptLevelRoleNameUrlExpandedDtoList = new ArrayList<>();

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList=null;

		List<UserDeptLevelRoleNameUrlExpandedDto> userV2DepartmentLevelEntityList=userV2FeignService.getUserDeptRoleNameList(userUuid);

		userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);
		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {

			Pair<List<String>, List<String>> roleUuidApiUuidList = getRoleUuidApiUuidListOfUser(userDepartmentLevelEntity);

			List<RoleEntity> roleEntityList = roleDbService.findByUuidInAndStatusAndMigrated(roleUuidApiUuidList.getFirst(), true,false);
			List<ApiEntity> apiEntityList = apiDbService.findByUuidInAndStatus(roleUuidApiUuidList.getSecond(), true);

			if (CollectionUtils.isNotEmpty(apiEntityList) || CollectionUtils.isNotEmpty(roleEntityList)) {

				userDeptLevelRoleNameUrlExpandedDtoList.add(
						UserDepartmentLevelRoleAdapter.getUserDeptLevelRoleNameUrlExpandedDto(userDepartmentLevelEntity, roleEntityList, apiEntityList, transformationCache));
			}
		}
		userV2DepartmentLevelEntityList.addAll(userDeptLevelRoleNameUrlExpandedDtoList);

		return userV2DepartmentLevelEntityList;
	}

	private Pair<List<String>, List<String>> getRoleUuidApiUuidListOfUser(UserDepartmentLevelEntity userDepartmentLevelEntity) {

		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
		Set<String> finalRoleUuidSet = new HashSet<>();
		Set<String> finalApiUuidSet = new HashSet<>();

		List<String> roleUuidListParent = new ArrayList<>(userDepartmentLevelRoleEntityList.stream().map(entity -> entity.getRoleUuid()).collect(Collectors.toSet()));
		finalRoleUuidSet.addAll(roleUuidListParent);

		List<RoleAccessEntity> roleAccessEntityListChild;
		Set<String> roleUuidSetChild;
		Set<String> apiUuidSetChild;

		while (CollectionUtils.isNotEmpty(roleUuidListParent)) {
			roleAccessEntityListChild = roleAccessDbService.findByRoleUuidInAndStatus(roleUuidListParent, true);
			roleUuidSetChild =
					roleAccessEntityListChild.stream().filter(entity -> RoleAccessType.ROLE.equals(entity.getRoleAccessType())).map(entity -> entity.getAccessUuid()).collect(Collectors.toSet());
			apiUuidSetChild =
					roleAccessEntityListChild.stream().filter(entity -> RoleAccessType.API.equals(entity.getRoleAccessType())).map(entity -> entity.getAccessUuid()).collect(Collectors.toSet());
			roleUuidListParent = roleUuidSetChild.stream().filter(child -> !finalRoleUuidSet.contains(child)).collect(Collectors.toList());
			finalRoleUuidSet.addAll(roleUuidListParent);
			finalApiUuidSet.addAll(apiUuidSetChild);
		}

		return Pair.of(new ArrayList<>(finalRoleUuidSet), new ArrayList<>(finalApiUuidSet));

	}
}