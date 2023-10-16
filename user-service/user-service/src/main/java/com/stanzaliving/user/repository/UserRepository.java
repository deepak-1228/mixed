/**
 * 
 */
package com.stanzaliving.user.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.user.Projections.UserView;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.core.user.enums.UserType;
import com.stanzaliving.user.entity.UserEntity;

/**
 * @author naveen
 *
 * @date 10-Oct-2019
 */
@Repository
public interface UserRepository extends AbstractJpaRepository<UserEntity, Long> {

	UserEntity findByMobileAndIsoCode(String mobile, String isoCode);

	/**
	 * @author piyush srivastava
	 * @param nameStartsWith String
	 * @return List
	 */
	@Query("SELECT u FROM com.stanzaliving.user.entity.UserEntity u"
			+ " WHERE CONCAT_WS(' ', u.userProfile.firstName, u.userProfile.middleName, u.userProfile.lastName)"
			+ " LIKE :name%")
	List<UserEntity> searchByName(@Param("name") String nameStartsWith);

	List<UserEntity> findByEmail(String email);

	List<UserEntity> findByUserType(UserType userType);

	UserEntity findByMobileAndUserType(String mobileNo, UserType userType);

	UserEntity findByUuidAndEmail(String userUuid, String email);

	List<UserEntity> findByMobileIn(Set<String> mobileNos);

	UserEntity findTop1ByEmailOrderByCreatedAtDesc(String email);

//	@Query("SELECT new com.stanzaliving.core.user.acl.dto.UuidByEmailDto(user.email, user.uuid) " +
//			"FROM UserEntity user " +
//			"WHERE user.department = :dept AND user.email IN (:emails)")
//	List<UserView> getUuidByEmailAndDept(@Param("emails") List<String> emails, @Param("dept") Department department);


	List<UserView> findByEmailInAndStatus(List<String> emails, Boolean status);
	
	@Query("Select u.uuid from user u where u.id in (select up.user.id from user_profile up where MONTH(up.birthday) = MONTH(NOW()) AND DAY(up.birthday) = DAY(NOW()))")
	Optional<List<String>> findUsersWhoseBirthdayIsToday();

	List<UserEntity> findByUuidInAndStatus(List<String> uuid, boolean status);

    List<UserEntity> findByUserTypeIn(List<UserType> asList);

	UserEntity findByMobileAndIsoCodeAndMigrated(String mobile, String isoCode, boolean migrated);

	UserEntity findByUuidAndMigrated(String uuid, boolean migrated);

	List<UserEntity> findByUuidInAndMigrated(List<String> userUuids, boolean migrated);

    List<UserEntity> findByEmailAndMigrated(String email, boolean migrated);

    List<UserEntity> findByUserTypeInAndStatus(List<UserType> asList, boolean status);

	UserEntity findByEmailAndStatus(String email, boolean b);

}