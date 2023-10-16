package com.stanzaliving.user.acl.db.service.impl;

import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.core.user.acl.enums.RoleAccessType;
import com.stanzaliving.user.acl.db.service.RoleAccessDbService;
import com.stanzaliving.user.acl.entity.RoleAccessEntity;
import com.stanzaliving.user.acl.repository.RoleAccessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleAccessDbServiceImpl extends AbstractJpaServiceImpl<RoleAccessEntity, Long, RoleAccessRepository> implements RoleAccessDbService {

    @Autowired
    RoleAccessRepository roleAccessRepository;

    @Override
    protected RoleAccessRepository getJpaRepository() {
        return roleAccessRepository;
    }


    @Override
    public RoleAccessEntity findByRoleUuidAndAccessUuidAndRoleAccessType(String roleUuid, String accessUuid, RoleAccessType roleAccessType) {
        return getJpaRepository().findByRoleUuidAndAccessUuidAndRoleAccessType(roleUuid, accessUuid, roleAccessType);
    }

    @Override
    public List<RoleAccessEntity> findByRoleUuidInAndRoleAccessTypeAndStatus(List<String> roleUuidListParent, RoleAccessType role, boolean status) {
        return getJpaRepository().findByRoleUuidInAndRoleAccessTypeAndStatus(roleUuidListParent, role, status);
    }

    @Override
    public List<RoleAccessEntity> findByRoleUuidInAndStatus(List<String> roleUuidListParent, boolean status) {
        return getJpaRepository().findByRoleUuidInAndStatus(roleUuidListParent, status);
    }
}
