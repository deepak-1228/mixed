/**
 * 
 */
package com.stanzaliving.user.acl.db.service;

import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.sqljpa.service.AbstractJpaService;
import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.user.acl.entity.RoleEntity;
import org.apache.kafka.common.protocol.types.Field;

import java.util.List;

/**
 * @author naveen
 *
 * @date 21-Oct-2019
 */
public interface RoleDbService extends AbstractJpaService<RoleEntity, Long> {

	boolean isRoleExists(String roleName);
	
	boolean isRoleExists(String roleName,Department department);

	RoleEntity findByRoleName(String roleName);

	RoleEntity findByRoleNameAndDepartment(String roleName,Department department);
	
	List<RoleEntity> findByDepartmentAndAccessLevel(Department department, AccessLevel accessLevel);

	List<RoleEntity> filter(RoleDto roleDto);

	List<RoleEntity> findByRoleNameAndDepartment(List<String> roleName, Department department);

	List<RoleEntity> findByDepartmentAndRoleNameEndsWithIgnoreCase(Department department, String suffix);

	List<RoleEntity> findByUuidInAndStatusNotMigrated(List<String> roleUuids, boolean b, boolean b1);

	List<RoleEntity> findByRoleNameAndDepartmentNotMigrated(List<String> roleNames, Department department, boolean migrated);

    List<RoleEntity> findByUuidInAndStatusAndMigrated(List<String> first, boolean status, boolean migrated);
}