package com.stanzaliving.user.acl.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelRoleEntity;

import java.util.List;

public interface UserDepartmentLevelRoleDbService extends AbstractJpaService<UserDepartmentLevelRoleEntity, Long> {
    List<UserDepartmentLevelRoleEntity> findByUserDepartmentLevelUuid(String userDepartmentLevelUuid);

    List<UserDepartmentLevelRoleEntity> findByUserDepartmentLevelUuidAndStatus(String userDepartmentLevelUuid, boolean status);
    
    List<UserDepartmentLevelRoleEntity> findByRoleUuid(String roleUuid);

    List<UserDepartmentLevelRoleEntity> findByUserDepartmentLevelUuidAndRoleUuidInAndStatus(String userDepartmentLevelUuid, List<String> rolesUuid, boolean status);

    List<UserDepartmentLevelRoleEntity> findByRoleUuidInAndStatus(List<String> rolesUuid, boolean status);

    List<UserDepartmentLevelRoleEntity> findByUserDepartmentLevelUuidIn(List<String> udUuids);
}
