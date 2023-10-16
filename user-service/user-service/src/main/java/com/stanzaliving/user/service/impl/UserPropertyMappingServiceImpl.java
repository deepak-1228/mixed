/**
 * 
 */
package com.stanzaliving.user.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.core.base.exception.MappingNotFoundException;
import com.stanzaliving.core.sqljpa.specification.utils.CriteriaOperation;
import com.stanzaliving.core.sqljpa.specification.utils.StanzaSpecificationBuilder;
import com.stanzaliving.core.user.dto.UserPropertyMappingDto;
import com.stanzaliving.core.user.request.dto.UserPropertyMappingRequestDto;
import com.stanzaliving.user.adapters.UserPropertyMappingAdapter;
import com.stanzaliving.user.db.service.UserPropertyMappingDbService;
import com.stanzaliving.user.entity.UserPropertyMappingEntity;
import com.stanzaliving.user.service.UserPropertyMappingService;

import lombok.extern.log4j.Log4j2;

/**
 * @author naveen
 *
 * @date 13-Oct-2019
 */
@Log4j2
@Service
public class UserPropertyMappingServiceImpl implements UserPropertyMappingService {

	@Autowired
	private UserPropertyMappingDbService userPropertyMappingDbService;

	@Override
	public void createUserPropertyMapping(List<UserPropertyMappingRequestDto> mappingRequestDtos) {

		log.info("Received Request to create " + mappingRequestDtos.size() + " User-Property Mappings");

		List<UserPropertyMappingEntity> mappingEntities = new ArrayList<>();

		for (UserPropertyMappingRequestDto requestDto : mappingRequestDtos) {

			if (StringUtils.isBlank(requestDto.getUserId()) || StringUtils.isBlank(requestDto.getPropertyId())) {
				throw new ApiValidationException("UserId and PropertyId must not be empty to create mapping");
			}

			if (userPropertyMappingDbService.isUserPropertyMappingExists(requestDto.getUserId(), requestDto.getPropertyId())) {
				throw new ApiValidationException("User [" + requestDto.getUserId() + "] is already mapped to Property [" + requestDto.getPropertyId() + "]");
			}

			mappingEntities.add(UserPropertyMappingAdapter.getMappingEntityFromRequest(requestDto));
		}

		log.info("Saving " + mappingEntities.size() + " User-Property Mappings");

		userPropertyMappingDbService.save(mappingEntities);

		log.info(mappingEntities.size() + " User-Property Mappings Created");
	}

	@Override
	public void deleteMapping(String mappingId) {

		UserPropertyMappingEntity mappingEntity = userPropertyMappingDbService.findByUuid(mappingId);

		if (Objects.isNull(mappingEntity)) {
			throw new MappingNotFoundException("No User Property Mapping found with given id");
		}

		log.info("Deleting User-Property Mapping: " + mappingEntity);

		userPropertyMappingDbService.delete(mappingEntity);

	}

	@Override
	public List<UserPropertyMappingDto> getUserPropertyMappings(String userId) {

		log.info("Searching Mapped properties to User: " + userId);

		List<UserPropertyMappingEntity> mappingEntities = userPropertyMappingDbService.getActiveUserPropertyMappings(userId);

		if (CollectionUtils.isEmpty(mappingEntities)) {
			log.error("User [" + userId + "] is not mapped to any property");
			throw new MappingNotFoundException("User is not mapped to any property");
		}

		log.info("User: " + userId + " is mapped to  " + mappingEntities.size() + " Properties");

		return UserPropertyMappingAdapter.getMappingDtos(mappingEntities);
	}

	@Override
	public PageResponse<UserPropertyMappingDto> searchUserPropertyMappings(String userId, String propertyId, int pageNo, int limit) {

		Page<UserPropertyMappingEntity> mappingPage = getMappingPage(userId, propertyId, pageNo, limit);

		log.info("Found " + mappingPage.getNumberOfElements() + " Mapping Records on Page: " + pageNo + " for Search Criteria");

		List<UserPropertyMappingDto> mappingDtos =
				mappingPage.getContent().stream().map(UserPropertyMappingAdapter::getMappingDto).collect(Collectors.toList());

		return new PageResponse<>(pageNo, mappingPage.getNumberOfElements(), mappingPage.getTotalPages(), mappingPage.getTotalElements(), mappingDtos);
	}

	private Page<UserPropertyMappingEntity> getMappingPage(String userId, String propertyId, int pageNo, int limit) {

		Specification<UserPropertyMappingEntity> specification = getSearchQuery(userId, propertyId);

		Pageable pagination = getPaginationForSearchRequest(pageNo, limit);

		return userPropertyMappingDbService.findAll(specification, pagination);
	}

	private Specification<UserPropertyMappingEntity> getSearchQuery(String userId, String propertyId) {

		StanzaSpecificationBuilder<UserPropertyMappingEntity> specificationBuilder = new StanzaSpecificationBuilder<>();

		if (StringUtils.isNotBlank(userId)) {
			specificationBuilder.with("userId", CriteriaOperation.EQ, userId);
		}

		if (StringUtils.isNotBlank(propertyId)) {
			specificationBuilder.with("propertyId", CriteriaOperation.EQ, propertyId);
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