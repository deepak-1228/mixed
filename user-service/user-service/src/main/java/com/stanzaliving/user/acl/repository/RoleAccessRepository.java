package com.stanzaliving.user.acl.repository;

import com.stanzaliving.core.sqljpa.repository.AbstractJpaRepository;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleAccessRepository extends AbstractJpaRepository<RoleAccessEntity, Long> {

    RoleAccessEntity findByRoleUuidAndAccessUuidAndRoleAccessType(String roleUuid, String accessUuid, RoleAccessType roleAccessType);

    List<RoleAccessEntity> findByRoleUuidInAndRoleAccessTypeAndStatus(List<String> roleUuidListParent, RoleAccessType role, boolean status);

    List<RoleAccessEntity> findByRoleUuidInAndStatus(List<String> roleUuidListParent, boolean status);
}
