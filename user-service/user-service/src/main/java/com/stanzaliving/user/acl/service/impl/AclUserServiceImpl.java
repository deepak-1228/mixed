package com.stanzaliving.user.acl.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.stanzaliving.core.base.enums.AccessModule;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.transformation.client.cache.TransformationCache;
import com.stanzaliving.core.user.acl.dto.AddUserAndRoleDto;
import com.stanzaliving.core.user.acl.dto.CityMicromarketDropdownResponseDto;
import com.stanzaliving.core.user.acl.dto.MicromarketAndResidencesDropdownRequestDto;
import com.stanzaliving.core.user.acl.dto.MicromarketAndResidencesDropdownResponseDto;
import com.stanzaliving.core.user.acl.dto.UpdateAccessModuleAccessLevelRequestDto;
import com.stanzaliving.core.user.acl.dto.UserAccessLevelIdsByRoleNameWithFiltersDto;
import com.stanzaliving.core.user.acl.dto.UserAccessLevelListDto;
import com.stanzaliving.core.user.acl.dto.UserAccessModuleDto;
import com.stanzaliving.core.user.acl.dto.UserDepartmentLevelAccessModulesDto;
import com.stanzaliving.core.user.acl.dto.UsersByAccessModulesAndCitiesRequestDto;
import com.stanzaliving.core.user.acl.dto.UsersByAccessModulesAndCitiesResponseDto;
import com.stanzaliving.core.user.acl.dto.UsersByFiltersRequestDto;
import com.stanzaliving.core.user.acl.dto.UsersByFiltersResponseDto;
import com.stanzaliving.core.user.acl.enums.Role;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleByEmailRequestDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.transformations.pojo.CityMetadataDto;
import com.stanzaliving.transformations.pojo.MicroMarketMetadataDto;
import com.stanzaliving.transformations.pojo.ResidenceMetadataDto;
import com.stanzaliving.user.acl.entity.RoleAccessModuleMappingEntity;
import com.stanzaliving.user.acl.repository.RoleAccessModuleRepository;
import com.stanzaliving.user.acl.repository.RoleRepository;
import com.stanzaliving.user.acl.repository.UserDepartmentLevelRepository;
import com.stanzaliving.user.acl.repository.UserDepartmentLevelRoleRepository;
import com.stanzaliving.user.adapters.Userv2ToUserAdapter;
import com.stanzaliving.user.feignclient.UserV2FeignService;
import com.stanzaliving.user.feignclient.Userv2HttpService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.core.kafka.producer.NotificationProducer;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleListDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleNameUrlExpandedDto;
import com.stanzaliving.core.user.acl.dto.UserRoleSnapshot;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.response.UserContactDetailsResponseDto;
import com.stanzaliving.user.acl.adapters.RoleAdapter;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelRoleAdapter;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.AclService;
import com.stanzaliving.user.acl.service.AclUserService;
import com.stanzaliving.user.acl.service.RoleService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelRoleService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Service
public class AclUserServiceImpl implements AclUserService {

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private RoleDbService roleDbService;

	@Autowired
	private UserDbService userDbService;

	@Autowired
	private UserDepartmentLevelService userDepartmentLevelService;

	@Autowired
	private UserDepartmentLevelDbService userDepartmentLevelDbService;

	@Autowired
	private UserDepartmentLevelRoleService userDepartmentLevelRoleService;

	@Autowired
	private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

	@Autowired
	private AclService aclService;

	@Autowired
	private NotificationProducer notificationProducer;

	@Value("${kafka.topic.role}")
	private String roleTopic;

	@Autowired
	private UserDepartmentLevelRepository userDepartmentLevelRepository;

	@Autowired
	private TransformationCache transformationCache;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserDepartmentLevelRoleRepository userDepartmentLevelRoleRepository;

	@Autowired
	private RoleAccessModuleRepository roleAccessModuleRepository;

	@Autowired
	private UserV2FeignService userV2FeignService;

	@Override
	public void addRole(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto) {

		UserEntity user=userDbService.findByUuidNotMigrated(addUserDeptLevelRoleDto.getUserUuid(),false);
		if(Objects.isNull(user)){
			throw new StanzaException("User might be migrated/created in acl2.0,please use new user management to assign permissions for this user.");
		}


		userService.assertActiveUserByUserUuid(addUserDeptLevelRoleDto.getUserUuid());

		AddUserDeptLevelRequestDto addUserDeptLevelRequestDto = new AddUserDeptLevelRequestDto(addUserDeptLevelRoleDto);

		UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelService.add(addUserDeptLevelRequestDto);

		userDepartmentLevelRoleService.addRoles(userDepartmentLevelEntity.getUuid(), addUserDeptLevelRoleDto.getRolesUuid());
		publishCurrentRoleSnapshot(addUserDeptLevelRoleDto.getUserUuid());
	}

	@Override
	public void revokeAllRolesOfDepartment(String userUuid, Department department) {

		userService.assertActiveUserByUserUuid(userUuid);

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndDepartment(userUuid, department);
		if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
			throw new ApiValidationException("User doesn't have any access in this department");
		}

		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			userDepartmentLevelService.delete(userDepartmentLevelEntity);
		}
		publishCurrentRoleSnapshot(userUuid);
		return;

	}

	@Override
	public List<UserDeptLevelRoleDto> getActiveUserDeptLevelRole(String userUuid) {
		userService.assertActiveUserByUserUuid(userUuid);
		return getUserDeptLevelRole(userUuid);
	}

	@Override
	public List<UserDeptLevelRoleDto> getUserDeptLevelRole(String userUuid) {

		List<UserDeptLevelRoleDto> userDeptLevelRoleDtoList = new ArrayList<>();
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList;

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);

		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
			userDeptLevelRoleDtoList.add(UserDepartmentLevelRoleAdapter.getUserDeptLevelRoleDto(userDepartmentLevelEntity, userDepartmentLevelRoleEntityList));
		}
		return userDeptLevelRoleDtoList;
	}

	@Override
	public List<RoleDto> getUserRoles(String userUuid) {
		List<RoleDto> roleDtoList = new ArrayList<>();
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList;
		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);
		List<String> roleUuids;

		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
			roleUuids = userDepartmentLevelRoleEntityList.parallelStream().map(UserDepartmentLevelRoleEntity::getRoleUuid).collect(Collectors.toList());
			List<RoleEntity> roleEntities = roleDbService.findByUuidInAndStatus(roleUuids, true);
			roleDtoList.addAll(RoleAdapter.getDtoList(roleEntities));
		}
		return roleDtoList;
