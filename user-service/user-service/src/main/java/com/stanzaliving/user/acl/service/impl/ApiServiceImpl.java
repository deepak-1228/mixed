/**
 * 
 */
package com.stanzaliving.user.acl.service.impl;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.sqljpa.specification.utils.CriteriaOperation;
import com.stanzaliving.core.sqljpa.specification.utils.StanzaSpecificationBuilder;
import com.stanzaliving.core.user.acl.dto.ApiDto;
import com.stanzaliving.core.user.acl.request.dto.AddApiRequestDto;
import com.stanzaliving.core.user.acl.request.dto.UpdateApiRequestDto;
import com.stanzaliving.user.acl.adapters.ApiAdapter;
import com.stanzaliving.user.acl.constants.QueryConstants;
import com.stanzaliving.user.acl.db.service.ApiDbService;
import com.stanzaliving.user.acl.entity.ApiEntity;
import com.stanzaliving.user.acl.service.ApiService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
@Log4j2
@Service
public class ApiServiceImpl implements ApiService {

	@Autowired
	private ApiDbService apiDbService;

	@Override
	public ApiDto addApi(AddApiRequestDto addApiRequestDto) {

		if (apiDbService.existsByActionUrl(addApiRequestDto.getActionUrl())) {
			throw new StanzaException("API already exists with given URI");
		}

		if (apiDbService.existsByApiName(addApiRequestDto.getApiName())) {
			throw new StanzaException("API already exists with given Name");
		}

		log.info("Adding New API with URL: " + addApiRequestDto.getActionUrl() + " and name: " + addApiRequestDto.getApiName());

		ApiEntity apiEntity = ApiAdapter.getEntityFromRequest(addApiRequestDto);

		apiEntity = apiDbService.save(apiEntity);

		return ApiAdapter.getDto(apiEntity);
	}

	@Override
	public ApiDto updateApi(UpdateApiRequestDto updateApiRequestDto) {

		ApiEntity apiEntity = apiDbService.findByUuid(updateApiRequestDto.getApiUuid());

		if (Objects.isNull(apiEntity)) {
			throw new StanzaException("No API exists with Id: " + updateApiRequestDto.getApiUuid() + " to update");
		}

		if (!apiEntity.getActionUrl().equals(updateApiRequestDto.getActionUrl())
				&& apiDbService.existsByActionUrl(updateApiRequestDto.getActionUrl())) {
			throw new StanzaException("API already exists with given URL");
		}

		if (!apiEntity.getApiName().equals(updateApiRequestDto.getApiName())
				&& apiDbService.existsByApiName(updateApiRequestDto.getApiName())) {
			throw new StanzaException("API already exists with given Name");
		}

		log.info("Updating API: " + apiEntity.getUuid() + " With Details [Name: " + updateApiRequestDto.getApiName() + ", URL: " + updateApiRequestDto.getActionUrl() + ", Category: "
				+ updateApiRequestDto.getService() + "]");

		apiEntity.setApiName(updateApiRequestDto.getApiName());
		apiEntity.setActionUrl(updateApiRequestDto.getActionUrl());
		apiEntity.setService(updateApiRequestDto.getService());

		apiEntity = apiDbService.update(apiEntity);

		return ApiAdapter.getDto(apiEntity);
	}

	@Override
	public void deleteApi(String apiUuid) {

		ApiEntity apiEntity = apiDbService.findByUuid(apiUuid);

		if (Objects.isNull(apiEntity)) {
			throw new StanzaException("No API exists with Id: " + apiUuid + " to delete");
		}

		log.info("Deleting API [ID: " + apiUuid + ", Name: " + apiEntity.getApiName() + ", URL: " + apiEntity.getActionUrl() + "]");

		apiDbService.delete(apiEntity);

	}

	@Override
	public PageResponse<ApiDto> searchApi(String apiName, String apiUrl, String service, Boolean status, int pageNo, int limit) {

		Page<ApiEntity> apiPage = getApiPage(apiName, apiUrl, service, status, pageNo, limit);

		log.info("Found " + apiPage.getNumberOfElements() + " Api Records on Page: " + pageNo + " for Search Criteria");

		List<ApiDto> apiDtos = ApiAdapter.getDto(apiPage.getContent());

		return new PageResponse<>(pageNo, apiPage.getNumberOfElements(), apiPage.getTotalPages(), apiPage.getTotalElements(), apiDtos);

	}

	private Page<ApiEntity> getApiPage(String apiName, String apiUrl, String service, Boolean status, int pageNo, int limit) {

		Specification<ApiEntity> specification = getSearchQuery(apiName, apiUrl, service, status);

		Pageable pagination = getPaginationForSearchRequest(pageNo, limit);

		return apiDbService.findAll(specification, pagination);
	}

	private Specification<ApiEntity> getSearchQuery(String apiName, String apiUrl, String service, Boolean status) {

		StanzaSpecificationBuilder<ApiEntity> specificationBuilder = new StanzaSpecificationBuilder<>();

		if (StringUtils.isNotBlank(apiName)) {
			specificationBuilder.with(QueryConstants.Api.API_NAME, CriteriaOperation.EQ, apiName);
		}

		if (StringUtils.isNotBlank(apiUrl)) {
			specificationBuilder.with(QueryConstants.Api.API_URL, CriteriaOperation.EQ, apiUrl);
		}

		if (StringUtils.isNotBlank(service)) {
			specificationBuilder.with(QueryConstants.Api.SERVICE, CriteriaOperation.EQ, service);
		}

		if (status != null) {

			if (status) {
				specificationBuilder.with(QueryConstants.STATUS, CriteriaOperation.TRUE, true);
			} else {
				specificationBuilder.with(QueryConstants.STATUS, CriteriaOperation.FALSE, false);
			}
		}

		return specificationBuilder.build();
	}

	private Pageable getPaginationForSearchRequest(int pageNo, int limit) {

		Pageable pagination = PageRequest.of(0, 10, Direction.DESC, "createdAt");

		if (pageNo > 0 && limit > 0 && limit < 1000) {
			pagination = PageRequest.of(pageNo - 1, limit, Direction.DESC, "createdAt");
		}

		return pagination;
	}
}