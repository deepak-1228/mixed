package com.stanzaliving.user.acl.db.service.impl;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.service.impl.AbstractJpaServiceImpl;
import com.stanzaliving.user.acl.db.service.UserDepartmentLevelDbService;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import com.stanzaliving.user.acl.repository.UserDepartmentLevelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDepartmentLevelDbServiceImpl extends AbstractJpaServiceImpl<UserDepartmentLevelEntity, Long, UserDepartmentLevelRepository> implements UserDepartmentLevelDbService {

    @Autowired
    UserDepartmentLevelRepository userDepartmentLevelRepository;

    @Override
    protected UserDepartmentLevelRepository getJpaRepository() {
        return userDepartmentLevelRepository;
    }

    @Override
    public List<UserDepartmentLevelEntity> findByUserUuidAndDepartmentAndStatus(String userUuid, Department department, boolean status) {
        return userDepartmentLevelRepository.findByUserUuidAndDepartmentAndStatus(userUuid, department, status);
    }

    @Override
    public List<UserDepartmentLevelEntity> findByUserUuidAndDepartment(String userUuid, Department department) {
        return userDepartmentLevelRepository.findByUserUuidAndDepartment(userUuid, department);
    }

    @Override
    public List<UserDepartmentLevelEntity> findByUserUuidAndStatus(String userUuid, boolean status) {
        return userDepartmentLevelRepository.findByUserUuidAndStatus(userUuid, status);
    }

    @Override
    public UserDepartmentLevelEntity findByUserUuidAndDepartmentAndAccessLevelAndStatus(String userUuid, Department department, AccessLevel accessLevel, boolean status) {
        return userDepartmentLevelRepository.findByUserUuidAndDepartmentAndAccessLevelAndStatus(userUuid, department, accessLevel, status);
    }

    @Override
    public List<UserDepartmentLevelEntity> findByUserUuidAndDepartmentAndAccessLevel(String userUuid, Department department, AccessLevel accessLevel) {
        return userDepartmentLevelRepository.findByUserUuidAndDepartmentAndAccessLevel(userUuid, department, accessLevel);
    }

	@Override
	public List<UserDepartmentLevelEntity> findByUuidInAndAccessLevel(List<String> uuids, AccessLevel accessLevel) {
		return getJpaRepository().findByUuidInAndAccessLevel(uuids, accessLevel);
	}

    @Override
    public List<UserDepartmentLevelEntity> findByUuidInAndDepartmentAndAccessLevel(List<String> userUuid, Department department, AccessLevel accessLevel){
        return getJpaRepository().findByUuidInAndDepartmentAndAccessLevel(userUuid,department,accessLevel);
    }

    @Override
    public List<UserDepartmentLevelEntity> findByUserUuidIn(List<String> userUuids) {
        return getJpaRepository().findByUserUuidIn(userUuids);
    }
}
