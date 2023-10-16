package com.stanzaliving.user.acl.repository;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDepartmentLevelRepository extends AbstractJpaRepository<UserDepartmentLevelEntity, Long> {

    List<UserDepartmentLevelEntity> findByUserUuidAndDepartmentAndStatus(String userUuid, Department department, boolean status);

    List<UserDepartmentLevelEntity> findByUserUuidAndDepartment(String userUuid, Department department);

    List<UserDepartmentLevelEntity> findByUserUuidAndStatus(String userUuid, boolean status);

    UserDepartmentLevelEntity findByUserUuidAndDepartmentAndAccessLevelAndStatus(String userUuid, Department department, AccessLevel accessLevel, boolean status);

    List<UserDepartmentLevelEntity> findByUserUuidAndDepartmentAndAccessLevel(String userUuid, Department department, AccessLevel accessLevel);

    List<UserDepartmentLevelEntity> findByUuidInAndAccessLevel(List<String> uuids, AccessLevel accessLevel);

    List<UserDepartmentLevelEntity> findByDepartment(Department refDept);

    List<UserDepartmentLevelEntity> findByUuidInAndDepartmentAndAccessLevel(List<String> userUuid, Department department, AccessLevel accessLevel);

    List<UserDepartmentLevelEntity> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel);

    UserDepartmentLevelEntity findByUuid(String uuid);

    List<UserDepartmentLevelEntity> findByUserUuidIn(List<String> userUuids);
}
