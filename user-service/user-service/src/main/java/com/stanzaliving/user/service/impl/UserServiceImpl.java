/**
 *
 */
package com.stanzaliving.user.service.impl;

import com.stanzaliving.core.base.common.dto.PageResponse;
import com.stanzaliving.core.base.common.dto.PaginationRequest;
import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.base.exception.ApiValidationException;
import com.stanzaliving.core.base.exception.NoRecordException;
import com.stanzaliving.core.base.exception.StanzaException;
import com.stanzaliving.core.base.exception.UserValidationException;
import com.stanzaliving.core.base.utils.PhoneNumberUtils;
import com.stanzaliving.core.generic.dto.UIKeyValue;
import com.stanzaliving.core.kafka.dto.KafkaDTO;
import com.stanzaliving.core.kafka.producer.NotificationProducer;
import com.stanzaliving.core.sqljpa.adapter.AddressAdapter;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.core.user.acl.dto.UserDeptLevelRoleDto;
import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.AccessLevelRoleRequestDto;
import com.stanzaliving.core.user.dto.UserDto;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.dto.UserManagerAndRoleDto;
import com.stanzaliving.core.user.dto.UserProfileDto;
import com.stanzaliving.core.user.dto.UserRoleCacheDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.core.user.request.dto.ActiveUserRequestDto;
import com.stanzaliving.core.user.request.dto.AddUserAndRoleRequestDto;
import com.stanzaliving.core.user.request.dto.AddUserRequestDto;
import com.stanzaliving.core.user.request.dto.UpdateDepartmentUserTypeDto;
import com.stanzaliving.core.user.request.dto.UpdateUserRequestDto;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelRoleDbService;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import com.stanzaliving.user.acl.repository.RoleRepository;
import com.stanzaliving.user.acl.repository.UserDepartmentLevelRepository;
import com.stanzaliving.user.acl.service.AclUserService;
import com.stanzaliving.user.acl.service.RoleService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelRoleService;
import com.stanzaliving.user.acl.service.UserDepartmentLevelService;
import com.stanzaliving.user.adapters.UserAdapter;
import com.stanzaliving.user.adapters.Userv2ToUserAdapter;
import com.stanzaliving.user.constants.UserConstants;
import com.stanzaliving.user.db.service.UserDbService;
import com.stanzaliving.user.dto.userv2.UpdateUserDto;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserProfileEntity;
import com.stanzaliving.user.feignclient.UserV2FeignService;
import com.stanzaliving.user.feignclient.Userv2HttpService;
import com.stanzaliving.user.service.UserManagerMappingService;
import com.stanzaliving.user.service.UserService;
import io.reactivex.internal.functions.ObjectHelper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Log4j2
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserV2FeignService userV2FeignService;

	@Autowired
	private UserDbService userDbService;

	@Autowired
	private UserDepartmentLevelRoleDbService userDepartmentLevelRoleDbService;

	@Autowired
	private UserDepartmentLevelDbService userDepartmentLevelDbService;

	@Autowired
	private UserManagerMappingService userManagerMappingService;

	@Autowired
	private AclUserService aclUserService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private UserDepartmentLevelRoleService userDepartmentLevelRoleService;

	@Autowired
	private UserDepartmentLevelService userDepartmentLevelService;

	@Autowired
	private NotificationProducer notificationProducer;
	
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UserDepartmentLevelRepository userDepartmentLevelRepository;

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private Validator validator;

	@Value("${kafka.resident.detail.topic}")
	private String kafkaResidentDetailTopic;

	@Value("${consumer.role}")
	private String consumerUuid;

	@Value("${broker.role}")
	private String brokerUuid;

	@Value("${country.uuid}")
	private String countryUuid;

	@Value("${sigmaManageSales.role}")
	private String sigmaManageSalesUuid;

	@Override
	public UserProfileDto getActiveUserByUserId(String userId) {

		log.info("Searching User by UserId: " + userId);

		UserEntity userEntity=null;
		userEntity = userDbService.findByUuidAndStatus(userId, true);
		if(Objects.isNull(userEntity)){
			com.stanzaliving.user.dto.userv2.UserDto user=userV2FeignService.getActiveUserByUuid(userId);
			if(Objects.nonNull(user)) {
				userEntity = Userv2ToUserAdapter.getUserEntityFromUserv2(user);
			}
		}

		if (Objects.isNull(userEntity)) {
			throw new UserValidationException("User not found for UserId: " + userId);
		}

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public UserProfileDto getUserByUserId(String userId) {
        log.info("Searching User by UserId: " + userId);


		com.stanzaliving.user.dto.userv2.UserDto userDto=userV2FeignService.getUserByUuid(userId);
		if(Objects.nonNull(userDto)) {
			UserEntity userEntity=Userv2ToUserAdapter.getUserEntityFromUserv2(userDto);
			return UserAdapter.getUserProfileDto(userEntity);
		}
		else{
			UserEntity userEntity = userDbService.findByUuidNotMigrated(userId,false);
			return UserAdapter.getUserProfileDto(userEntity);
		}
	}

	@Override
	public UserDto getActiveUserByUuid(String userUuid) {

		//UserEntity userEntity = userDbService.findByUuid(userUuid);
		com.stanzaliving.user.dto.userv2.UserDto userv2=userV2FeignService.getUserByUuid(userUuid);

		UserEntity userEntity=null;
		if(Objects.nonNull(userv2)) {
			userEntity = Userv2ToUserAdapter.getUserEntityFromUserv2(userv2);
		}

		if(Objects.isNull(userEntity)){
			userEntity = userDbService.findByUuidNotMigrated(userUuid,false);
		}

		if (Objects.isNull(userEntity)) {
			log.error("User Not Found with Uuid: {}", userUuid);
			throw new UserValidationException("User Not Found with Uuid: " + userUuid);
		}

		if (!userEntity.isStatus()) {
			log.error("User Account is Disabled for Uuid : {}", userUuid);
			throw new UserValidationException("User Account is Disabled for Uuid " + userUuid);
		}

		log.info("Found User: " + userEntity.getUuid() + " of Type: " + userEntity.getUserType());

		return UserAdapter.getUserDto(userEntity);
	}

	@Override
	public void assertActiveUserByUserUuid(String userUuid) {
		this.getActiveUserByUserId(userUuid);
	}

	@Override
	public UserDto addUser(AddUserRequestDto addUserRequestDto) {

		if(UserType.getMigratedUserTypes().contains(addUserRequestDto.getUserType())){
			throw new StanzaException("Users of this user type are migrated to acl2.0,and should be created from there.");
		}

		if (!PhoneNumberUtils.isValidMobileForCountry(addUserRequestDto.getMobile(), addUserRequestDto.getIsoCode())) {
			log.error("Number: " + addUserRequestDto.getMobile() + " and ISO: " + addUserRequestDto.getIsoCode()
					+ " doesn't appear to be a valid mobile combination");
			throw new ApiValidationException("Mobile Number and ISO Code combination not valid");
		}

		UserEntity userEntity = userDbService.getUserForMobile(addUserRequestDto.getMobile(),
				addUserRequestDto.getIsoCode());

		if (Objects.nonNull(userEntity)) {

			if (!userEntity.isStatus()) {
				userEntity.setStatus(true);
				userDbService.update(userEntity);
			}

			log.warn("User: " + userEntity.getUuid() + " already exists for Mobile: " + addUserRequestDto.getMobile()
					+ ", ISO Code: " + addUserRequestDto.getIsoCode() + " of type: " + addUserRequestDto.getUserType());

			if (addUserRequestDto.getUserType().equals(UserType.CONSUMER) || addUserRequestDto.getUserType().equals(UserType.EXTERNAL) || addUserRequestDto.getUserType().equals(UserType.VENDOR)) {
				userEntity.setUserType(addUserRequestDto.getUserType());
				userEntity.setDepartment(addUserRequestDto.getDepartment());
				try {
					if (addUserRequestDto.getUserType().equals(UserType.CONSUMER) || addUserRequestDto.getUserType().equals(UserType.EXTERNAL))
						addUserOrConsumerRole(userEntity);
					else if (addUserRequestDto.getUserType().equals(UserType.VENDOR))
						addUserOrConsumerRoleByRoleNames(userEntity, addUserRequestDto.getRoleNames());
				} catch (Exception e) {
					log.error("Got error while adding role", e);
				}
				userDbService.update(userEntity);
			} else if (addUserRequestDto.getUserType().equals(UserType.FOOD_DELIVERY_AGENT)) {
				List<UserDeptLevelRoleDto> userDeptLevelRoleList = aclUserService.getActiveUserDeptLevelRole(userEntity.getUuid());

				UserDeptLevelRoleDto foodOpsRole = userDeptLevelRoleList.stream()
						.filter(userDeptLevelRole ->  userDeptLevelRole.getDepartment().equals(Department.FOOD_OPS))
						.findFirst().orElse(null);
				if(Objects.isNull(foodOpsRole)) {
					log.error("User: {} does not belong FOOD_OPS", userEntity.getUuid());
					return UserAdapter.getUserDto(userEntity);
				}

				List<RoleEntity> roleEntities = roleRepository.findByRoleNameInAndDepartment(addUserRequestDto.getRoleNames(), Department.FOOD_OPS);
				if(CollectionUtils.isNotEmpty(roleEntities)) {
					Set<String> roleUuids = roleEntities.stream().map(RoleEntity::getUuid).collect(Collectors.toSet());
					for(String roleUuid : roleUuids) {
						if(!foodOpsRole.getRolesUuid().contains(roleUuid)) {
							addUserOrConsumerRoleByRoleNames(userEntity, addUserRequestDto.getRoleNames());
							return UserAdapter.getUserDto(userEntity);
						}
					}
				}
			}

			return UserAdapter.getUserDto(userEntity);
		}

		log.info("Adding new User [Mobile: " + addUserRequestDto.getMobile() + ", ISOCode: "
				+ addUserRequestDto.getIsoCode() + ", UserType: " + addUserRequestDto.getUserType() + "]");

		UserProfileEntity profileEntity = UserAdapter.getUserProfileEntity(addUserRequestDto);

		userEntity = UserEntity.builder().userType(addUserRequestDto.getUserType())
				.isoCode(addUserRequestDto.getIsoCode()).mobile(addUserRequestDto.getMobile()).mobileVerified(false)
				.email(addUserRequestDto.getEmail()).emailVerified(false).userProfile(profileEntity).status(true)
				.department(addUserRequestDto.getDepartment()).build();

		profileEntity.setUser(userEntity);

		userEntity = userDbService.saveAndFlush(userEntity);

		addUserOrConsumerRole(userEntity);
		addUserOrConsumerRoleByRoleNames(userEntity, addUserRequestDto.getRoleNames());

		log.info("Added New User with Id: " + userEntity.getUuid());

		UserDto userDto = UserAdapter.getUserDto(userEntity);

		KafkaDTO kafkaDTO = new KafkaDTO();
		kafkaDTO.setData(userDto);

		notificationProducer.publish(kafkaResidentDetailTopic, KafkaDTO.class.getName(), kafkaDTO);

		return userDto;
	}

	public List<UserDto> addBulkUserAndRole(List<AddUserAndRoleRequestDto> addUserAndRoleRequestDtoList) {

		// Adding validation at service layer
		validateConstraint(addUserAndRoleRequestDtoList);

		Map<String, ArrayList<AddUserAndRoleRequestDto>> userAndRoleRequestDtoListMapByMobile = createUserAndRoleRequestDtoListByMobile(addUserAndRoleRequestDtoList);

		List<UserEntity> newUserEntityList = new ArrayList<>();

		List<UserDto> existingUserDtoList = new ArrayList<>();

		userAndRoleRequestDtoListMapByMobile.forEach((key, userAndRoleRequestDtoListByMobile) -> {

			UserEntity userEntity = checkExistingUser(userAndRoleRequestDtoListByMobile);

			if(Objects.isNull(userEntity)) {
				userEntity = createNewUser(userAndRoleRequestDtoListByMobile.get(0));
				newUserEntityList.add(userEntity);
			} else {
				existingUserDtoList.add(UserAdapter.getUserDto(userEntity));
			}
		});

		if(newUserEntityList.isEmpty()) return existingUserDtoList;

		List<UserEntity> newUserEntityCreatedList = userDbService.saveAndFlush(newUserEntityList);

		List<UserDto> userDtoList = new ArrayList<>();

		newUserEntityCreatedList.forEach(newUserEntityCreated -> {
			userDtoList.add(UserAdapter.getUserDto(newUserEntityCreated));
			log.info("Added New User with Id: " + newUserEntityCreated.getUuid());
		});

		log.info("Assigning roles to each created user is started");
		assignRoleToAllUser(userAndRoleRequestDtoListMapByMobile, newUserEntityCreatedList);
		log.info("Assigning roles to each created user is completed");

		publishToKafka(userDtoList);

		userDtoList.addAll(existingUserDtoList);
		return userDtoList;
	}

	@Override
	public List<UserProfileDto> getUserProfileList(List<String> userUuidList) {

		log.info("Searching users in list: {}",userUuidList);

		List<UserEntity> userEntityList = userDbService.findAllByUuidInAndStatus(userUuidList, true);
		List<com.stanzaliving.user.dto.userv2.UserDto> userDtos=userV2FeignService.getUsersList(userUuidList);
		if(userDtos.size()>0){
			for(com.stanzaliving.user.dto.userv2.UserDto userDto:userDtos){
				userEntityList.add(Userv2ToUserAdapter.getUserEntityFromUserv2(userDto));
			}
		}
		if (CollectionUtils.isEmpty(userEntityList)) {
			throw new ApiValidationException("Users not found for UserId List: " + userUuidList);
		}
		List<UserProfileDto> userProfileDtoList = new ArrayList<>();
		for (UserEntity userEntity : userEntityList) {
			userProfileDtoList.add(UserAdapter.getUserProfileDto(userEntity));
		}
		return userProfileDtoList;
	}


	@Override
	public UserProfileDto getUserProfile(String userId) {

		UserEntity userEntity=null;
		userEntity=userDbService.findByUuidNotMigrated(userId,false);
		if(Objects.isNull(userEntity)) {
			com.stanzaliving.user.dto.userv2.UserDto user = userV2FeignService.getUserByUuid(userId);
			if (Objects.nonNull(user)) {
				userEntity = Userv2ToUserAdapter.getUserEntityFromUserv2(user);
			}
		}

		if (Objects.isNull(userEntity)) {
			throw new UserValidationException("User not found for UserId: " + userId);
		}

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public Map<String, UserProfileDto> getUserProfileIn(Map<String, String> userManagerUuidMap) {

		Map<String, UserProfileDto> managerProfileDtoMap = new HashMap<>();

		List<String> managerUuids = new ArrayList<>();

		// Extract managerIds
		userManagerUuidMap.forEach((k, v) -> {
			managerUuids.add(v);
		});

		List<UserEntity> userEntities = userDbService.findByUuidIn(managerUuids);

		if (Objects.isNull(userEntities)) {
			throw new ApiValidationException("User not found for Uuids: " + managerUuids);
		}

		userEntities.forEach(userEntity -> {
			managerProfileDtoMap.put(userEntity.getUuid(), UserAdapter.getUserProfileDto(userEntity));
		});

		Map<String, UserProfileDto> userManagerProfileMapping = new HashMap<>();

		userManagerUuidMap.forEach((k, v) -> {
			userManagerProfileMapping.put(k, managerProfileDtoMap.get(v));
		});

		return userManagerProfileMapping;
	}

	@Override
	public PageResponse<UserProfileDto> searchUser(UserFilterDto userFilterDto) {

		PageResponse<com.stanzaliving.user.dto.userv2.UserDto> userv2DtoPageResponse=userV2FeignService.searchOrFilterUsers(userFilterDto);

		List<com.stanzaliving.user.dto.userv2.UserDto> userV2Dtos= userv2DtoPageResponse.getData();
		List<UserProfileDto> userProfileDtos=new ArrayList<>();

		if(Objects.nonNull(userV2Dtos) && userV2Dtos.size()>0) {
			for (com.stanzaliving.user.dto.userv2.UserDto userDto : userV2Dtos) {
				userProfileDtos.add(UserAdapter.getUserProfileDto(Userv2ToUserAdapter.getUserEntityFromUserv2(userDto)));
			}
		}

		userFilterDto.setPageRequest(PaginationRequest.builder()
						.limit(userFilterDto.getPageRequest().getLimit())
						.pageNo(userFilterDto.getPageRequest().getPageNo())
				.build());

		userFilterDto.setMigrated(Boolean.FALSE);
		Page<UserEntity> userPage = getUserPage(userFilterDto);

		Integer pageNo = userFilterDto.getPageRequest().getPageNo();

		log.info("Found " + userPage.getNumberOfElements() + " User Records on Page: " + pageNo
				+ " for Search Criteria");

		List<UserProfileDto> userDtos = userPage.getContent().stream().map(UserAdapter::getUserProfileDto)
				.collect(Collectors.toList());

		if(userv2DtoPageResponse.getRecords()<userFilterDto.getPageRequest().getLimit()){
			userProfileDtos.addAll(userDtos.stream().limit(userFilterDto.getPageRequest().getLimit()-userv2DtoPageResponse.getRecords()).collect(Collectors.toList()));
		}

		int totalRecords=0;

		if(userv2DtoPageResponse.getRecords()+userPage.getNumberOfElements()>userFilterDto.getPageRequest().getLimit()){
			totalRecords=userFilterDto.getPageRequest().getLimit();
		}
		else{
			totalRecords=userv2DtoPageResponse.getRecords()+userPage.getNumberOfElements();
		}

		int totalPages= (int) Math.ceil((userv2DtoPageResponse.getTotalRecords()+userPage.getTotalElements())/Double.valueOf(userFilterDto.getPageRequest().getLimit()));

		return new PageResponse<>(pageNo,totalRecords, totalPages,
				userv2DtoPageResponse.getTotalRecords()+userPage.getTotalElements(), userProfileDtos);

	}
	
	@Override
	public Set<UserProfileDto> searchUserList(UserFilterDto userFilterDto) {

		Set<UserProfileDto> userDtos = getUserList(userFilterDto).stream().map(UserAdapter::getUserProfileDto)
				.collect(Collectors.toSet());

		return userDtos;
	}

	@Override
	public UserProfileDto getActiveUsersByEmail(String email) {
		log.info("Searching User by email: " + email);

		UserEntity userEntity = userDbService.findActiveUserByEmail(email);

		if (Objects.isNull(userEntity)) {
			throw new StanzaException("User not found for email: " + email);
		}

		return UserAdapter.getUserProfileDto(userEntity);
	}

	private void validateConstraint(List<AddUserAndRoleRequestDto> addUserAndRoleRequestDtoList) {
		Set<ConstraintViolation<AddUserAndRoleRequestDto>> violations = new HashSet<>();

		addUserAndRoleRequestDtoList.forEach(addUserAndRoleRequestDto -> {

			if(addUserAndRoleRequestDto.getIsoCode() == null || addUserAndRoleRequestDto.getIsoCode().isEmpty()) {
				addUserAndRoleRequestDto.setIsoCode("IN");
			}

			if(userDbService.getUserForMobile(addUserAndRoleRequestDto.getMobile(), addUserAndRoleRequestDto.getIsoCode()) == null) {
				// if user does not exist then validating
				violations.addAll(validator.validate(addUserAndRoleRequestDto));
			}
		});

		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<AddUserAndRoleRequestDto> constraintViolation : violations) {
				sb.append(constraintViolation.getMessage()).append(", ");
			}
			throw new ConstraintViolationException("Error occurred: " + sb, violations);
		}
	}

	private void assignRoleToAllUser(Map<String, ArrayList<AddUserAndRoleRequestDto>> userAndRoleRequestDtoMapByMobile, List<UserEntity> newUserEntityList) {

		newUserEntityList.forEach(userEntity -> {
			String key = String.format("%s%s", userEntity.getIsoCode(), userEntity.getMobile());
			List<AddUserAndRoleRequestDto> userAndRoleRequestDtoListByMobile = userAndRoleRequestDtoMapByMobile.get(key);

			userAndRoleRequestDtoListByMobile.forEach(userAndRoleRequestDtoByMobile -> {
				assignRoleToUser(userAndRoleRequestDtoByMobile, userEntity);
			});

		});
	}

	private void assignRoleToUser(AddUserAndRoleRequestDto addUserAndRoleRequestDto, UserEntity userEntity) {

			if (CollectionUtils.isEmpty(addUserAndRoleRequestDto.getRolesUuid()) || Objects.isNull(addUserAndRoleRequestDto.getAccessLevel()) ||
					CollectionUtils.isEmpty(addUserAndRoleRequestDto.getAccessLevelEntityListUuid()) || Objects.isNull(addUserAndRoleRequestDto.getRoleDepartment())) {
				// Assigning default role
				addUserOrConsumerRole(userEntity);
			} else {
				List<String> validRoles = validateRole(addUserAndRoleRequestDto);

				if (CollectionUtils.isEmpty(validRoles)) return;

				aclUserService.addRole(
						AddUserDeptLevelRoleRequestDto.builder()
								.userUuid(userEntity.getUuid())
								.department(addUserAndRoleRequestDto.getRoleDepartment())
								.rolesUuid(addUserAndRoleRequestDto.getRolesUuid())
								.accessLevel(addUserAndRoleRequestDto.getAccessLevel())
								.accessLevelEntityListUuid(addUserAndRoleRequestDto.getAccessLevelEntityListUuid())
								.build()
				);
			}
	}

	private List<String> validateRole(AddUserAndRoleRequestDto addUserAndRoleRequestDto) {

		// validating the role department and the accessLevel while bulk upload
		List<String> validRoles = new ArrayList<>();

		List<RoleDto> roleDtoList = roleService.getRoleByUuidIn(addUserAndRoleRequestDto.getRolesUuid());

		roleDtoList.forEach(roleDto -> {
			if (roleDto.getDepartment() == addUserAndRoleRequestDto.getRoleDepartment() && roleDto.getAccessLevel() == addUserAndRoleRequestDto.getAccessLevel()) {
				validRoles.add(roleDto.getUuid());
			}
		});
		return validRoles;
	}

	private void publishToKafka(Object object) {
		KafkaDTO kafkaDTO = new KafkaDTO();
		kafkaDTO.setData(object);
		notificationProducer.publish(kafkaResidentDetailTopic, KafkaDTO.class.getName(), kafkaDTO);
	}

	private Map<String, ArrayList<AddUserAndRoleRequestDto>> createUserAndRoleRequestDtoListByMobile(List<AddUserAndRoleRequestDto> addUserAndRoleRequestDtoList) {

		Map<String, ArrayList<AddUserAndRoleRequestDto>> map = new HashMap<>();
		addUserAndRoleRequestDtoList.forEach(addUserAndRoleRequestDto -> {
			String key = String.format("%s%s",addUserAndRoleRequestDto.getIsoCode(), addUserAndRoleRequestDto.getMobile());

			if(map.containsKey(key)) {
				map.get(key).add(addUserAndRoleRequestDto);
			} else {
				map.put(key, new ArrayList<AddUserAndRoleRequestDto>(){{ add(addUserAndRoleRequestDto); }});
			}
		});
		return map;
	}

	private UserEntity checkExistingUser(ArrayList<AddUserAndRoleRequestDto> userAndRoleRequestDtoListByMobile) {

		AddUserAndRoleRequestDto addUserAndRoleRequestDto = userAndRoleRequestDtoListByMobile.get(0);

		if (!PhoneNumberUtils.isValidMobileForCountry(addUserAndRoleRequestDto.getMobile(), addUserAndRoleRequestDto.getIsoCode())) {
			log.error("Number: " + addUserAndRoleRequestDto.getMobile() + " and ISO: " + addUserAndRoleRequestDto.getIsoCode()
					+ " doesn't appear to be a valid mobile combination");
			throw new ApiValidationException("Mobile Number and ISO Code combination not valid");
		}

		UserEntity userEntity = userDbService.getUserForMobile(addUserAndRoleRequestDto.getMobile(),
				addUserAndRoleRequestDto.getIsoCode());
		if(Objects.isNull(userEntity)) return null;

		if(!userEntity.isStatus()) {
			userEntity.setStatus(true);
			userDbService.update(userEntity);
		}

		log.warn("User: " + userEntity.getUuid() + " already exists for Mobile: " + addUserAndRoleRequestDto.getMobile()
				+ ", ISO Code: " + addUserAndRoleRequestDto.getIsoCode() + " of type: " + addUserAndRoleRequestDto.getUserType());

		// assigning all the roles to user
		userAndRoleRequestDtoListByMobile.forEach(userAndRoleRequestDtoByMobile -> {
			assignRoleToUser(userAndRoleRequestDtoByMobile, userEntity);
		});

		return userEntity;

	}

	private UserEntity createNewUser(AddUserAndRoleRequestDto addUserAndRoleRequestDto) {
		log.info("Adding new User [Mobile: " + addUserAndRoleRequestDto.getMobile() + ", ISOCode: "
				+ addUserAndRoleRequestDto.getIsoCode() + ", UserType: " + addUserAndRoleRequestDto.getUserType() + "]");

		UserProfileEntity profileEntity = UserAdapter.getUserProfileEntity(addUserAndRoleRequestDto);

		UserEntity userEntity = UserEntity.builder().userType(addUserAndRoleRequestDto.getUserType())
				.isoCode(addUserAndRoleRequestDto.getIsoCode()).mobile(addUserAndRoleRequestDto.getMobile()).mobileVerified(false)
				.email(addUserAndRoleRequestDto.getEmail()).emailVerified(false).userProfile(profileEntity).status(true)
				.department(addUserAndRoleRequestDto.getDepartment()).build();

		profileEntity.setUser(userEntity);
		return userEntity;
	}

	private Page<UserEntity> getUserPage(UserFilterDto userFilterDto) {

		Specification<UserEntity> specification = userDbService.getSearchQuery(userFilterDto);

		Pageable pagination = getPaginationForSearchRequest(userFilterDto.getPageRequest().getPageNo(),
				userFilterDto.getPageRequest().getLimit());

		return userDbService.findAll(specification, pagination);
	}
	
	private List<UserEntity> getUserList(UserFilterDto userFilterDto) {

		Specification<UserEntity> specification = userDbService.getSearchQuery(userFilterDto);

		return userDbService.findAll(specification);
	}

	private Pageable getPaginationForSearchRequest(int pageNo, int limit) {

		Pageable pagination = PageRequest.of(0, 10, Direction.DESC, "createdAt");

		if (pageNo > 0 && limit > 0 && limit < 1000) {
			pagination = PageRequest.of(pageNo - 1, limit, Direction.DESC, "createdAt");
		}

		return pagination;
	}

	@Override
	public boolean updateUserStatus(String userId, Boolean status) {
		//create api to update user status

		String res=userV2FeignService.updateUserStatus(userId,status);
		if(res.equalsIgnoreCase("Not Updated")) {
			UserEntity user = userDbService.findByUuidAndStatus(userId, !status);
			if (user == null) {
				throw new ApiValidationException("User either does not exist or user is already in desired state.");
			}
			UserProfileEntity userProfile = user.getUserProfile();

			if (userProfile != null) {
				userProfile.setStatus(status);
				user.setUserProfile(userProfile);
			}

			user.setStatus(status);
			userDbService.save(user);
		}
		return true;
	}

	@Override
	public UserManagerAndRoleDto getUserWithManagerAndRole(String userUuid) {
		UserProfileDto userProfile = getUserProfile(userUuid);
		if (userProfile == null) {
			throw new NoRecordException("Please provide valid userId.");
		}
		UserProfileDto managerProfile = userManagerMappingService.getManagerProfileForUser(userUuid);
		List<RoleDto> roleDtoList = aclUserService.getUserRoles(userUuid);

		return UserManagerAndRoleDto.builder().userProfile(userProfile).manager(managerProfile).roles(roleDtoList)
				.build();
	}

	@Override
	public List<UserProfileDto> getAllUsers() {

		List<UserEntity> userEntities = userDbService.findAll();

		return UserAdapter.getUserProfileDtos(userEntities);
	}

	@Override
	public List<UserProfileDto> getAllActiveUsersByUuidIn(ActiveUserRequestDto activeUserRequestDto) {

		List<UserEntity> userEntities = userDbService.findByUuidInAndStatus(activeUserRequestDto.getUserIds(), true);

		return UserAdapter.getUserProfileDtos(userEntities);
	}

	@Override
	public List<UserEntity> getUserByEmail(String email) {

		List<com.stanzaliving.user.dto.userv2.UserDto> userDtos=userV2FeignService.getUserFromEmail(email);
		List<UserEntity> userV2Entities=new ArrayList<>();

		if(Objects.nonNull(userDtos) && userDtos.size()>0) {
			for (com.stanzaliving.user.dto.userv2.UserDto userDto : userDtos) {
				userV2Entities.add(Userv2ToUserAdapter.getUserEntityFromUserv2(userDto));
			}
		}
		List<UserEntity> userEntityList=userDbService.findByEmailNotMigrated(email,false);
		if(userEntityList.size()>0) {
			userV2Entities.addAll(userEntityList);
		}
		return userV2Entities;
	}

	@Override
	public boolean updateUserTypeAndDepartment(UpdateDepartmentUserTypeDto updateDepartmentUserTypeDto) {

		log.info("Searching User by UserId: " + updateDepartmentUserTypeDto.getUserId());

		UserEntity userEntity = userDbService.findByUuidAndStatus(updateDepartmentUserTypeDto.getUserId(),
				Boolean.TRUE);

		if (Objects.isNull(userEntity)) {
			throw new UserValidationException("User not found for UserId: " + updateDepartmentUserTypeDto.getUserId());
		}

		userEntity.setUserType(updateDepartmentUserTypeDto.getUserType());
		userEntity.setDepartment(updateDepartmentUserTypeDto.getDepartment());

		userEntity = userDbService.update(userEntity);

		addUserOrConsumerRole(userEntity);

		return Objects.nonNull(userEntity);
	}

	@Override
	public UserDto updateUser(UpdateUserRequestDto updateUserRequestDto) {

		com.stanzaliving.user.dto.userv2.UserDto userDto=userV2FeignService.updateWithUser(updateUserRequestDto);
		if(Objects.nonNull(userDto)) {
			return UserAdapter.getUserProfileDto(Userv2ToUserAdapter.getUserEntityFromUserv2(userDto));
		}

		UserEntity userEntity = userDbService.findByUuidNotMigrated(updateUserRequestDto.getUserId(),false);
		if (Objects.isNull(userEntity)) {
			throw new UserValidationException("User not found for UserId: " + updateUserRequestDto.getUserId());
		}

		if (Objects.nonNull(updateUserRequestDto.getAddress())) {
			userEntity.getUserProfile().setAddress(AddressAdapter.getAddressEntity(updateUserRequestDto.getAddress()));
		}
		if (Objects.nonNull(updateUserRequestDto.getBirthday())) {
			userEntity.getUserProfile().setBirthday(updateUserRequestDto.getBirthday());
		}
		if (Objects.nonNull(updateUserRequestDto.getBloodGroup())) {
			userEntity.getUserProfile().setBloodGroup(updateUserRequestDto.getBloodGroup());
		}
		if (Objects.nonNull(updateUserRequestDto.getEmail())) {
			userEntity.setEmail(updateUserRequestDto.getEmail());
		}
		if (Objects.nonNull(updateUserRequestDto.getFirstName())) {
			userEntity.getUserProfile().setFirstName(updateUserRequestDto.getFirstName());
		}
		if (Objects.nonNull(updateUserRequestDto.getGender())) {
			userEntity.getUserProfile().setGender(updateUserRequestDto.getGender());
		}
		if (Objects.nonNull(updateUserRequestDto.getLastName())) {
			userEntity.getUserProfile().setLastName(updateUserRequestDto.getLastName());
		}
		if (Objects.nonNull(updateUserRequestDto.getNationality())) {
			userEntity.getUserProfile().setNationality(updateUserRequestDto.getNationality());
		}
		if (Objects.nonNull(updateUserRequestDto.getProfilePicture())) {
			userEntity.getUserProfile().setProfilePicture(updateUserRequestDto.getProfilePicture());
		}
		if (Objects.nonNull(updateUserRequestDto.getDateOfArrival())) {
			userEntity.getUserProfile().setArrivalDate(updateUserRequestDto.getDateOfArrival());
		}
		if (Objects.nonNull(updateUserRequestDto.getForiegnCountryCode())) {
			userEntity.getUserProfile().setSecondaryIsoCode(updateUserRequestDto.getForiegnCountryCode());
		}
		if (Objects.nonNull(updateUserRequestDto.getForiegnMobileNumber())) {
			userEntity.getUserProfile().setSecondaryMobile(updateUserRequestDto.getForiegnMobileNumber());
			userEntity.getUserProfile().setProfilePicture(updateUserRequestDto.getForiegnMobileNumber());
		}
		if (Objects.nonNull(updateUserRequestDto.getNextDestination())) {
			userEntity.getUserProfile().setNextDestination(updateUserRequestDto.getNextDestination());
		}
		if (Objects.nonNull(updateUserRequestDto.getUserMobile())) {
			//not allowing reuse of even inactive user's number.
			//not checking ISO code
			if ((!updateUserRequestDto.getUserMobile().equals(userEntity.getMobile())) && Objects.nonNull(userDbService.findByMobile(updateUserRequestDto.getUserMobile()))) {
				throw new ApiValidationException("User exists for Mobile Number: " + updateUserRequestDto.getUserMobile());
			}
			userEntity.setMobile(updateUserRequestDto.getUserMobile());
		}
		if (Objects.nonNull(updateUserRequestDto.getDepartment())) {
			userEntity.setDepartment(updateUserRequestDto.getDepartment());
		}
		if (Objects.nonNull(updateUserRequestDto.getUserType())) {
			userEntity.setUserType(updateUserRequestDto.getUserType());
		}
		if (Objects.nonNull(updateUserRequestDto.getMiddleName())) {
			userEntity.getUserProfile().setMiddleName(updateUserRequestDto.getMiddleName());
		}

		if(!userEntity.isStatus())
			userEntity.setStatus(true);

		userEntity = userDbService.update(userEntity);

		UserProfileDto userProfileDto = UserAdapter.getUserProfileDto(userEntity);

		addUserOrConsumerRole(userEntity);

		KafkaDTO kafkaDTO = new KafkaDTO();
		kafkaDTO.setData(userProfileDto);

		notificationProducer.publish(kafkaResidentDetailTopic, KafkaDTO.class.getName(), kafkaDTO);

		return userProfileDto;
	}

	private void addUserOrConsumerRole(UserEntity userEntity) {
		if (userEntity.getUserType().equals(UserType.CONSUMER) || userEntity.getUserType().equals(UserType.EXTERNAL)) {
			AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = getRoleDetails(userEntity);

			aclUserService.addRole(addUserDeptLevelRoleRequestDto);
		}
	}
	
	private void addUserOrConsumerRoleByRoleNames(UserEntity userEntity, List<String> roleNames) {
		
		if (Objects.nonNull(userEntity) && (UserType.VENDOR == userEntity.getUserType() || UserType.FOOD_DELIVERY_AGENT == userEntity.getUserType())) {
			AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = getRoleDetailsForListOfRoleNames(userEntity, roleNames);

			aclUserService.addRole(addUserDeptLevelRoleRequestDto);
		}
	}

	@Override
	public UserDto updateUserMobile(UpdateUserRequestDto updateUserRequestDto) {
		return updateUser(updateUserRequestDto);
	}

	@Override
	public boolean updateUserStatus(String mobileNo, UserType userType, Boolean enabled) {

		UserEntity user = userDbService.findByMobileAndUserType(mobileNo, userType);

		if (user == null) {
			throw new ApiValidationException("User either does not exist.");
		}
		UserProfileEntity userProfile = user.getUserProfile();

		if (userProfile != null) {
			userProfile.setStatus(enabled);
			user.setUserProfile(userProfile);
		}

		user.setStatus(enabled);
		userDbService.save(user);
		return Boolean.TRUE;
	}

	@Override
	public UserDto updateUserType(String mobileNo, String isoCode, UserType userType) {

		UserEntity userEntity = userDbService.getUserForMobile(mobileNo, isoCode);

		if (Objects.isNull(userEntity)) {
			throw new UserValidationException("User does not exists for Mobile Number: " + mobileNo + " and isoCode :" + isoCode);
		}

		if (Objects.nonNull(userType)) {
			userEntity.setUserType(userType);
		}

		UserDto userDto = UserAdapter.getUserProfileDto(userDbService.update(userEntity));

		addUserOrConsumerRole(userEntity);

		return userDto;
	}

	public UserDto getUserForAccessLevelAndRole(@Valid AccessLevelRoleRequestDto cityRolesRequestDto) {

//		Map<String,List<String>> userAccessLevelMap=userV2FeignService.getUserAndAccessLevelMapForRole(cityRolesRequestDto.getRoleName()
//				,cityRolesRequestDto.getDepartment());
//
//		for(Map.Entry<String,List<String>> entry:userAccessLevelMap.entrySet()){
//			if(entry.getValue().contains(cityRolesRequestDto.getAccessLevelUuid())){
//				return UserAdapter.getUserDto(Userv2ToUserAdapter.getUserEntityFromUserv2(userV2FeignService.getUserByUuid(entry.getKey())));
//			}
//		}
//		RoleDto roleDto = roleService.findByRoleNameAndDepartment(cityRolesRequestDto.getRoleName(),
//				cityRolesRequestDto.getDepartment());
//		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleService
//				.findByRoleUuid(roleDto.getUuid());
//
//		if (CollectionUtils.isEmpty(userDepartmentLevelRoleEntityList)) {
//			return null;
//		}
//
//		for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
//			UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelService
//					.findByUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());
//			String csvStringOfUuids = userDepartmentLevelEntity.getCsvAccessLevelEntityUuid();
//
//			if (StringUtils.isNotEmpty(csvStringOfUuids)) {
//				List<String> accessLevelEntityUuids = Arrays
//						.asList(csvStringOfUuids.split(UserConstants.DELIMITER_KEY));
//				if (accessLevelEntityUuids.contains(cityRolesRequestDto.getAccessLevelUuid())) {
//					UserEntity userEntity = userDbService.findByUuidNotMigrated(userDepartmentLevelEntity.getUserUuid(),false);
//					return UserAdapter.getUserDto(userEntity);
//				}
//			}
//
//		}
//		return null;
		RoleDto roleDto = roleService.findByRoleNameAndDepartment(cityRolesRequestDto.getRoleName(),
				cityRolesRequestDto.getDepartment());
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleService
				.findByRoleUuid(roleDto.getUuid());

		if (CollectionUtils.isEmpty(userDepartmentLevelRoleEntityList)) {
			return null;
		}

		for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
			UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelService
					.findByUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());
			String csvStringOfUuids = userDepartmentLevelEntity.getCsvAccessLevelEntityUuid();

			if (StringUtils.isNotEmpty(csvStringOfUuids)) {
				List<String> accessLevelEntityUuids = Arrays
						.asList(csvStringOfUuids.split(UserConstants.DELIMITER_KEY));
				if (accessLevelEntityUuids.contains(cityRolesRequestDto.getAccessLevelUuid())) {
					UserEntity userEntity = userDbService.findByUuid(userDepartmentLevelEntity.getUserUuid());
					return UserAdapter.getUserDto(userEntity);
				}
			}

		}
		return null;
	}

	@Override
	public List<UserDto> getUsersForRole(AccessLevelRoleRequestDto cityRolesRequestDto){
//
//		List<com.stanzaliving.user.dto.userv2.UserDto> userV2Dtos=userV2FeignService.findUsersForRoleNameAndDepartment(cityRolesRequestDto.getRoleName(),cityRolesRequestDto.getDepartment());
//		List<UserDto> userDtos=new ArrayList<>();
//
//		if(Objects.nonNull(userV2Dtos) && userV2Dtos.size()>0) {
//			for (com.stanzaliving.user.dto.userv2.UserDto userDto : userV2Dtos) {
//				userDtos.add(UserAdapter.getUserDto(Userv2ToUserAdapter.getUserEntityFromUserv2(userDto)));
//			}
//		}
//
//		RoleDto roleDto=null;
//		try {
//			roleDto = roleService.findByRoleNameAndDepartment(cityRolesRequestDto.getRoleName(),
//					cityRolesRequestDto.getDepartment());
//		}
//		catch (ApiValidationException e){}
//		if(Objects.nonNull(roleDto)) {
//			List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleService
//					.findByRoleUuid(roleDto.getUuid());
//
//			if (CollectionUtils.isNotEmpty(userDepartmentLevelRoleEntityList)) {
//				for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
//					UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelService
//							.findByUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());
//
//					UserEntity userEntity = userDbService.findByUuidNotMigrated(userDepartmentLevelEntity.getUserUuid(),false);
//					if (Objects.nonNull(userEntity) && userEntity.getDepartment().equals(cityRolesRequestDto.getDepartment())) {
//						userDtos.add(UserAdapter.getUserDto(userEntity));
//					}
//
//				}
//			}
//		}
//		return userDtos;
		RoleDto roleDto = roleService.findByRoleNameAndDepartment(cityRolesRequestDto.getRoleName(),
				cityRolesRequestDto.getDepartment());
		List<UserDepartmentLevelRoleEntity> userDepartmentLevelRoleEntityList = userDepartmentLevelRoleService
				.findByRoleUuid(roleDto.getUuid());

		if (CollectionUtils.isEmpty(userDepartmentLevelRoleEntityList)) {
			return null;
		}
		List<UserDto> userDtos=new ArrayList<>();

		for (UserDepartmentLevelRoleEntity userDepartmentLevelRoleEntity : userDepartmentLevelRoleEntityList) {
			UserDepartmentLevelEntity userDepartmentLevelEntity = userDepartmentLevelService
					.findByUuid(userDepartmentLevelRoleEntity.getUserDepartmentLevelUuid());

			UserEntity userEntity = userDbService.findByUuid(userDepartmentLevelEntity.getUserUuid());
			if(userEntity.getDepartment().equals(cityRolesRequestDto.getDepartment())) {
				userDtos.add(UserAdapter.getUserDto(userEntity));
			}

		}
		return userDtos;
	}

	@Override
	public boolean createRoleBaseUser(UserType userType) {

		List<UserEntity> userEntity = userDbService.findByUserType(userType);

		if (CollectionUtils.isEmpty(userEntity)) {
			log.error("user Type: " + userType + " not available in User table.");
			throw new ApiValidationException("User Type not exists in user Table.");
		}
		userEntity.forEach(user -> {
			if(user.isStatus()) {
				AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = getRoleDetails(user);

				aclUserService.addRole(addUserDeptLevelRoleRequestDto);
			}

		});

		return Boolean.TRUE;
	}

	@Override
	public boolean createRoleBaseUser(List<String> mobiles) {

		for (String mobile : mobiles) {
			UserEntity userEntity = userDbService.findByMobile(mobile);
			if(Objects.nonNull(userEntity) && userEntity.isStatus()) {
				AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = getRoleDetails(userEntity);

				aclUserService.addRole(addUserDeptLevelRoleRequestDto);
			}
		}
		return Boolean.TRUE;
	}

	private AddUserDeptLevelRoleRequestDto getRoleDetails(UserEntity user) {
		AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = AddUserDeptLevelRoleRequestDto.builder()
				.build();

		addUserDeptLevelRoleRequestDto.setUserUuid(user.getUuid());
		addUserDeptLevelRoleRequestDto.setAccessLevelEntityListUuid(Arrays.asList(countryUuid));

		if (user.getUserType().getTypeName().equalsIgnoreCase("Consumer")) {
			addUserDeptLevelRoleRequestDto.setRolesUuid(Arrays.asList(consumerUuid));
			addUserDeptLevelRoleRequestDto.setAccessLevel(AccessLevel.valueOf("COUNTRY"));
			addUserDeptLevelRoleRequestDto.setDepartment(user.getDepartment());
		} else if (user.getUserType().getTypeName().equalsIgnoreCase("External")) {
			addUserDeptLevelRoleRequestDto.setRolesUuid(Arrays.asList(brokerUuid));
			addUserDeptLevelRoleRequestDto.setAccessLevel(AccessLevel.valueOf("COUNTRY"));
			addUserDeptLevelRoleRequestDto.setDepartment(user.getDepartment());
		}

		return addUserDeptLevelRoleRequestDto;
	}
	
	private AddUserDeptLevelRoleRequestDto getRoleDetailsForListOfRoleNames(UserEntity user, List<String> roleNames){
		AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = AddUserDeptLevelRoleRequestDto.builder()
				.build();

		addUserDeptLevelRoleRequestDto.setUserUuid(user.getUuid());
		addUserDeptLevelRoleRequestDto.setAccessLevelEntityListUuid(Arrays.asList(countryUuid));

		if (Objects.nonNull(user) && (UserType.VENDOR == user.getUserType() || UserType.FOOD_DELIVERY_AGENT == user.getUserType())) {

			if (CollectionUtils.isNotEmpty(roleNames)) {
				List<RoleEntity> roleEntities = roleRepository.findByRoleNameInAndDepartment(roleNames, user.getDepartment());
				List<String> roleUuids = roleEntities.stream().map(RoleEntity::getUuid).collect(Collectors.toList());

				addUserDeptLevelRoleRequestDto.setRolesUuid(roleUuids);
			}

			addUserDeptLevelRoleRequestDto.setAccessLevel(AccessLevel.valueOf("COUNTRY"));
			addUserDeptLevelRoleRequestDto.setDepartment(user.getDepartment());
		}
		
		return addUserDeptLevelRoleRequestDto;
	}

	@Override
	public Map<String, UserProfileDto> getUserProfileDto(Set<String> mobileNos) {

		Map<String, UserProfileDto> userMap = new HashMap<>();

		List<UserProfileDto> userProfileDto=UserAdapter.getUserProfileDtos(userDbService.findByMobileIn(mobileNos));

		userProfileDto.forEach(user -> {
			userMap.put(user.getMobile(), user);
		});

		return userMap;
	}
	
	
	@Override
	public List<String> getUserProfileDto(List<String> mobileNos) {

		Set<String> mobileNo = mobileNos.stream().collect(Collectors.toSet());

		List<UserProfileDto> userProfileDto=UserAdapter.getUserProfileDtos(userDbService.findByMobileIn(mobileNo));

		userProfileDto.forEach(user -> {
			if(mobileNos.contains(user.getMobile())) {
				mobileNos.remove(user.getMobile());
				
			}
		});

		return mobileNos;
	}

	@Override
	public UserProfileDto getUserDetails(String mobileNo) {

			log.info("Searching User by UserId: " + mobileNo);

			List<UserEntity> userEntities=new ArrayList<>(2);

			UserEntity userEntity=null;
			userEntity = userDbService.findByMobileNotMigrated(mobileNo,false);

			if(Objects.isNull(userEntity)){
				com.stanzaliving.user.dto.userv2.UserDto userDto=userV2FeignService.getActiveUser(Long.parseLong(mobileNo));
				if(Objects.nonNull(userDto)) {
					userEntity = Userv2ToUserAdapter.getUserEntityFromUserv2(userDto);
				}
			}

			if (Objects.isNull(userEntity)) {
				throw new UserValidationException("User not found for mobileNo: " + mobileNo);
			}

			return UserAdapter.getUserProfileDto(userEntity);
		}


		@Override
		public Map<String, UserProfileDto> getUserProfileForUserIn(List<String> userUuids) {

			Map<String, UserProfileDto> userMap = new HashMap<>();
			List<UserProfileDto> userProfileDto =new ArrayList<>();
			List<com.stanzaliving.user.dto.userv2.UserDto> userDtos=userV2FeignService.getUsersList(userUuids);
			List<UserEntity> userV2Entities=new ArrayList<>();
			if(userDtos.size()>0) {
				for (com.stanzaliving.user.dto.userv2.UserDto userDto : userDtos) {
					userV2Entities.add(Userv2ToUserAdapter.getUserEntityFromUserv2(userDto));
				}
			}
			List<UserEntity> userEntities=userDbService.findByUuidInNotMigrated(userUuids,false);
			if(Objects.nonNull(userEntities) && userEntities.size()>0){
				userV2Entities.addAll(userEntities);
			}

			userProfileDto = UserAdapter.getUserProfileDtos(userV2Entities);
			userProfileDto.forEach(user -> {
				userMap.put(user.getUuid(), user);
			});

			return userMap;
		}

	@Override
	public List<UserRoleCacheDto> getCacheableForRoles(List<String> roleNames) {

		log.info("Got request to get list of userid by rolenames {}", roleNames);
		Map<String,UserRoleCacheDto> cacheDtos = new HashMap<>(Department.values().length*roleNames.size());
		Set<String> users = new HashSet<>();
		for(Department department: Department.values()) {

//			log.info("Department {}",department);

			List<RoleDto> roleDtos = roleService.findByRoleNameInAndDepartment(roleNames, department);

//			log.info("Roles {}",roleDtos);


			for (RoleDto roleDto : roleDtos) {
				if (Objects.nonNull(roleDtos) && roleDto.getDepartment().equals(department)) {

					List<UserDepartmentLevelRoleEntity> departmentLevelRoleEntities = userDepartmentLevelRoleDbService.findByRoleUuid(roleDto.getUuid());

//					log.info("Department Role Entity {}",departmentLevelRoleEntities);

					if (CollectionUtils.isNotEmpty(departmentLevelRoleEntities)) {

						List<String> uuids = departmentLevelRoleEntities.stream().map(UserDepartmentLevelRoleEntity::getUserDepartmentLevelUuid).collect(Collectors.toList());

						List<UserDepartmentLevelEntity> departmentLevelEntities = userDepartmentLevelDbService.findByUuidInAndAccessLevel(uuids, roleDto.getAccessLevel());

						Set<String> userIds = departmentLevelEntities.stream().map(UserDepartmentLevelEntity::getUserUuid).collect(Collectors.toSet());
						log.info("userIds {}",userIds);

						Set<String> activeUserIds = getActiveUserUuids(userIds);

						if (CollectionUtils.isNotEmpty(departmentLevelEntities)) {

							String key = roleDto.getRoleName() + "" + department;
							cacheDtos.putIfAbsent(key, UserRoleCacheDto.builder().roleName(roleDto.getRoleName()).department(department).accessUserMap(new HashMap<>()).build());
							departmentLevelEntities.forEach(entity -> {

								if (activeUserIds.contains(entity.getUserUuid())) {
									Arrays.asList((entity.getCsvAccessLevelEntityUuid().split(","))).stream().forEach(accessUuid -> {
										cacheDtos.get(key).getAccessUserMap().putIfAbsent(accessUuid, new ArrayList<>());
										cacheDtos.get(key).getAccessUserMap().get(accessUuid).add(new UIKeyValue(entity.getUserUuid(), entity.getUserUuid()));
										users.add(entity.getUserUuid());
									});
								}

							});
						}
					}
				}
			}

		}
		if(CollectionUtils.isNotEmpty(users)){

			PaginationRequest paginationRequest = PaginationRequest.builder().pageNo(1).limit(users.size()).build();
			Map<String,String> userNames = this.searchUser(UserFilterDto.builder().pageRequest(paginationRequest).userIds(users.stream().collect(Collectors.toList())).build()).getData()
					.stream().collect(Collectors.toMap(f->f.getUuid(), f->getUserName(f)));
			return cacheDtos.values().stream().map(userRoleCacheDto -> {
				for (Map.Entry<String, List<UIKeyValue>> entry : userRoleCacheDto.getAccessUserMap().entrySet()) {
					entry.setValue(entry.getValue().stream().map(uiKeyValue -> new UIKeyValue(userNames.getOrDefault(uiKeyValue.getValue(),""),uiKeyValue.getValue())).collect(Collectors.toList()));
				}
				return userRoleCacheDto;
			}).collect(Collectors.toList());


		}
		return ListUtils.EMPTY_LIST;
	}

	/**
	 * @param userIds
	 * @return
	 */
	private Set<String> getActiveUserUuids(Set<String> userIds) {
		// build ActiveUserRequestDto
		ActiveUserRequestDto activeUserRequestDto = ActiveUserRequestDto.builder().userIds(userIds).build();

		// fetch active user response dtos
		List<UserProfileDto> userProfileDtos = getAllActiveUsersByUuidIn(activeUserRequestDto);

		// create set of active user ids
		Set<String> activeUserUuids = userProfileDtos.stream().map(UserProfileDto::getUuid).collect(Collectors.toSet());

		return activeUserUuids;
	}

	private String getUserName(UserProfileDto userProfile){
		StringBuilder name = new StringBuilder();

		if (Objects.nonNull(userProfile)) {
			name.append(StringUtils.defaultString(userProfile.getFirstName())).append( " ");
			name.append(StringUtils.defaultString(userProfile.getMiddleName())).append( " ");
			name.append(StringUtils.defaultString(userProfile.getLastName()));
		}
		return name.toString();
	}

	@Override
	public UserProfileDto getUserProfileDtoByEmail(String email) {

		log.info("Searching User by email: " + email);

		UserEntity userEntity = userDbService.findTop1ByEmailOrderByCreatedAtDesc(email);
		if(Objects.nonNull(userEntity) && userEntity.isStatus()){
			return UserAdapter.getUserProfileDto(userEntity);
		}

		List<com.stanzaliving.user.dto.userv2.UserDto> userDtos=userV2FeignService.getUserFromEmail(email);
		if(userDtos.size()>0) {
			userEntity = Userv2ToUserAdapter.getUserEntityFromUserv2(userDtos.get(0));
		}

		if (Objects.isNull(userEntity)) {
			throw new ApiValidationException("User not found for email: " + email);
		}

		return UserAdapter.getUserProfileDto(userEntity);
	}

	@Override
	public void saveUserDeptLevelForNewDept(Department newDept, Department refDept) {

		List<UserDepartmentLevelEntity> entityList = userDepartmentLevelRepository.findByDepartment(refDept);
		List<UserDepartmentLevelEntity> entityListNewDept = userDepartmentLevelRepository.findByDepartment(newDept);

		//handled for duplicate entries
		if(CollectionUtils.isNotEmpty(entityListNewDept)){

			log.info("already configured for new department: {}", newDept);
			return;
		}

		if (CollectionUtils.isNotEmpty(entityList)) {
			List<UserDepartmentLevelEntity> list = entityList.stream().map(entity ->
					UserDepartmentLevelEntity.builder()
							.department(newDept)
							.accessLevel(entity.getAccessLevel())
							.userUuid(entity.getUserUuid())
							.csvAccessLevelEntityUuid(entity.getCsvAccessLevelEntityUuid())
							.status(entity.isStatus()).build()).collect(Collectors.toList());

			userDepartmentLevelRepository.saveAll(list);
		}
	}

	@Override
	public void rollBack(Department newDepartment) {

		List<UserDepartmentLevelEntity> entityListNewDept = userDepartmentLevelRepository.findByDepartment(newDepartment);

		if(CollectionUtils.isNotEmpty(entityListNewDept)){

			userDepartmentLevelRepository.deleteAll(entityListNewDept);
		}
	}

	public UserDto addUserV3(AddUserRequestDto addUserRequestDto) {

		if (!PhoneNumberUtils.isValidMobileForCountry(addUserRequestDto.getMobile(), addUserRequestDto.getIsoCode())) {
			log.error("Number: " + addUserRequestDto.getMobile() + " and ISO: " + addUserRequestDto.getIsoCode()
				+ " doesn't appear to be a valid mobile combination");
			throw new ApiValidationException("Mobile Number and ISO Code combination not valid");
		}

		UserEntity userEntity = userDbService.getUserForMobile(addUserRequestDto.getMobile(),
			addUserRequestDto.getIsoCode());

		if (Objects.nonNull(userEntity) && userEntity.isStatus() == true) {

			throw new ApiValidationException("Active User already exists for Mobile: " + addUserRequestDto.getMobile()
				+ ", ISO Code: " + addUserRequestDto.getIsoCode());
		}

		if (Objects.nonNull(userEntity) && userEntity.isStatus() == false) {
			log.info("Activating deactivated user [Mobile: " + addUserRequestDto.getMobile() + ", ISOCode: "
				+ addUserRequestDto.getIsoCode() + ", UserType: " + addUserRequestDto.getUserType() + "]");

			userEntity.setUserType(addUserRequestDto.getUserType());
			userEntity.setEmail(addUserRequestDto.getEmail());
			userEntity.setDepartment(addUserRequestDto.getDepartment());
			userEntity.setStatus(true);
			UserProfileEntity userProfileEntity = userEntity.getUserProfile();
			userProfileEntity.setFirstName(addUserRequestDto.getFirstName());
			userProfileEntity.setLastName(addUserRequestDto.getLastName());
			userProfileEntity.setStatus(true);
			userEntity = userDbService.saveAndFlush(userEntity);
		} else {
			log.info("Adding new User [Mobile: " + addUserRequestDto.getMobile() + ", ISOCode: "
				+ addUserRequestDto.getIsoCode() + ", UserType: " + addUserRequestDto.getUserType() + "]");

			UserProfileEntity profileEntity = UserAdapter.getUserProfileEntity(addUserRequestDto);

			userEntity = UserEntity.builder().userType(addUserRequestDto.getUserType())
				.isoCode(addUserRequestDto.getIsoCode()).mobile(addUserRequestDto.getMobile()).mobileVerified(false)
				.email(addUserRequestDto.getEmail()).emailVerified(false).userProfile(profileEntity).status(true)
				.department(addUserRequestDto.getDepartment()).build();

			profileEntity.setUser(userEntity);

			userEntity = userDbService.saveAndFlush(userEntity);

			log.info("Added New User with Id: " + userEntity.getUuid());
		}
		addUserOrConsumerRole(userEntity);
		addSigmaManageSalesRole(userEntity);
		UserDto userDto = UserAdapter.getUserDto(userEntity);

		KafkaDTO kafkaDTO = new KafkaDTO();
		kafkaDTO.setData(userDto);

		notificationProducer.publish(kafkaResidentDetailTopic, KafkaDTO.class.getName(), kafkaDTO);

		return userDto;
	}

	private void addSigmaManageSalesRole(UserEntity userEntity) {
		AddUserDeptLevelRoleRequestDto addUserDeptLevelRoleRequestDto = AddUserDeptLevelRoleRequestDto.builder().department(Department.SALES)
			.rolesUuid(Arrays.asList(sigmaManageSalesUuid)).accessLevel(AccessLevel.COUNTRY)
			.accessLevelEntityListUuid(Arrays.asList(countryUuid)).userUuid(userEntity.getUuid()).build();
		aclUserService.addRole(addUserDeptLevelRoleRequestDto);
	}

	public List<String> getUserProfileDtoWhoseBirthdayIsToday() {
		log.info("Fetching users who have there birthday today.");
		List <String> newList = new ArrayList<>();
		List<String> userList = userDbService.getUserWhoseBirthdayIsToday().orElse(newList);

		return userList;
	}
}
