package com.stanzaliving.user.acl.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.stanzaliving.estate_v2.dto.KeyValueDto;
import com.stanzaliving.user.feignclient.UserV2FeignService;
import com.stanzaliving.user.feignclient.Userv2HttpService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.core.user.acl.request.dto.AddRoleAccessDto;
import com.stanzaliving.core.user.acl.request.dto.AddRoleRequestDto;
import com.stanzaliving.user.acl.adapters.RoleAdapter;
import com.stanzaliving.user.acl.db.service.RoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.service.RoleAccessService;
import com.stanzaliving.user.acl.service.RoleService;
import com.stanzaliving.user.kafka.service.KafkaUserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleDbService roleDbService;

	@Autowired
	private RoleAccessService roleAccessService;

	@Autowired
	private KafkaUserService kafkaUserService;

	@Autowired
	private UserV2FeignService userV2FeignService;

	private static String PARENT_UUID_TO_SKIP_PARENT_ROLE = "SELF";

	@Override
	public RoleDto addRole(AddRoleRequestDto addRoleRequestDto) {
		if (roleDbService.isRoleExists(addRoleRequestDto.getRoleName(), addRoleRequestDto.getDepartment())) {
			throw new ApiValidationException("Role already exists with given name " + addRoleRequestDto.getRoleName());
		}

		RoleEntity parentRoleEntity = roleDbService.findByUuid(addRoleRequestDto.getParentRoleUuid());
		if (!PARENT_UUID_TO_SKIP_PARENT_ROLE.equalsIgnoreCase(addRoleRequestDto.getParentRoleUuid()) && null == parentRoleEntity) {
			throw new ApiValidationException("Parent role doesn't exist for parentUuid " + addRoleRequestDto.getParentRoleUuid());
		}

		RoleEntity roleEntity = RoleAdapter.getEntityFromRequest(addRoleRequestDto);

		if (!PARENT_UUID_TO_SKIP_PARENT_ROLE.equalsIgnoreCase(addRoleRequestDto.getParentRoleUuid())) {
			roleAccessService.assertSameDepartmentAssignment(parentRoleEntity, roleEntity);
			roleAccessService.assertParentChildAssignment(parentRoleEntity, roleEntity);
		}

		roleDbService.save(roleEntity);

		if (!PARENT_UUID_TO_SKIP_PARENT_ROLE.equalsIgnoreCase(addRoleRequestDto.getParentRoleUuid())) {
			roleAccessService.addRoleAccess(
					AddRoleAccessDto.builder()
							.roleUuid(addRoleRequestDto.getParentRoleUuid())
							.accessUuid(roleEntity.getUuid())
							.roleAccessType(RoleAccessType.ROLE)
							.build());
		}
		RoleDto roleDto = RoleAdapter.getDto(roleEntity);
		kafkaUserService.sendNewRoleToKafka(roleDto);
		return roleDto;
	}

	@Override
	public RoleDto getRoleByUuid(String roleUuid) {

//		RoleDto roleDto= userV2FeignService.getRoleByUuid(roleUuid);
//
//		if(Objects.nonNull(roleDto)){
//			return roleDto;
//		}

		RoleEntity roleEntity = roleDbService.findByUuid(roleUuid);

		if (null == roleEntity) {
			throw new ApiValidationException("Unable to find rule by uuid " + roleUuid);
		}

		return RoleAdapter.getDto(roleEntity);
	}

	@Override
	public List<RoleDto> getRoleByUuidIn(List<String> roleUuidList) {
		List<RoleEntity> roleEntityList = roleDbService.findByUuidIn(roleUuidList);
		return RoleAdapter.getDtoList(roleEntityList);
	}

	@Override
	public List<RoleDto> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel) {
		List<RoleEntity> roleEntityList = roleDbService.findByDepartmentAndAccessLevel(department, accessLevel);
		return RoleAdapter.getDtoList(roleEntityList);
	}

	@Override
	public List<RoleDto> filter(String roleName, Department department, AccessLevel accessLevel) {
		return filter(
				RoleDto.builder()
						.roleName(roleName)
						.accessLevel(accessLevel)
						.department(department)
						.build());
	}

	@Override
	public List<RoleDto> filter(RoleDto roleDto) {

//		List<RoleDto> roleDtos= userV2FeignService.findFilteredRoles(roleDto);
//
//		roleDto.setMigrated(false);
		List<RoleDto> roleDtos=new ArrayList<>();
		List<RoleEntity> roleEntities = roleDbService.filter(roleDto);
		if(roleEntities.size()>0) {
			roleDtos.addAll(RoleAdapter.getDtoList(roleEntities));
		}
		return roleDtos;
	}

	@Override
	public RoleDto findByRoleName(String roleName) {

		log.info("Searching role by name: {}", roleName);

		RoleEntity roleEntity = roleDbService.findByRoleName(roleName);

		if (null == roleEntity) {
			throw new ApiValidationException("Unable to find role by roleName " + roleName);
		}

		return RoleAdapter.getDto(roleEntity);
	}

	@Override
	public RoleDto findByRoleNameAndDepartment(String roleName, Department department) {

		log.info("Searching role by name: {}", roleName);

		RoleEntity roleEntity = roleDbService.findByRoleNameAndDepartment(roleName, department);

		if (null == roleEntity) {
			throw new ApiValidationException("Unable to find rule by roleName " + roleName);
		}

		return RoleAdapter.getDto(roleEntity);
	}

	@Override
	public List<RoleDto> findByRoleNameInAndDepartment(List<String> roleNames, Department department) {

		log.info("Searching roles by name: {}", roleNames);

//		List<RoleDto> roleV2Dtos=userV2FeignService.findByRoleNameInAndDepartment(roleNames,department);


		List<RoleEntity> roleEntity = roleDbService.findByRoleNameAndDepartment(roleNames, department);

		if (null == roleEntity) {
			throw new ApiValidationException("Unable to find role by roleNames ");
		}

		List<RoleDto> roleDtos= roleEntity.stream().map(f->RoleAdapter.getDto(f)).collect(Collectors.toList());
//		if(roleV2Dtos.size()>0){
//			roleDtos.addAll(roleV2Dtos);
//		}
		return roleDtos;
	}

	@Override
	public List<KeyValueDto> getAllViewOnlyRoles() {
		List<RoleEntity> roleEntities = new ArrayList<>();
		roleEntities.addAll(roleDbService.findByDepartmentAndRoleNameEndsWithIgnoreCase(Department.BUSINESS_DEVELOPMENT, "_VIEW"));
		roleEntities.addAll(roleDbService.findByDepartmentAndRoleNameEndsWithIgnoreCase(Department.LEADERSHIP,"_VIEW"));

		List<KeyValueDto> roles = new ArrayList<>();
		if (!ObjectUtils.isEmpty(roleEntities)) {
			roleEntities.stream().forEach(roleEntity ->
					roles.add(KeyValueDto.builder().label(roleEntity.getRoleName()).value(roleEntity.getUuid()).build()));
		}
		else
			throw new ApiValidationException("Unable to find review template view only roles");

	return roles;
	}
}