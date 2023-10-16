/**
 * 
 */
package com.stanzaliving.user.acl.adapters;

import com.stanzaliving.core.user.acl.dto.ApiDto;
import com.stanzaliving.core.user.acl.request.dto.AddApiRequestDto;
import com.stanzaliving.user.acl.entity.ApiEntity;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections.CollectionUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
@UtilityClass
public class ApiAdapter {

	public static ApiEntity getEntityFromRequest(AddApiRequestDto addApiRequestDto) {

		try {
			URL url = new URL(addApiRequestDto.getActionUrl());
			addApiRequestDto.setActionUrl(url.getPath());
		} catch (Exception ex) {
		}

		return ApiEntity.builder()
				.apiName(addApiRequestDto.getApiName())
				.actionUrl(addApiRequestDto.getActionUrl())
				.service(addApiRequestDto.getService())
				.build();
	}

	public static List<ApiDto> getDto(List<ApiEntity> apiEntities) {

		if (CollectionUtils.isEmpty(apiEntities)) {
			return new ArrayList<>();
		}

		return apiEntities.stream().map(ApiAdapter::getDto).collect(Collectors.toList());
	}

	public static ApiDto getDto(ApiEntity apiEntity) {

		return ApiDto.builder()
				.uuid(apiEntity.getUuid())
				.createdAt(apiEntity.getCreatedAt())
				.createdBy(apiEntity.getCreatedBy())
				.updatedAt(apiEntity.getUpdatedAt())
				.updatedBy(apiEntity.getUpdatedBy())
				.status(apiEntity.isStatus())
				.apiName(apiEntity.getApiName())
				.actionUrl(apiEntity.getActionUrl())
				.service(apiEntity.getService())
				.build();
	}
}