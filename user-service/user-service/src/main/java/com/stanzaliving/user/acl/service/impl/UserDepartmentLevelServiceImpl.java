package com.stanzaliving.user.acl.service.impl;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.base.utils.StanzaUtils;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.adapters.UserDepartmentLevelAdapter;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.service.UserDepartmentLevelService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class UserDepartmentLevelServiceImpl implements UserDepartmentLevelService {

	@Autowired
	private UserDepartmentLevelDbService userDepartmentLevelDbService;

	@Autowired
	private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

	@Override
	public UserDepartmentLevelEntity add(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {

		UserDepartmentLevelEntity userDepartmentLevelEntity =
				userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevelAndStatus(
						addUserDeptLevelRequestDto.getUserUuid(), addUserDeptLevelRequestDto.getDepartment(), addUserDeptLevelRequestDto.getAccessLevel(), true);

		if (null != userDepartmentLevelEntity) {
			TreeSet<String> accessLevelEntityListUuid =
					StanzaUtils.getSplittedListOnComma(
							userDepartmentLevelEntity.getCsvAccessLevelEntityUuid()).stream().collect(Collectors.toCollection(TreeSet::new));

			accessLevelEntityListUuid.addAll(addUserDeptLevelRequestDto.getAccessLevelEntityListUuid());
			userDepartmentLevelEntity.setCsvAccessLevelEntityUuid(String.join(",", accessLevelEntityListUuid));

		} else {
			userDepartmentLevelEntity = UserDepartmentLevelAdapter.getEntityFromRequest(addUserDeptLevelRequestDto);
		}

		return userDepartmentLevelDbService.save(userDepartmentLevelEntity);
	}

	@Override
	public void delete(UserDepartmentLevelEntity userDepartmentLevelEntity) {
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleDbService.findByUserDepartmentLevelUuid(userDepartmentLevelEntity.getUuid());
		log.info("Deleting userDepartmentLevelRoleEntityList " + userDepartmentLevelRoleEntityList);
		userDepartmentLevelRoleDbService.delete(userDepartmentLevelRoleEntityList);

		log.info("Deleting userDepartmentLevelEntity " + userDepartmentLevelEntity);
		userDepartmentLevelDbService.delete(userDepartmentLevelEntity);
	}

	@Override
	public void revokeAccessLevelEntityForDepartmentOfLevel(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
		UserDepartmentLevelEntity userDepartmentLevelEntity =
				userDepartmentLevelDbService.findByUserUuidAndDepartmentAndAccessLevelAndStatus(
						addUserDeptLevelRequestDto.getUserUuid(), addUserDeptLevelRequestDto.getDepartment(), addUserDeptLevelRequestDto.getAccessLevel(), true);

		log.info("userDepartmentLevelEntity found for user " + addUserDeptLevelRequestDto.getUserUuid() + " is " + userDepartmentLevelEntity);

		if (userDepartmentLevelEntity != null && StringUtils.isBlank(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid())) {
			this.delete(userDepartmentLevelEntity);
			throw new StanzaException("user doesn't have access in department " + addUserDeptLevelRequestDto.getDepartment() + " at " + addUserDeptLevelRequestDto.getAccessLevel());
		}

		List<String> accessLevelEntityUuidList = StanzaUtils.getSplittedListOnComma(userDepartmentLevelEntity.getCsvAccessLevelEntityUuid());
		accessLevelEntityUuidList.removeAll(addUserDeptLevelRequestDto.getAccessLevelEntityListUuid());

		if (CollectionUtils.isEmpty(accessLevelEntityUuidList)) {
			this.delete(userDepartmentLevelEntity);
		} else {
			userDepartmentLevelEntity.setCsvAccessLevelEntityUuid(StringUtils.join(accessLevelEntityUuidList, ","));
			userDepartmentLevelDbService.save(userDepartmentLevelEntity);
		}

	}

	@Override
	public UserDepartmentLevelEntity findByUuid(String userDepartmentLevelUuid) {
		return userDepartmentLevelDbService.findByUuid(userDepartmentLevelUuid);
	}
}