//		List<RoleDto> roleDtos=userV2FeignService.getRolesFromUserUuid(userUuid);
//
//		List<RoleDto> roleDtoList = new ArrayList<>();
//		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList;
//		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndStatus(userUuid, true);
//		List<String> roleUuids;
//
//		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
//			userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
//			roleUuids = userDepartmentLevelRoleEntityList.parallelStream().map(UserDepartmentLevelRoleEntity::getRoleUuid).collect(Collectors.toList());
//			List<RoleEntity> roleEntities = roleDbService.findByUuidInAndStatus(roleUuids, true);
//			roleDtoList.addAll(RoleAdapter.getDtoList(roleEntities));
//		}
//
//		if(Objects.nonNull(roleDtos) && roleDtos.size()>0){
//			roleDtoList.addAll(roleDtos);
//		}
//		return roleDtoList;
	}

	@Override
	public void revokeAllRolesOfDepartmentOfLevel(String userUuid, Department department, AccessLevel accessLevel) {
		userService.assertActiveUserByUserUuid(userUuid);

		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevel(userUuid, department, accessLevel);
		if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
			throw new ApiValidationException("User doesn't have any access in this department");
		}

		for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
			userDepartmentLevelService.delete(userDepartmentLevelEntity);
		}
		publishCurrentRoleSnapshot(userUuid);
		return;
	}

	@Override
	public void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {

		userService.assertActiveUserByUserUuid(addUserDeptLevelRequestDto.getUserUuid());

		userDepartmentLevelService.revokeAccessLevelEntityForDepartmentOfLevel(addUserDeptLevelRequestDto);
		publishCurrentRoleSnapshot(addUserDeptLevelRequestDto.getUserUuid());
	}

	@Override
	public void revokeRolesForDepartmentOfLevel(UserDeptLevelRoleListDto userDeptLevelRoleListDto) {
		userService.assertActiveUserByUserUuid(userDeptLevelRoleListDto.getUserUuid());

		UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevelAndStatus(userDeptLevelRoleListDto.getUserUuid(),
				userDeptLevelRoleListDto.getDepartment(), userDeptLevelRoleListDto.getAccessLevel(), true);

		if (null == userDepartmentLevelEntity) {
			throw new ApiValidationException("Unable to revoke roles, User doesn't exist at this level in the department");
		}

		userDepartmentLevelRoleService.revokeRoles(userDepartmentLevelEntity.getUuid(), userDeptLevelRoleListDto.getRolesUuid());
		publishCurrentRoleSnapshot(userDeptLevelRoleListDto.getUserUuid());
	}

	@Override
	public Map<String, List<String>> getUsersForRoles(Department department, String roleName, List<String> accessLevelEntityList) {

		log.info("Got request to get list of userid by rolename {} and department {}", roleName, department);

		RoleDto roleDto = roleService.findByRoleNameAndDepartment(roleName, department);

		Map<String, List<String>> userIdAccessLevelIdListMap = new HashMap<>();

		if (Objects.nonNull(roleDto) && roleDto.getDepartment().equals(department)) {

			List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuid(roleDto.getUuid());

			if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {

				List<String> uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());

				List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndAccessLevel(uuids, roleDto.getAccessLevel());

				if (CollectionUtils.isNotEmpty(departmentLevelEntities)) {

					departmentLevelEntities.forEach(entity -> {

						Set<String> accessLevelUuids = new HashSet<>(Arrays.asList((entity.getCsvAccessLevelEntityUuid().split(","))));

						for (String accessLevelEntity : accessLevelEntityList) {
							if (accessLevelUuids.contains(accessLevelEntity)) {
								List<String> accessLevelIds = userIdAccessLevelIdListMap.getOrDefault(entity.getUserUuid(), new ArrayList<>());
								accessLevelIds.add(accessLevelEntity);
								userIdAccessLevelIdListMap.put(entity.getUserUuid(), accessLevelIds);
							}
						}
//						 if (!Collections.disjoint(accessLevelEntityList, accessLevelUuids)) {
//						 userIds.add(entity.getUserUuid());
//						 }
					});
				}
			}

		}

		return userIdAccessLevelIdListMap;
	}

	@Override
	public Map<String, List<String>> getActiveUsersForRoles(Department department, String roleName, List<String> accessLevelEntityList) {

		log.info("Got request to get list of userid by rolename {} and department {}", roleName, department);

		RoleDto roleDto = roleService.findByRoleNameAndDepartment(roleName, department);

		Map<String, List<String>> userIdAccessLevelIdListMap = new HashMap<>();

		if (Objects.nonNull(roleDto) && roleDto.getDepartment().equals(department)) {

			List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuidInAndStatus(Collections.singletonList(roleDto.getUuid()), true);

			if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {

				List<String> uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());

				List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndAccessLevel(uuids, roleDto.getAccessLevel());

				if (CollectionUtils.isNotEmpty(departmentLevelEntities)) {

					departmentLevelEntities.forEach(entity -> {

						UserEntity user = userDbService.findByUuid(entity.getUserUuid());

						if(user.isStatus() || (user.isMigrated())) {
							Set<String> accessLevelUuids = new HashSet<>(Arrays.asList((entity.getCsvAccessLevelEntityUuid().split(","))));

							for (String accessLevelEntity : accessLevelEntityList) {
								if (accessLevelUuids.contains(accessLevelEntity)) {
									List<String> accessLevelIds = userIdAccessLevelIdListMap.getOrDefault(entity.getUserUuid(), new ArrayList<>());
									accessLevelIds.add(accessLevelEntity);
									userIdAccessLevelIdListMap.put(entity.getUserUuid(), accessLevelIds);
								}
							}
						}
						// if (!Collections.disjoint(accessLevelEntityList, accessLevelUuids)) {
						// userIds.add(entity.getUserUuid());
						// }
					});
				}
			}

		}

		return userIdAccessLevelIdListMap;
