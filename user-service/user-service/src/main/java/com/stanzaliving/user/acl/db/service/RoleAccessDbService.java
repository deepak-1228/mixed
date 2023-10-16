package com.stanzaliving.user.acl.db.service;

import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;

import java.util.List;

public interface RoleAccessDbService extends AbstractJpaService<RoleAccessEntity, Long> {
    RoleAccessEntity findByRoleUuidAndAccessUuidAndRoleAccessType(String roleUuid, String accessUuid, RoleAccessType roleAccessType);

    List<RoleAccessEntity> findByRoleUuidInAndRoleAccessTypeAndStatus(List<String> roleUuidListParent, RoleAccessType role, boolean status);

    List<RoleAccessEntity> findByRoleUuidInAndStatus(List<String> roleUuidListParent, boolean status);
}
