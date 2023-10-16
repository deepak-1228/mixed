package com.stanzaliving.user.acl.repository;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.AccessModule;
import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.user.acl.entity.RoleAccessModuleMappingEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleAccessModuleRepository extends AbstractJpaRepository<RoleAccessModuleMappingEntity, Long> {

    @Query(value = "SELECT DISTINCT r.accessModule " +
        "FROM com.stanzaliving.user.acl.entity.RoleAccessModuleMappingEntity r " +
        "WHERE r.roleUuid IN :roleUuids " +
        "AND r.accessLevel IN :accessLevels " +
        "AND r.status = :status")
    List<AccessModule> findAccessModuleByRoleUuidInAndAccessLevelInAndStatus(List<String> roleUuids, List<AccessLevel> accessLevels, boolean status);

    @Query(value = "SELECT r.roleUuid " +
        "FROM com.stanzaliving.user.acl.entity.RoleAccessModuleMappingEntity r " +
        "WHERE r.accessModule IN :accessModules " +
        "AND r.status = :status")
    List<String> findRoleUuidByAccessModuleInAndStatus(List<AccessModule> accessModules, boolean status);

    RoleAccessModuleMappingEntity findByRoleUuidAndStatus(String roleUuid, boolean status);

    RoleAccessModuleMappingEntity findByAccessModuleAndAccessLevelAndStatus(AccessModule accessModule, AccessLevel accessLevel, boolean status);

    List<RoleAccessModuleMappingEntity> findByRoleUuidInAndStatus(List<String> roleUuids, boolean status);
}