//		log.info("Got request to get list of userid by rolename {} and department {}", roleName, department);
//
//		//call feign client to get roles by role and department
//		Map<String,List<String>> userAccessLevelMap=userV2FeignService.getActiveUserAndAccessLevelMapForRole(roleName,department);
//		Map<String, List<String>> userIdV2AccessLevelIdListMap = new HashMap<>();
//		if(userAccessLevelMap.size()>0) {
//			for (Map.Entry<String, List<String>> entry : userAccessLevelMap.entrySet()) {
//				for (String accessLevelEntity : accessLevelEntityList) {
//					if (entry.getValue().contains(accessLevelEntity)) {
//						List<String> accessLevelIds = userIdV2AccessLevelIdListMap.getOrDefault(entry.getKey(), new ArrayList<>());
//						accessLevelIds.add(accessLevelEntity);
//						userIdV2AccessLevelIdListMap.put(entry.getKey(), accessLevelIds);
//					}
//				}
//			}
//		}
//
//		RoleDto roleDto=null;
//		try{
//			roleDto = roleService.findByRoleNameAndDepartment(roleName, department);
//		}
//		catch (ApiValidationException e){}
//
//		Map<String, List<String>> userIdAccessLevelIdListMap = new HashMap<>();
//
//		if (Objects.nonNull(roleDto) && roleDto.getDepartment().equals(department)) {
//
//			List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuidInAndStatus(Collections.singletonList(roleDto.getUuid()), true);
//
//			if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {
//
//				List<String> uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());
//
//				List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndAccessLevel(uuids, roleDto.getAccessLevel());
//
//				if (CollectionUtils.isNotEmpty(departmentLevelEntities)) {
//
//					departmentLevelEntities.forEach(entity -> {
//
//						UserEntity user = userDbService.findByUuid(entity.getUserUuid());
//
//						if(user.isStatus()) {
//							Set<String> accessLevelUuids = new HashSet<>(Arrays.asList((entity.getCsvAccessLevelEntityUuid().split(","))));
//
//							for (String accessLevelEntity : accessLevelEntityList) {
//								if (accessLevelUuids.contains(accessLevelEntity)) {
//									List<String> accessLevelIds = userIdAccessLevelIdListMap.getOrDefault(entity.getUserUuid(), new ArrayList<>());
//									accessLevelIds.add(accessLevelEntity);
//									userIdAccessLevelIdListMap.put(entity.getUserUuid(), accessLevelIds);
//								}
//							}
//						}
//						// if (!Collections.disjoint(accessLevelEntityList, accessLevelUuids)) {
//						// userIds.add(entity.getUserUuid());
//						// }
//					});
//				}
//			}
//
//		}
//
//		userIdAccessLevelIdListMap.forEach((k,v)->{
//			if(!userIdV2AccessLevelIdListMap.containsKey(k)){
//				userIdV2AccessLevelIdListMap.put(k,v);
//			}
//		});
//
//		return userIdV2AccessLevelIdListMap;
	}


	@Override
	public Map<String, List<String>> getActiveUsersForRole(String roleName, Set<String> accessLevelEntityList) {

		log.info("Got request to get list of userid by rolename {}", roleName);

		RoleDto roleDto = roleService.findByRoleName(roleName);

		Map<String, List<String>> userIdAccessLevelIdListMap = new HashMap<>();

		if (Objects.nonNull(roleDto) ) {

			List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuid(roleDto.getUuid());

			if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {

				List<String> uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());

				List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndAccessLevel(uuids, roleDto.getAccessLevel());

				if (CollectionUtils.isNotEmpty(departmentLevelEntities)) {

					departmentLevelEntities.forEach(entity -> {

						UserEntity user = userDbService.findByUuid(entity.getUserUuid());

						if(user.isStatus()) {
							Set<String> accessLevelUuids = new HashSet<>(Arrays.asList((entity.getCsvAccessLevelEntityUuid().split(","))));

							for (String accessLevelEntity : accessLevelEntityList) {
								if (accessLevelUuids.contains(accessLevelEntity)) {
									List<String> accessLevelIds = userIdAccessLevelIdListMap.getOrDefault(entity.getUserUuid(), new ArrayList<>());
									accessLevelIds.add(accessLevelEntity);
									userIdAccessLevelIdListMap.put(entity.getUserUuid(), accessLevelIds);
								}
							}
						}
					});
				}
			}

		}

		return userIdAccessLevelIdListMap;
	}

	@Override
	public List<UserContactDetailsResponseDto> getUserContactDetails(Department department, String roleName, List<String> accessLevelEntity) {
		List<String> userUuids = new ArrayList<>(getUsersForRoles(department, roleName, accessLevelEntity).keySet());

		if (CollectionUtils.isEmpty(userUuids)) {
			return Collections.emptyList();
		}

		List<com.stanzaliving.user.dto.userv2.UserDto> userDtos=userV2FeignService.getUsersList(userUuids);

		List<UserEntity> userEntities=new ArrayList<>();

		if(Objects.nonNull(userDtos)) {
			for (com.stanzaliving.user.dto.userv2.UserDto userDto : userDtos) {
				userEntities.add(Userv2ToUserAdapter.getUserEntityFromUserv2(userDto));
			}
		}

		List<UserEntity> userEntities2 = userDbService.findByUuidInAndStatus(userUuids, true);

		if(Objects.nonNull(userEntities2)) {
			userEntities.addAll(userEntities2);
		}

		if (CollectionUtils.isEmpty(userEntities)) {
			return Collections.emptyList();
		}

		return userEntities.parallelStream().map(UserAdapter::convertToContactResponseDto).collect(Collectors.toList());
	}

	@Override
	public void bulkAddRole(AddUserDeptLevelRoleByEmailRequestDto addUserDeptLevelRoleByEmailRequestDto) {
		Map<String,String> userUuids = userDbService.getUuidByEmail(addUserDeptLevelRoleByEmailRequestDto.getEmails());

		addUserDeptLevelRoleByEmailRequestDto.getEmails()
						.forEach(email->{
							if (!userUuids.keySet().contains(email)){
								throw new ApiValidationException("No user exists with email id: "+email);
							}
						});

		userUuids
				.forEach((email,uuid) -> {
							addRole(
									AddUserDeptLevelRoleRequestDto
											.builder()
											.rolesUuid(addUserDeptLevelRoleByEmailRequestDto.getRolesUuid())
											.userUuid(uuid)
											.department(addUserDeptLevelRoleByEmailRequestDto.getDepartment())
											.accessLevel(addUserDeptLevelRoleByEmailRequestDto.getAccessLevel())
											.accessLevelEntityListUuid(addUserDeptLevelRoleByEmailRequestDto.getAccessLevelEntityListUuid())
											.build()
							);
						}
				);
	}

	private void publishCurrentRoleSnapshot(String userUuid) {
		List<UserDeptLevelRoleNameUrlExpandedDto> data = aclService.getUserDeptLevelRoleNameUrlExpandedDtoBe(userUuid);
		UserRoleSnapshot userRoleSnapshot = UserRoleSnapshot.builder().userUuid(userUuid).userDeptLevelRoles(data).build();
		notificationProducer.publish(roleTopic, UserRoleSnapshot.class.getName(), userRoleSnapshot);
	}

	@Override
	public Set<String> getAccessLevelIds(Department department, String roleName) {
		log.info("Got request to get list of userid by rolename {} and department {}", roleName, department);
		RoleDto roleDto = roleService.findByRoleNameAndDepartment(roleName, department);
		Set<String> access_level_entity_uuids =new HashSet<>();
		if (Objects.nonNull(roleDto) && roleDto.getDepartment().equals(department)) {
			List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuid(roleDto.getUuid());
			if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {
				List<String> user_department_level_uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());
				List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndDepartmentAndAccessLevel(user_department_level_uuids, roleDto.getDepartment(),roleDto.getAccessLevel());
				access_level_entity_uuids = departmentLevelEntities.stream().map(UserDepartmentLevelEntity::getCsvAccessLevelEntityUuid).collect(Collectors.toSet());
			}
		}
		return access_level_entity_uuids;
	}

	@Override
	public Map<String, List<String>> getUsersForRolesWithFilters(UserAccessLevelIdsByRoleNameWithFiltersDto requestDto) {

		log.info("Got request to get list of userid by rolename {} and department {}", requestDto.getRoleName(), requestDto.getDepartment());
		log.info("UserAccessLevelIdsByRoleNameWithFiltersDto : {}", requestDto);

		RoleDto roleDto = roleService.findByRoleNameAndDepartment(requestDto.getRoleName(), requestDto.getDepartment());

		Map<String, List<String>> userIdAccessLevelIdListMap = new HashMap<>();

		if (Objects.nonNull(roleDto) && roleDto.getDepartment().equals(requestDto.getDepartment())) {

			List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuid(roleDto.getUuid());

			if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {

				List<String> uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());

				List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndAccessLevel(uuids, roleDto.getAccessLevel());

				if (CollectionUtils.isNotEmpty(departmentLevelEntities)) {

					departmentLevelEntities.forEach(entity -> {

						Set<String> accessLevelUuids = new HashSet<>(Arrays.asList((entity.getCsvAccessLevelEntityUuid().split(","))));

						for (String accessLevelEntity : requestDto.getAccessLevelId()) {
							if (accessLevelUuids.contains(accessLevelEntity)) {
								List<String> accessLevelIds = userIdAccessLevelIdListMap.getOrDefault(entity.getUserUuid(), new ArrayList<>());
								accessLevelIds.add(accessLevelEntity);
								userIdAccessLevelIdListMap.put(entity.getUserUuid(), accessLevelIds);
							}
						}
					});
				}
			}

		}
		if (CollectionUtils.isNotEmpty(requestDto.getCityFilterUuids()) || CollectionUtils.isNotEmpty(requestDto.getMicromarketFilterUuids())
			|| CollectionUtils.isNotEmpty(requestDto.getResidenceFilterUuids()) || CollectionUtils.isNotEmpty(requestDto.getCityLeadFilterUuids())
			|| CollectionUtils.isNotEmpty(requestDto.getClusterManagerFilterUuids())
			|| CollectionUtils.isNotEmpty(requestDto.getSalesAssociateFilterUuids())) {

			UsersByFiltersRequestDto usersByFiltersRequestDto = UsersByFiltersRequestDto.builder().accessLevel(requestDto.getAccessLevel())
				.cityFilterUuids(requestDto.getCityFilterUuids()).micromarketFilterUuids(requestDto.getMicromarketFilterUuids())
				.residenceFilterUuids(requestDto.getResidenceFilterUuids()).cityLeadFilterUuids(requestDto.getCityLeadFilterUuids())
				.clusterManagerFilterUuids(requestDto.getClusterManagerFilterUuids()).salesAssociateFilterUuids(requestDto.getSalesAssociateFilterUuids()).build();

			UsersByFiltersResponseDto usersByFiltersResponseDto = getUsersWithFilters(usersByFiltersRequestDto);
			List<String> userUuidsWithFilters = new ArrayList<>();
			if (usersByFiltersRequestDto.getAccessLevel() == AccessLevel.CITY && Objects.nonNull(usersByFiltersResponseDto)) {
				userUuidsWithFilters = usersByFiltersResponseDto.getCityHeadUuids();
			} else if (usersByFiltersRequestDto.getAccessLevel() == AccessLevel.MICROMARKET && Objects.nonNull(usersByFiltersResponseDto)) {
				userUuidsWithFilters = usersByFiltersResponseDto.getClusterManagerUuids();
			} else if (usersByFiltersRequestDto.getAccessLevel() == AccessLevel.RESIDENCE && Objects.nonNull(usersByFiltersResponseDto)) {
				userUuidsWithFilters = usersByFiltersResponseDto.getSalesAssociateUuids();
			}
			Map<String, List<String>> userIdAccessLevelIdListMapWithFilters = new HashMap<>();
			for (String uuid : userIdAccessLevelIdListMap.keySet()) {
				if (CollectionUtils.isNotEmpty(userUuidsWithFilters) && userUuidsWithFilters.contains(uuid)) {
					userIdAccessLevelIdListMapWithFilters.put(uuid, userIdAccessLevelIdListMap.get(uuid));
				}
			}
			return userIdAccessLevelIdListMapWithFilters;
		}
		return userIdAccessLevelIdListMap;
	}


	private UsersByFiltersResponseDto getUsersWithFilters(UsersByFiltersRequestDto filtersRequestDto) {

		if (filtersRequestDto.getAccessLevel() == AccessLevel.CITY) {
			return getCityLeadsWithFilters(filtersRequestDto);
		}
        if (filtersRequestDto.getAccessLevel() == AccessLevel.MICROMARKET) {
            return getClusterManagersWithFilters(filtersRequestDto);
        }
        if (filtersRequestDto.getAccessLevel() == AccessLevel.RESIDENCE) {
            return getSalesAssociatesWithFilters(filtersRequestDto);
        }
		return null;
	}

	private UsersByFiltersResponseDto getCityLeadsWithFilters(UsersByFiltersRequestDto filtersRequestDto) {
		log.info("Get City Leads with Filters");
		List<String> cityUuidsWithEntityFilters = getCityUuidsWithEntityFilters(filtersRequestDto);
		List<String> cityUuidsWithUserFilters = getCityUuidsWithUserFilters(filtersRequestDto);
		List<String> cityFilters = new ArrayList<>();
		if (CollectionUtils.isEmpty(cityUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(cityUuidsWithUserFilters)) {
			cityFilters = cityUuidsWithUserFilters;
		}
		if (CollectionUtils.isEmpty(cityUuidsWithUserFilters) && CollectionUtils.isNotEmpty(cityUuidsWithEntityFilters)) {
			cityFilters = cityUuidsWithEntityFilters;
		}
		if (CollectionUtils.isNotEmpty(cityUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(cityUuidsWithUserFilters)) {
			cityFilters = cityUuidsWithEntityFilters.stream().distinct().filter(cityUuidsWithUserFilters::contains).collect(Collectors.toList());
		}
		List<String> allCityLeads = new ArrayList<>();
		UsersByFiltersResponseDto usersByFiltersResponseDto = new UsersByFiltersResponseDto();
		if (CollectionUtils.isEmpty(cityFilters)) {
			for (UserAccessLevelListDto userAccessLevelListDto : getAllCityLeadManagers()) {
				allCityLeads.add(userAccessLevelListDto.getUserUuid());
			}
		} else {
			for (UserAccessLevelListDto userAccessLevelListDto : getAllCityLeadManagers()) {
				for (String accessLevelId : userAccessLevelListDto.getAccessLevelIds()) {
					if (cityFilters.contains(accessLevelId)) {
						allCityLeads.add(userAccessLevelListDto.getUserUuid());
						break;
					}
				}
			}
		}
		usersByFiltersResponseDto.setCityHeadUuids(allCityLeads);
		log.info("Users by filters : {}", usersByFiltersResponseDto);
		return usersByFiltersResponseDto;
	}

    private UsersByFiltersResponseDto getClusterManagersWithFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get Cluster Managers with Filters");
        List<String> micromarketUuidsWithEntityFilters = getMicromarketUuidsWithEntityFilters(filtersRequestDto);
        List<String> micromarketUuidsWithUserFilters = getMicromarketUuidsWithUserFilters(filtersRequestDto);
        List<String> micromarketFilters = new ArrayList<>();
        if (CollectionUtils.isEmpty(micromarketUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(micromarketUuidsWithUserFilters)) {
            micromarketFilters = micromarketUuidsWithUserFilters;
        }
        if (CollectionUtils.isEmpty(micromarketUuidsWithUserFilters) && CollectionUtils.isNotEmpty(micromarketUuidsWithEntityFilters)) {
            micromarketFilters = micromarketUuidsWithEntityFilters;
        }
        if (CollectionUtils.isNotEmpty(micromarketUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(micromarketUuidsWithUserFilters)) {
            micromarketFilters = micromarketUuidsWithEntityFilters.stream().distinct().filter(micromarketUuidsWithUserFilters::contains).collect(Collectors.toList());
        }
        List<String> allClusterManagers = new ArrayList<>();
        UsersByFiltersResponseDto usersByFiltersResponseDto = new UsersByFiltersResponseDto();
        if (CollectionUtils.isEmpty(micromarketFilters)) {
            for (UserAccessLevelListDto userAccessLevelListDto : getAllMicromarketLeadManagers()) {
                allClusterManagers.add(userAccessLevelListDto.getUserUuid());
            }
        } else {
            for (UserAccessLevelListDto userAccessLevelListDto : getAllMicromarketLeadManagers()) {
                for (String accessLevelId : userAccessLevelListDto.getAccessLevelIds()) {
                    if (micromarketFilters.contains(accessLevelId)) {
                        allClusterManagers.add(userAccessLevelListDto.getUserUuid());
                        break;
                    }
                }
            }
        }
        usersByFiltersResponseDto.setClusterManagerUuids(allClusterManagers);
		log.info("Users by filters : {}", usersByFiltersResponseDto);
        return usersByFiltersResponseDto;
    }

    private UsersByFiltersResponseDto getSalesAssociatesWithFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get Sales Associates with Filters");
        List<String> residenceUuidsWithEntityFilters = getResidenceUuidsWithEntityFilters(filtersRequestDto);
        List<String> residenceUuidsWithUserFilters = getResidenceUuidsWithUserFilters(filtersRequestDto);
        List<String> residenceFilters = new ArrayList<>();
        if (CollectionUtils.isEmpty(residenceUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(residenceUuidsWithUserFilters)) {
            residenceFilters  = residenceUuidsWithUserFilters;
        }
        if (CollectionUtils.isEmpty(residenceUuidsWithUserFilters) && CollectionUtils.isNotEmpty(residenceUuidsWithEntityFilters)) {
            residenceFilters = residenceUuidsWithEntityFilters;
        }
        if (CollectionUtils.isNotEmpty(residenceUuidsWithEntityFilters) && CollectionUtils.isNotEmpty(residenceUuidsWithUserFilters)) {
            residenceFilters = residenceUuidsWithEntityFilters.stream().distinct().filter(residenceUuidsWithUserFilters::contains).collect(Collectors.toList());
        }
        List<String> allSalesAssociates = new ArrayList<>();
        UsersByFiltersResponseDto usersByFiltersResponseDto = new UsersByFiltersResponseDto();
        if (CollectionUtils.isEmpty(residenceFilters)) {
            for (UserAccessLevelListDto userAccessLevelListDto : getAllResidenceLeadManagers()) {
                allSalesAssociates.add(userAccessLevelListDto.getUserUuid());
            }
        } else {
            for (UserAccessLevelListDto userAccessLevelListDto : getAllResidenceLeadManagers()) {
                for (String accessLevelId : userAccessLevelListDto.getAccessLevelIds()) {
                    if (residenceFilters.contains(accessLevelId)) {
                        allSalesAssociates.add(userAccessLevelListDto.getUserUuid());
                        break;
                    }
                }
            }
        }
        usersByFiltersResponseDto.setSalesAssociateUuids(allSalesAssociates);
		log.info("Users by filters : {}", usersByFiltersResponseDto);
        return usersByFiltersResponseDto;
    }

	private List<String> getCityUuidsWithEntityFilters(UsersByFiltersRequestDto filtersRequestDto) {
		log.info("Get City uuids with entity filters");
		List<String> cityUuids = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(filtersRequestDto.getResidenceFilterUuids())) {
			for (String residenceUuid : filtersRequestDto.getResidenceFilterUuids()) {
				cityUuids.add(transformationCache.getCityUuidByResidenceUuid(residenceUuid));
			}
			filtersRequestDto.setCityFilterUuids(cityUuids);
		}
		if (CollectionUtils.isEmpty(cityUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getMicromarketFilterUuids())) {
			for (String micromarketUuid : filtersRequestDto.getMicromarketFilterUuids()) {
				cityUuids.add(transformationCache.getCityUuidByMicromarketUuid(micromarketUuid));
			}
			filtersRequestDto.setCityFilterUuids(cityUuids);
		}
		if (CollectionUtils.isEmpty(cityUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityFilterUuids())) {
			cityUuids.addAll(filtersRequestDto.getCityFilterUuids());
			filtersRequestDto.setCityFilterUuids(cityUuids);
		}
		log.info("City Uuids with entity filters : {}", cityUuids);
		return cityUuids;
	}

	private List<String> getCityUuidsWithUserFilters(UsersByFiltersRequestDto filtersRequestDto) {
		log.info("Get City uuids with user filters");
		List<String> cityUuids = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(filtersRequestDto.getSalesAssociateFilterUuids())) {
			List<String> accessLevelIds = new ArrayList<>();
			List<UserAccessLevelListDto> allSalesAssociates = getAllResidenceLeadManagers();
			for (UserAccessLevelListDto userAccessLevelListDto : allSalesAssociates) {
				if (filtersRequestDto.getSalesAssociateFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
			}
			for (String residenceUuid : accessLevelIds) {
				cityUuids.add(transformationCache.getCityUuidByResidenceUuid(residenceUuid));
			}
		}
		if (CollectionUtils.isEmpty(cityUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getClusterManagerFilterUuids())) {
			List<String> accessLevelIds = new ArrayList<>();
			List<UserAccessLevelListDto> allClusterManagers = getAllMicromarketLeadManagers();
			for (UserAccessLevelListDto userAccessLevelListDto : allClusterManagers) {
				if (filtersRequestDto.getClusterManagerFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
			}
			for (String micromarketUuid : accessLevelIds) {
				cityUuids.add(transformationCache.getCityUuidByMicromarketUuid(micromarketUuid));
			}
		}
		log.info("City Uuids with user filters : {}", cityUuids);
		return cityUuids;
	}

    private List<String> getMicromarketUuidsWithEntityFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get micromarket with entity filters");
        List<String> micromarketUuids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filtersRequestDto.getResidenceFilterUuids())) {
            for (String residenceUuid : filtersRequestDto.getResidenceFilterUuids()) {
                micromarketUuids.add(transformationCache.getMicromarketUuidByResidenceUuid(residenceUuid));
            }
            filtersRequestDto.setMicromarketFilterUuids(micromarketUuids);
        }
        if (CollectionUtils.isEmpty(micromarketUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getMicromarketFilterUuids())) {
            micromarketUuids.addAll(filtersRequestDto.getMicromarketFilterUuids());
            filtersRequestDto.setMicromarketFilterUuids(micromarketUuids);
        }
        if (CollectionUtils.isEmpty(micromarketUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityFilterUuids())) {
            for (String cityUuid : filtersRequestDto.getCityFilterUuids()) {
				if(Objects.nonNull(cityUuid)) {
					List<String> micromarketUuidsByCityUuid = transformationCache.getMicromarketUuidsByCityUuid(cityUuid);
					if(CollectionUtils.isNotEmpty(micromarketUuidsByCityUuid))
						micromarketUuids.addAll(micromarketUuidsByCityUuid);
				}
            }
            filtersRequestDto.setMicromarketFilterUuids(micromarketUuids);
        }
		log.info("Micromarket Uuids with entity filters : {}", micromarketUuids);
        return micromarketUuids;
    }

    private List<String> getMicromarketUuidsWithUserFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get Micromarket uuids with user filters");
        List<String> micromarketUuids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filtersRequestDto.getSalesAssociateFilterUuids())) {
            List<String> accessLevelIds = new ArrayList<>();
            List<UserAccessLevelListDto> allSalesAssociates = getAllResidenceLeadManagers();
            for (UserAccessLevelListDto userAccessLevelListDto : allSalesAssociates) {
            	if (filtersRequestDto.getSalesAssociateFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
            }
            for (String residenceUuid : accessLevelIds) {
                micromarketUuids.add(transformationCache.getMicromarketUuidByResidenceUuid(residenceUuid));
            }
        }
        if (CollectionUtils.isEmpty(micromarketUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityLeadFilterUuids())) {
            List<String> accessLevelIds = new ArrayList<>();
            List<UserAccessLevelListDto> allCityLeads = getAllCityLeadManagers();
            for (UserAccessLevelListDto userAccessLevelListDto : allCityLeads) {
				if (filtersRequestDto.getCityLeadFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
            }
            for (String cityUuid : accessLevelIds) {
                micromarketUuids.addAll(transformationCache.getMicromarketUuidsByCityUuid(cityUuid));
            }
        }
		log.info("MM Uuids with user filters : {}", micromarketUuids);
        return micromarketUuids;
    }

    private List<String> getResidenceUuidsWithEntityFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get residences with entity filters");
        List<String> residenceUuids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filtersRequestDto.getResidenceFilterUuids())) {
            residenceUuids.addAll(filtersRequestDto.getResidenceFilterUuids());
            filtersRequestDto.setResidenceFilterUuids(residenceUuids);
        }
        if (CollectionUtils.isEmpty(residenceUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getMicromarketFilterUuids())) {
            for (String micromarketUuid : filtersRequestDto.getMicromarketFilterUuids()) {
                residenceUuids.addAll(transformationCache.getResidenceUuidsByMicromarketUuid(micromarketUuid));
            }
            filtersRequestDto.setResidenceFilterUuids(residenceUuids);
        }
        if (CollectionUtils.isEmpty(residenceUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityFilterUuids())) {
            for (String cityUuid : filtersRequestDto.getCityFilterUuids()) {
                residenceUuids.addAll(transformationCache.getResidenceUuidsByCityUuid(cityUuid));
            }
            filtersRequestDto.setResidenceFilterUuids(residenceUuids);
        }
		log.info("Residence Uuids with entity filters : {}", residenceUuids);
        return residenceUuids;
    }

    private List<String> getResidenceUuidsWithUserFilters(UsersByFiltersRequestDto filtersRequestDto) {
        log.info("Get Residence uuids with user filters");
        List<String> residenceUuids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(filtersRequestDto.getClusterManagerFilterUuids())) {
            List<String> accessLevelIds = new ArrayList<>();
            List<UserAccessLevelListDto> allClusterManagers = getAllMicromarketLeadManagers();
            for (UserAccessLevelListDto userAccessLevelListDto : allClusterManagers) {
            	if (filtersRequestDto.getClusterManagerFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
            }
            for (String micromarketUuid : accessLevelIds) {
                residenceUuids.addAll(transformationCache.getResidenceUuidsByMicromarketUuid(micromarketUuid));
            }
        }
        if (CollectionUtils.isEmpty(residenceUuids) && CollectionUtils.isNotEmpty(filtersRequestDto.getCityLeadFilterUuids())) {
            List<String> accessLevelIds = new ArrayList<>();
            List<UserAccessLevelListDto> allCityLeads = getAllCityLeadManagers();
            for (UserAccessLevelListDto userAccessLevelListDto : allCityLeads) {
				if (filtersRequestDto.getCityLeadFilterUuids().contains(userAccessLevelListDto.getUserUuid())) {
					accessLevelIds.addAll(userAccessLevelListDto.getAccessLevelIds());
				}
            }
            for (String cityUuid : accessLevelIds) {
                residenceUuids.addAll(transformationCache.getResidenceUuidsByCityUuid(cityUuid));
            }
        }
		log.info("Residence Uuids with user filters : {}", residenceUuids);
        return residenceUuids;
    }

	private List<UserAccessLevelListDto> getAllCityLeadManagers() {
		log.info("Get All City Lead Managers");
        List<UserAccessLevelListDto> userAccessLevelListDtoList = getSalesUsersAndAccessLevelsByRole(Arrays.asList(Role.CITY_LEAD_MANAGER.getRoleName(),
			Role.CITY_APARTMENT_LEAD_MANAGER.getRoleName()));
		log.info("Number of City Lead Managers : {}", userAccessLevelListDtoList.size());
		return userAccessLevelListDtoList;
	}

	private List<UserAccessLevelListDto> getAllMicromarketLeadManagers() {
		log.info("Get All Micromarket Lead Managers");
        List<UserAccessLevelListDto> userAccessLevelListDtoList = getSalesUsersAndAccessLevelsByRole(Arrays.asList(Role.MICROMARKET_LEAD_MANAGER.getRoleName(),
			Role.MICROMARKET_APARTMENT_LEAD_MANAGER.getRoleName()));
		log.info("Number of Micromarket Lead Managers : {}", userAccessLevelListDtoList.size());
		return userAccessLevelListDtoList;
	}

	private List<UserAccessLevelListDto> getAllResidenceLeadManagers() {
		log.info("Get All Residence Lead Managers");
		List<UserAccessLevelListDto> userAccessLevelListDtoList = getSalesUsersAndAccessLevelsByRole(Arrays.asList(Role.RESIDENCE_LEAD_MANAGER.getRoleName(),
			Role.RESIDENCE_APARTMENT_LEAD_MANAGER.getRoleName()));
		log.info("Number of Residence Lead Managers : {}", userAccessLevelListDtoList.size());
		return userAccessLevelListDtoList;
	}

	private List<UserAccessLevelListDto> getSalesUsersAndAccessLevelsByRole(List<String> rolesList) {
		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = new ArrayList<>();
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = new ArrayList<>();
		List<RoleEntity> roleEntityList = roleRepository.findByRoleNameInAndDepartment(rolesList, Department.SALES);
		for (RoleEntity roleEntity : roleEntityList) {
			List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities = userDepartmentLevelRoleRepository.findByRoleUuid(roleEntity.getUuid());
			if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntities)) {
				userDepartmentLevelRoleEntityList.addAll(userDepartmentLevelRoleEntities);
			}
		}
		if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntityList)) {
			for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
				UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelRepository.findByUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());
				if (Objects.nonNull(userDepartmentLevelEntity)) {
					userDepartmentLevelEntityList.add(userDepartmentLevelEntity);
				}
			}
		}
		List<UserAccessLevelListDto> userAccessLevelListDtoList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(userDepartmentLevelEntityList)) {
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				String[] accessLevelIdArray = userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split(",");
				UserAccessLevelListDto userAccessLevelListDto = new UserAccessLevelListDto();
				userAccessLevelListDto.setUserUuid(userDepartmentLevelEntity.getUserUuid());
				userAccessLevelListDto.setAccessLevelIds(Arrays.asList(accessLevelIdArray));
				userAccessLevelListDtoList.add(userAccessLevelListDto);
			}
		}
		return userAccessLevelListDtoList;
	}

	@Override
	public List<UserAccessModuleDto> getUserAccessModulesByUserUuid(String userUuid) {

		log.info("Search for access modules for user : {}", userUuid);
		List<UserAccessModuleDto> userAccessModuleDtoList = new ArrayList<>();
		List<String> roleUuids = new ArrayList<>();
		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelRepository.findByUserUuidAndStatus(userUuid, true);
		if (CollectionUtils.isNotEmpty(userDepartmentLevelEntityList)) {
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleRepository.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntity.getUuid(), true);
				if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntityList)) {
					for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
						roleUuids.add(userDepartmentLevelRoleEntity.getRoleUuid());
					}
				}
			}
		}
		if (CollectionUtils.isNotEmpty(roleUuids)) {
			List<AccessModule> accessModuleList = roleAccessModuleRepository
				.findAccessModuleByRoleUuidInAndAccessLevelInAndStatus(roleUuids, Arrays.asList(AccessLevel.COUNTRY, AccessLevel.CITY), true);
			if (CollectionUtils.isNotEmpty(accessModuleList)) {
				for (AccessModule accessModule : accessModuleList) {
					UserAccessModuleDto userAccessModuleDto = new UserAccessModuleDto();
					userAccessModuleDto.setAccessModule(accessModule);
					userAccessModuleDto.setAccessModuleName(accessModule.getName());
					userAccessModuleDtoList.add(userAccessModuleDto);
				}
			}
		}
		return userAccessModuleDtoList;
	}

	@Override
	public List<CityMetadataDto> getCitiesByUserAcessAndDepartment(String userUuid, Department department) {
		log.info("Get cities for user : {} and department : {}", userUuid, department);
		UserDepartmentLevelEntity userDepartmentLevelEntitiesByCountry = userDepartmentLevelRepository
			.findByUserUuidAndDepartmentAndAccessLevelAndStatus(userUuid, department, AccessLevel.COUNTRY, true);
		if (Objects.nonNull(userDepartmentLevelEntitiesByCountry)) {
			List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities = userDepartmentLevelRoleRepository
				.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntitiesByCountry.getUuid(), true);
			if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntities)) {
				List<String> roleUuids = userDepartmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getRoleUuid).collect(Collectors.toList());
				List<RoleAccessModuleMappingEntity> roleAccessModuleMappingEntities = roleAccessModuleRepository.findByRoleUuidInAndStatus(roleUuids, true);
				if (CollectionUtils.isNotEmpty(roleAccessModuleMappingEntities)) {
					log.info("User is having country level access for the access modules");
					return transformationCache.getAllCities();
				}
			}
		}
		UserDepartmentLevelEntity userDepartmentLevelEntitiesByCity = userDepartmentLevelRepository
			.findByUserUuidAndDepartmentAndAccessLevelAndStatus(userUuid, department, AccessLevel.CITY, true);
		if (Objects.nonNull(userDepartmentLevelEntitiesByCity)) {
			List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities = userDepartmentLevelRoleRepository
				.findByUserDepartmentLevelUuidAndStatus(userDepartmentLevelEntitiesByCity.getUuid(), true);
			if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntities)) {
				List<String> roleUuids = userDepartmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getRoleUuid).collect(Collectors.toList());
				List<RoleAccessModuleMappingEntity> roleAccessModuleMappingEntities = roleAccessModuleRepository.findByRoleUuidInAndStatus(roleUuids, true);
				if (CollectionUtils.isNotEmpty(roleAccessModuleMappingEntities)) {
					List<String> cityUuids = Arrays.asList(userDepartmentLevelEntitiesByCity.getCsvAccessLevelEntityUuid().split(","));
					List<CityMetadataDto> cityMetadataDtos = new ArrayList<>();
					for (String cityUuid : cityUuids) {
						if (CollectionUtils.isNotEmpty(cityUuids)) {
							cityMetadataDtos.add(transformationCache.getCityByUuid(cityUuid));
						}
					}
					return cityMetadataDtos;
				}

			}
		}
		return new ArrayList<>();
	}

	@Override
	public List<UsersByAccessModulesAndCitiesResponseDto> getUsersByAccessModulesAndCitites(UsersByAccessModulesAndCitiesRequestDto requestDto,
																							String userUuid) {

		log.info("Get Users by access modules and cities : {}", requestDto);
		List<String> roleUuids = roleAccessModuleRepository.findRoleUuidByAccessModuleInAndStatus(requestDto.getAccessModuleList(), true);
		if (CollectionUtils.isNotEmpty(roleUuids)) {
			log.info("Role uuids : {}", roleUuids);
			List<String> userDepartmentLevelUuids = userDepartmentLevelRoleRepository.findUserDepartmentLevelUuidByRoleUuidInAndStatus(roleUuids, true);
			if (CollectionUtils.isNotEmpty(userDepartmentLevelUuids)) {
				return getUsersByCities(requestDto, userDepartmentLevelUuids, userUuid);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private List<UsersByAccessModulesAndCitiesResponseDto> getUsersByCities(UsersByAccessModulesAndCitiesRequestDto requestDto,
																			List<String> userDepartmentLevelUuids, String userUuid) {
		log.info("User Department Level Uuids found with access modules {}", userDepartmentLevelUuids);
		List<UsersByAccessModulesAndCitiesResponseDto> responseDtoList = new ArrayList<>();
		if (requestDto.getAccessLevel() == AccessLevel.CITY)
			getCityHeads(requestDto, userDepartmentLevelUuids, responseDtoList, userUuid);
		else if (requestDto.getAccessLevel() == AccessLevel.MICROMARKET)
			getClusterManagers(requestDto, userDepartmentLevelUuids, responseDtoList, userUuid);
		else if (requestDto.getAccessLevel() == AccessLevel.RESIDENCE)
			getSalesAssociates(requestDto, userDepartmentLevelUuids, responseDtoList, userUuid);
		return responseDtoList.stream().sorted(Comparator.comparing(UsersByAccessModulesAndCitiesResponseDto::getAccessLevelEntityName))
			.collect(Collectors.toList());
	}

	private void getSalesAssociates(UsersByAccessModulesAndCitiesRequestDto requestDto, List<String> userDepartmentLevelUuids,
									List<UsersByAccessModulesAndCitiesResponseDto> responseDtoList, String userUuid) {
		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelRepository
			.findByUuidInAndAccessLevel(userDepartmentLevelUuids, AccessLevel.RESIDENCE);
		if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
			return;
		}
		List<String> userUuids = new ArrayList<>();
		Map<String, UserProfileDto> userMap = new HashMap<>();
		if (Objects.nonNull(userDepartmentLevelEntityList) && !userDepartmentLevelEntityList.isEmpty()) {
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				userUuids.add(userDepartmentLevelEntity.getUserUuid());
			}
			if (Objects.nonNull(userUuids) && !userUuids.isEmpty()) {
				List<UserProfileDto> users = getAllActiveUserByUserUuid(userUuids);
				for (UserProfileDto up : users) {
					userMap.put(up.getUuid(), up);
				}
			}
		}
		List<String> micromarketUuids = new ArrayList<>();
		Map<String, List<String>> micromarketResidenceMap = new HashMap<>();
		for (String cityUuid : requestDto.getCityUuids()) {
			Optional.ofNullable(transformationCache.getMicromarketUuidsByCityUuid(cityUuid))
				.ifPresent(micromarketUuids::addAll);
		}
		if (CollectionUtils.isEmpty(micromarketUuids)) {
			return;
		}
		for (String micromarketUuid : micromarketUuids) {
			if (CollectionUtils.isNotEmpty(transformationCache.getResidenceUuidsByMicromarketUuid(micromarketUuid))) {
				micromarketResidenceMap.put(micromarketUuid, transformationCache.getResidenceUuidsByMicromarketUuid(micromarketUuid));
			}
		}
		if (MapUtils.isNotEmpty(micromarketResidenceMap)) {
			for (Map.Entry<String, List<String>> entry : micromarketResidenceMap.entrySet()) {
				UsersByAccessModulesAndCitiesResponseDto responseDto = new UsersByAccessModulesAndCitiesResponseDto();
				responseDto.setAccessLevelEntityUuid(entry.getKey());
				responseDto.setAccessLevelEntityName(transformationCache.getMicromarketByUuid(entry.getKey()).getMicroMarketName());
				List<UserProfileDto> userProfileDtoList = new ArrayList<>();
				for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
					if (!Collections.disjoint(Arrays.asList(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split(",")), entry.getValue())) {
						UserProfileDto userProfileDto = userMap.get(userDepartmentLevelEntity.getUserUuid());
						if (Objects.nonNull(userProfileDto)) {
							filterUsers(requestDto, userProfileDtoList, userProfileDto, userUuid);
						}
					}
				}
				responseDto.setUserProfileDtoList(userProfileDtoList);
				if (CollectionUtils.isNotEmpty(responseDto.getUserProfileDtoList())) {
					responseDtoList.add(responseDto);
				}
			}
		}
	}

	private void getClusterManagers(UsersByAccessModulesAndCitiesRequestDto requestDto, List<String> userDepartmentLevelUuids,
									List<UsersByAccessModulesAndCitiesResponseDto> responseDtoList, String userUuid) {
		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelRepository
			.findByUuidInAndAccessLevel(userDepartmentLevelUuids, AccessLevel.MICROMARKET);
		if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
			return;
		}
		List<String> userUuids = new ArrayList<>();
		Map<String, UserProfileDto> userMap = new HashMap<>();
		if (Objects.nonNull(userDepartmentLevelEntityList) && !userDepartmentLevelEntityList.isEmpty()) {
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				userUuids.add(userDepartmentLevelEntity.getUserUuid());
			}
			if (Objects.nonNull(userUuids) && !userUuids.isEmpty()) {
				List<UserProfileDto> users = getAllActiveUserByUserUuid(userUuids);
				for (UserProfileDto up : users) {
					userMap.put(up.getUuid(), up);
				}
			}
		}
		List<String> micromarketUuids = new ArrayList<>();
		for (String cityUuid : requestDto.getCityUuids()) {
			log.info("micromarket uuids : {}", micromarketUuids);
			Optional.ofNullable(transformationCache.getMicromarketUuidsByCityUuid(cityUuid))
				.ifPresent(micromarketUuids::addAll);
		}
		if (CollectionUtils.isEmpty(micromarketUuids)) {
			return;
		}
		for (String micromarketUuid : micromarketUuids) {
			UsersByAccessModulesAndCitiesResponseDto responseDto = new UsersByAccessModulesAndCitiesResponseDto();
			responseDto.setAccessLevelEntityUuid(micromarketUuid);
			MicroMarketMetadataDto microMarketMetadataDto = transformationCache.getMicromarketByUuid(micromarketUuid);
			if (Objects.isNull(microMarketMetadataDto)) {
				continue;
			}
			responseDto.setAccessLevelEntityName(microMarketMetadataDto.getMicroMarketName());
			List<UserProfileDto> userProfileDtoList = new ArrayList<>();
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				if (Arrays.asList(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split(",")).contains(micromarketUuid)) {
					UserProfileDto userProfileDto = userMap.get(userDepartmentLevelEntity.getUserUuid());
					if (Objects.nonNull(userProfileDto)) {
						filterUsers(requestDto, userProfileDtoList, userProfileDto, userUuid);
					}
				}
			}
			responseDto.setUserProfileDtoList(userProfileDtoList);
			if (CollectionUtils.isNotEmpty(responseDto.getUserProfileDtoList())) {
				responseDtoList.add(responseDto);
			}
		}
	}

	private void filterUsers(UsersByAccessModulesAndCitiesRequestDto requestDto, List<UserProfileDto> userProfileDtoList,
							 UserProfileDto userProfileDto, String userUuid) {
		if (! userProfileDto.getUuid().equalsIgnoreCase(userUuid)) {
			if (StringUtils.isEmpty(requestDto.getSearchText())) {
				userProfileDtoList.add(userProfileDto);
			} else {
				String name = "";
				String firstName = StringUtils.isNotEmpty(userProfileDto.getFirstName()) ? userProfileDto.getFirstName() : "";
				String lastName = StringUtils.isNotEmpty(userProfileDto.getLastName()) ? userProfileDto.getLastName() : "";
				if (StringUtils.isNotEmpty(firstName) && StringUtils.isEmpty(lastName)) name = firstName;
				else if (StringUtils.isEmpty(firstName) && StringUtils.isNotEmpty(lastName)) name = lastName;
				else if (StringUtils.isNotEmpty(firstName) && StringUtils.isNotEmpty(lastName))
					name = firstName + " " + lastName;
				if (requestDto.getSearchText().length() >= 3 && (StringUtils.containsIgnoreCase(name, requestDto.getSearchText())
					|| StringUtils.containsIgnoreCase(userProfileDto.getMobile(), requestDto.getSearchText()))) {
					userProfileDtoList.add(userProfileDto);
				}
			}
		}
	}

	private void getCityHeads(UsersByAccessModulesAndCitiesRequestDto requestDto, List<String> userDepartmentLevelUuids,
							  List<UsersByAccessModulesAndCitiesResponseDto> responseDtoList, String userUuid) {
		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelRepository
			.findByUuidInAndAccessLevel(userDepartmentLevelUuids, AccessLevel.CITY);
		if (CollectionUtils.isEmpty(userDepartmentLevelEntityList)) {
			return;
		}
		List<String> userUuids = new ArrayList<>();
		Map<String, UserProfileDto> userMap = new HashMap<>();
		if (Objects.nonNull(userDepartmentLevelEntityList) && !userDepartmentLevelEntityList.isEmpty()) {
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				userUuids.add(userDepartmentLevelEntity.getUserUuid());
			}
			if (Objects.nonNull(userUuids) && !userUuids.isEmpty()) {
				List<UserProfileDto> users = getAllActiveUserByUserUuid(userUuids);
				for (UserProfileDto up : users) {
				   	userMap.put(up.getUuid(), up);
				}
			}
		}
		for (String cityUuid : requestDto.getCityUuids()) {
			UsersByAccessModulesAndCitiesResponseDto responseDto = new UsersByAccessModulesAndCitiesResponseDto();
			responseDto.setAccessLevelEntityUuid(cityUuid);
			CityMetadataDto cityMetadataDto = transformationCache.getCityByUuid(cityUuid);
			if (Objects.isNull(cityMetadataDto)) {
				continue;
			}
			responseDto.setAccessLevelEntityName(cityMetadataDto.getCityName());
			List<UserProfileDto> userProfileDtoList = new ArrayList<>();
			for (UserDepartmentLevelEntity userDepartmentLevelEntity : userDepartmentLevelEntityList) {
				if (Arrays.asList(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split(",")).contains(cityUuid)) {
					UserProfileDto userProfileDto = userMap.get(userDepartmentLevelEntity.getUserUuid());
					if (Objects.nonNull(userProfileDto)) {
						filterUsers(requestDto, userProfileDtoList, userProfileDto, userUuid);
					}
				}
			}
			responseDto.setUserProfileDtoList(userProfileDtoList);
			if (CollectionUtils.isNotEmpty(responseDto.getUserProfileDtoList())) {
				responseDtoList.add(responseDto);
			}
		}
	}

	private UserProfileDto getActiveUserByUserUuid(String userId) {

		log.info("Searching User by UserId: " + userId);

		UserEntity userEntity = userDbService.findByUuidAndStatus(userId, true);

		if (Objects.nonNull(userEntity)) {
			return UserAdapter.getUserProfileDto(userEntity);
		}

		return null;
	}

	@Override
	public List<UserDepartmentLevelAccessModulesDto> getUserDepartmentLevelAccessModules(String userUuid, Department department) {
		log.info("Get User Department Level Access Modules for user : {} and department : {}", userUuid, department);
		List<UserDepartmentLevelAccessModulesDto> userDepartmentLevelAccessModulesDtoList = new ArrayList<>();
		List<UserDepartmentLevelEntity> userDepartmentLevelEntityList = userDepartmentLevelRepository
			.findByUserUuidAndDepartmentAndStatus(userUuid, department, true);
		if (CollectionUtils.isNotEmpty(userDepartmentLevelEntityList)) {
			List<String> userDepartmentLevelUuids = userDepartmentLevelEntityList.stream().map(UserDepartmentLevelEntity::getUuid).collect(Collectors.toList());
			List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleRepository
				.findByUserDepartmentLevelUuidInAndStatus(userDepartmentLevelUuids, true);
			if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntityList)) {
				for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
					RoleAccessModuleMappingEntity roleAccessModuleMappingEntity = roleAccessModuleRepository
						.findByRoleUuidAndStatus(userDepartmentLevelRoleEntity.getRoleUuid(), true);
					if (Objects.nonNull(roleAccessModuleMappingEntity)) {
						UserDepartmentLevelAccessModulesDto accessModulesDto = new UserDepartmentLevelAccessModulesDto();
						accessModulesDto.setAccessModule(roleAccessModuleMappingEntity.getAccessModule());
						accessModulesDto.setAccessModuleName(roleAccessModuleMappingEntity.getAccessModule().getName());
						accessModulesDto.setRoleUuid(userDepartmentLevelRoleEntity.getRoleUuid());
						accessModulesDto.setUserDepartmentLevelUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());
						if (Arrays.asList(AccessModule.PG_LEAD_EDIT, AccessModule.APARTMENTS_LEAD_EDIT)
							.contains(roleAccessModuleMappingEntity.getAccessModule())) {
							accessModulesDto.setLeadTransferApplicable(true);
						}
						UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelRepository
							.findFirstByUuidAndStatus(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid(), true);
						if (Objects.nonNull(userDepartmentLevelEntity)) {
							accessModulesDto.setAccessLevel(userDepartmentLevelEntity.getAccessLevel());
							List<String> accessLevelEntityUuids = Arrays.asList(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid().split(","));
							Map<String, String> accessLevelEntityUuidNameMap = new HashMap<>();
							for (String entityUuid : accessLevelEntityUuids) {
								if (Objects.nonNull(transformationCache.getAccessLevelNameByUuid(entityUuid, userDepartmentLevelEntity.getAccessLevel().toString()))) {
									accessLevelEntityUuidNameMap.put(transformationCache.getAccessLevelNameByUuid(entityUuid,
										userDepartmentLevelEntity.getAccessLevel().toString()), entityUuid);
								}
							}
							accessModulesDto.setAccessLevelEntityUuidNameMap(accessLevelEntityUuidNameMap);
							userDepartmentLevelAccessModulesDtoList.add(accessModulesDto);
						}
					}
				}
			}
		}
		return userDepartmentLevelAccessModulesDtoList;
	}

	@Override
	@Transactional
	public void updateUserAccessModuleAccessLevel(UpdateAccessModuleAccessLevelRequestDto requestDto) {
		log.info("Update user access module access level {}", requestDto);
		UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelRepository
			.findFirstByUuidAndStatus(requestDto.getUserDepartmentLevelUuid(), true);
		if (Objects.nonNull(userDepartmentLevelEntity)) {
			try {
				if (userDepartmentLevelEntity.getAccessLevel() == requestDto.getAccessLevel()) {
					if (CollectionUtils.isNotEmpty(requestDto.getAccessLevelEntityUuids())) {
						userDepartmentLevelEntity.setCsvAccessLevelEntityUuid(String.join(",", requestDto.getAccessLevelEntityUuids()));
						userDepartmentLevelEntity.setUpdatedAt(new Date());
						userDepartmentLevelRepository.save(userDepartmentLevelEntity);
					} else {
						throw  new ApiValidationException("Access Level Entity Uuids can't be null or empty");
					}
				} else {

					RoleAccessModuleMappingEntity roleAccessModuleMappingEntity = roleAccessModuleRepository
							.findByAccessModuleAndAccessLevelAndStatus(requestDto.getAccessModule(), requestDto.getAccessLevel(),true);
					if (Objects.nonNull(roleAccessModuleMappingEntity)) {
						UserDeptLevelRoleListDto userDeptLevelRoleListDto = new UserDeptLevelRoleListDto();
						userDeptLevelRoleListDto.setUserUuid(requestDto.getUserUuid());
						userDeptLevelRoleListDto.setDepartment(Department.SALES);
						userDeptLevelRoleListDto.setAccessLevel(userDepartmentLevelEntity.getAccessLevel());
						userDeptLevelRoleListDto.setRolesUuid(Arrays.asList(requestDto.getRoleUuid()));
						revokeRolesForDepartmentOfLevel(userDeptLevelRoleListDto);

						AddUserDeptLevelRoleRequestDto addRoleaddUserDeptLevelRoleDto = new AddUserDeptLevelRoleRequestDto();
						addRoleaddUserDeptLevelRoleDto.setUserUuid(requestDto.getUserUuid());
						addRoleaddUserDeptLevelRoleDto.setDepartment(Department.SALES);
						addRoleaddUserDeptLevelRoleDto.setAccessLevel(requestDto.getAccessLevel());
						addRoleaddUserDeptLevelRoleDto.setAccessLevelEntityListUuid(requestDto.getAccessLevelEntityUuids());
						addRoleaddUserDeptLevelRoleDto.setRolesUuid(Arrays.asList(roleAccessModuleMappingEntity.getRoleUuid()));
						addRole(addRoleaddUserDeptLevelRoleDto);
					} else {
						throw new ApiValidationException("No Role Access Module found");
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		} else {
			throw  new ApiValidationException("No entry found in User Department Level entity for the request");
		}
	}

	@Override
	@Transactional
	public AddUserAndRoleDto addUserAndRole (AddUserAndRoleDto addUserAndRoleDto) {

		log.info("Add User and assign Role : {}", addUserAndRoleDto);
		if (StringUtils.isNotEmpty(addUserAndRoleDto.getFirstName()) && StringUtils.isNotEmpty(addUserAndRoleDto.getLastName())
			&& StringUtils.isNotEmpty(addUserAndRoleDto.getEmail()) && StringUtils.isNotEmpty(addUserAndRoleDto.getMobile())
			&& StringUtils.isNotEmpty(addUserAndRoleDto.getIsoCode())) {
			AddUserRequestDto addUserRequestDto = AddUserRequestDto.builder().userType(UserType.CITY_TEAM).department(Department.SALES)
				.isoCode(addUserAndRoleDto.getIsoCode()).email(addUserAndRoleDto.getEmail()).mobile(addUserAndRoleDto.getMobile())
				.firstName(addUserAndRoleDto.getFirstName()).lastName(addUserAndRoleDto.getLastName()).build();
			UserDto userDto = userService.addUserV3(addUserRequestDto);
			log.info("User Created with uuid : {}", userDto.getUuid());

			if (Objects.nonNull(addUserAndRoleDto.getAccessLevel()) & CollectionUtils.isNotEmpty(addUserAndRoleDto.getAccessLevelEntityListUuid())
				&& CollectionUtils.isNotEmpty(addUserAndRoleDto.getRolesUuid())) {

				AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = AddUserDeptLevelRoleRequestDto.builder().accessLevel(addUserAndRoleDto.getAccessLevel())
					.accessLevelEntityListUuid(addUserAndRoleDto.getAccessLevelEntityListUuid()).userUuid(userDto.getUuid()).department(Department.SALES)
					.rolesUuid(addUserAndRoleDto.getRolesUuid()).build();
				addRole(addUserDeptLevelRoleRequestDto);
			}
			addUserAndRoleDto.setUserUuid(userDto.getUuid());
			return addUserAndRoleDto;
		} else {
			throw new ApiValidationException("Bad Request - Required fields missing");
		}
	}

	@Override
	public List<MicromarketAndResidencesDropdownResponseDto> getMicromarketAndResidenceDropdown(MicromarketAndResidencesDropdownRequestDto requestDto) {
		log.info("Get micromarket and residence dropdown {}", requestDto);
		if (CollectionUtils.isNotEmpty(requestDto.getCityUuids())) {
			List<MicromarketAndResidencesDropdownResponseDto> responseDtos = new ArrayList<>();
			List<String> micromarketUuids = new ArrayList<>();
			for (String cityUuid : requestDto.getCityUuids()) {
				Optional.ofNullable(transformationCache.getMicromarketUuidsByCityUuid(cityUuid))
					.ifPresent(micromarketUuids::addAll);
				log.info("micromarket uuids : {}", micromarketUuids);
			}
			if (CollectionUtils.isNotEmpty(micromarketUuids)) {
				for (String micromarketUuid : micromarketUuids) {
					if (Objects.nonNull(transformationCache.getMicromarketByUuid(micromarketUuid))) {
						List<Map<String, String>> residenceNameUuidMapList = new ArrayList<>();
						MicromarketAndResidencesDropdownResponseDto responseDto = new MicromarketAndResidencesDropdownResponseDto();
						List<ResidenceMetadataDto> residences = new ArrayList<>();
						if (StringUtils.isEmpty(requestDto.getSearchText())) {
							residences = transformationCache.getResidencesByMicromarketUuid(micromarketUuid);
						}
						else if (requestDto.getSearchText().length() >= 3 && CollectionUtils.isNotEmpty(transformationCache.getResidencesByMicromarketUuid(micromarketUuid))) {
							residences = transformationCache.getResidencesByMicromarketUuid(micromarketUuid).stream()
								.filter(residenceMetadataDto -> residenceMetadataDto.getResidenceName().toLowerCase()
									.contains(requestDto.getSearchText().toLowerCase())).collect(Collectors.toList());
						}
						if (CollectionUtils.isNotEmpty(residences)) {
							responseDto.setMicromarketName(transformationCache.getMicromarketByUuid(micromarketUuid).getMicroMarketName());
							responseDto.setMicromarketUuid(micromarketUuid);
							for (ResidenceMetadataDto residenceMetadataDto : residences) {
								Map<String, String> residenceNameUuidMap = new HashMap<>();
								residenceNameUuidMap.put(residenceMetadataDto.getResidenceName(), residenceMetadataDto.getUuid());
								residenceNameUuidMapList.add(residenceNameUuidMap);
							}
							responseDto.setResidenceNameUuidMapList(residenceNameUuidMapList);
							responseDtos.add(responseDto);
						}
					}
				}
			}
			return responseDtos.stream()
				.sorted(Comparator.comparing(MicromarketAndResidencesDropdownResponseDto::getMicromarketName))
				.collect(Collectors.toList());
		} else {
			throw new ApiValidationException("Bad Request - Required fields missing");
		}
	}

	@Override
	public List<CityMicromarketDropdownResponseDto> getCityMicromarketDropdown(MicromarketAndResidencesDropdownRequestDto requestDto) {
		log.info("Get city and micromarket dropdown {}", requestDto);
		if (CollectionUtils.isNotEmpty(requestDto.getCityUuids())) {
			List<CityMicromarketDropdownResponseDto> responseDtos = new ArrayList<>();
			for (String cityUuid : requestDto.getCityUuids()) {
				List<MicroMarketMetadataDto> micromarkets = new ArrayList<>();
				if (StringUtils.isEmpty(requestDto.getSearchText())) {
					micromarkets = transformationCache.getMicromarketsByCityUuid(cityUuid);
				} else if (requestDto.getSearchText().length() >= 3 && CollectionUtils.isNotEmpty(transformationCache.getMicromarketsByCityUuid(cityUuid))) {
					micromarkets = transformationCache.getMicromarketsByCityUuid(cityUuid).stream().filter(microMarketMetadataDto ->
						microMarketMetadataDto.getMicroMarketName().toLowerCase().contains(requestDto.getSearchText().toLowerCase()))
						.collect(Collectors.toList());
				}
				if (CollectionUtils.isNotEmpty(micromarkets)) {
					List<Map<String, String>> micromarketNameUuidMapList = new ArrayList<>();
					CityMicromarketDropdownResponseDto responseDto = new CityMicromarketDropdownResponseDto();
					responseDto.setCityName(transformationCache.getCityByUuid(cityUuid).getCityName());
					responseDto.setCityUuid(cityUuid);
					for (MicroMarketMetadataDto microMarketMetadataDto : micromarkets) {
						Map<String, String> micromarketNameUuidMap = new HashMap<>();
						micromarketNameUuidMap.put(microMarketMetadataDto.getMicroMarketName(), microMarketMetadataDto.getUuid());
						micromarketNameUuidMapList.add(micromarketNameUuidMap);
					}
					responseDto.setMicromarketNameUuidMapList(micromarketNameUuidMapList);
					responseDtos.add(responseDto);
				}
			}
			return responseDtos.stream()
				.sorted(Comparator.comparing(CityMicromarketDropdownResponseDto::getCityName))
				.collect(Collectors.toList());
		} else {
			throw new ApiValidationException("Bad Request - Required fields missing");
		}
	}

	@Override
	public void addRoleV2(AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleDto) {

		userService.assertActiveUserByUserUuid(addUserDeptLevelRoleDto.getUserUuid());

		UserDepartmentLevelEntity userDepartmentLevelEntity =
			userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevelAndStatus(
				addUserDeptLevelRoleDto.getUserUuid(), addUserDeptLevelRoleDto.getDepartment(), addUserDeptLevelRoleDto.getAccessLevel(), true);

		if (Objects.nonNull(userDepartmentLevelEntity)) {
			List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntities = userDepartmentLevelRoleRepository
				.findByUserDepartmentLevelUuidAndRoleUuidInAndStatus(userDepartmentLevelEntity.getUuid(), addUserDeptLevelRoleDto.getRolesUuid(), true);
			if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntities)) {
				throw new ApiValidationException("User has already been assigned with the role you are trying to assign");
			}
		}

		AddUserDeptLevelRequestDto addUserDeptLevelRequestDto = new AddUserDeptLevelRequestDto(addUserDeptLevelRoleDto);

		userDepartmentLevelEntity = userDepartmentLevelService.add(addUserDeptLevelRequestDto);

		userDepartmentLevelRoleService.addRoles(userDepartmentLevelEntity.getUuid(), addUserDeptLevelRoleDto.getRolesUuid());
		publishCurrentRoleSnapshot(addUserDeptLevelRoleDto.getUserUuid());
	}

	private List<UserProfileDto> getAllActiveUserByUserUuid(List<String> userId) {

		log.info("Searching User by UserId: " + userId);
		List<UserProfileDto> userProfileDtoList = new ArrayList<>();
		List<UserEntity> userEntityList = userDbService.findAllByUuidInAndStatus(userId, true);

		if (Objects.nonNull(userEntityList) && !userEntityList.isEmpty()) {
			for (UserEntity userEntity : userEntityList) {
				userProfileDtoList.add(UserAdapter.getUserProfileDto(userEntity));
			}
		}
		return userProfileDtoList;
	}
}