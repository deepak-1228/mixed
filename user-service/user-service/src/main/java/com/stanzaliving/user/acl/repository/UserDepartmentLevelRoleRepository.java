package com.stanzaliving.user.acl.repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDepartmentLevelRoleRepository extends AbstractJpaRepository<UserDepartmentLevelRoleEntity, Long>  {

    List<UserDepartmentLevelRoleEntity> findByUserDepartmentLevelUuid(String userDepartmentLevelUuid);

    List<UserDepartmentLevelRoleEntity> findByUserDepartmentLevelUuidAndStatus(String userDepartmentLevelUuid, boolean status);
    
    List<UserDepartmentLevelRoleEntity> findByRoleUuid(String roleUuid);

    List<UserDepartmentLevelRoleEntity> findByUserDepartmentLevelUuidAndRoleUuidInAndStatus(String userDepartmentLevelUuid, List<String> rolesUuid, boolean status);

    @Query(value = "SELECT u.userDepartmentLevelUuid " +
        "FROM com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity u " +
        "WHERE u.roleUuid IN :roleUuids " +
        "AND u.status = :status")
    List<String> findUserDepartmentLevelUuidByRoleUuidInAndStatus(List<String> roleUuids, boolean status);

    List<UserDepartmentLevelRoleEntity> findByUserDepartmentLevelUuidInAndStatus(List<String> userDepartmentLevelUuids, boolean status);

    UserDepartmentLevelRoleEntity findByUserDepartmentLevelUuidAndRoleUuidAndStatus(String userDepartmentLevelUuid, String roleUuid, boolean status);

    List<UserDepartmentLevelRoleEntity> findByRoleUuidInAndStatus(List<String> rolesUuid, boolean b);

    List<UserDepartmentLevelRoleEntity> findByUserDepartmentLevelUuidIn(List<String> udUuids);
}
