/**
 * 
 */
package com.stanzaliving.user.db.service;

import com.stanzaliving.core.base.enums.Department;
import org.springframework.data.jpa.domain.Specification;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.dto.UserFilterDto;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.entity.UserEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
public interface UserDbService extends AbstractJpaService<UserEntity, Long> {
	
	UserEntity getUserForMobile(String mobile, String isoCode);

	UserEntity getUserForMobileNotMigrated(String mobile, String isoCode,boolean migrated);

	Specification<UserEntity> getSearchQuery(UserFilterDto userFilterDto);

	List<UserEntity> findByEmail(String email);

	UserEntity findByMobileAndUserType(String userMobile,UserType userType);

	List<UserEntity> findByUserType(UserType userType);
	
	UserEntity findByMobile(String mobile);

	UserEntity findByMobileNotMigrated(String mobile,boolean migrated);

	UserEntity findByUuid(String uuid);

	UserEntity findByUuidNotMigrated(String uuid,boolean migrated);

	UserEntity findByUuidAndEmail(String userUuid, String email);

	List<UserEntity> findByMobileIn(Set<String> mobileNos);

	UserEntity findTop1ByEmailOrderByCreatedAtDesc(String email);

	Map<String, String> getUuidByEmail(List<String> emails);

	UserEntity findActiveUserByEmail(String emailId);
	
	Optional<List<String>> getUserWhoseBirthdayIsToday();

	List<UserEntity> findAllByUuidInAndStatus(List<String> userId, boolean b);

	List<UserEntity> findByUserTypeIn(List<UserType> asList);

	List<UserEntity> findByUuidInNotMigrated(List<String> userUuids,boolean migrated);

	List<UserEntity> findByEmailNotMigrated(String email, boolean migrated);

	List<UserEntity> findByUserTypeInAndStatus(List<UserType> asList, boolean status);
}