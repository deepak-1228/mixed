package com.stanzaliving.user.acl.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.user.acl.dto.RoleAccessDto;
import com.stanzaliving.core.user.enums.EnumListing;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class RoleAccessAdapter {

	public static RoleAccessDto getDto(RoleAccessEntity roleAccessEntity) {

		return RoleAccessDto.builder()
				.uuid(roleAccessEntity.getUuid())
				.createdAt(roleAccessEntity.getCreatedAt())
				.createdBy(roleAccessEntity.getCreatedBy())
				.updatedAt(roleAccessEntity.getUpdatedAt())
				.updatedBy(roleAccessEntity.getUpdatedBy())
				.status(roleAccessEntity.isStatus())
				.roleUuid(roleAccessEntity.getRoleUuid())
				.accessUuid(roleAccessEntity.getAccessUuid())
				.roleAccessType(roleAccessEntity.getRoleAccessType())
				.build();

	}

	public List<EnumListing<AccessLevel>> getAccessLevelEnumAsEnumListing() {
		List<EnumListing<AccessLevel>> data = new ArrayList<>();

		for (AccessLevel accessLevel : AccessLevel.values()) {
			data.add(EnumListing.of(accessLevel, String.valueOf(accessLevel.getLevelNum())));
		}
		
		return data;
	}

	public List<EnumListing<AccessLevel>> getAccessLevelEnumAsEnumListingV3() {
		List<EnumListing<AccessLevel>> data = new ArrayList<>();

		for (AccessLevel accessLevel : AccessLevel.values()) {
			if (Arrays.asList(AccessLevel.CITY, AccessLevel.MICROMARKET, AccessLevel.RESIDENCE).contains(accessLevel)) {
				data.add(EnumListing.of(accessLevel, String.valueOf(accessLevel.getLevelNum())));
			}
		}

		return data;
	}

}