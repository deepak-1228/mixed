package com.stanzaliving.user.feignclient;

import com.stanzaliving.core.user.acl.dto.RoleDto;
import com.stanzaliving.user.acl.entity.RoleEntity;
import com.stanzaliving.user.dto.userv2.UserDto;
import com.stanzaliving.user.dto.userv2.UserMigrationRoleAndAssignmentDto;
import com.stanzaliving.user.dto.userv2.UserRoleMappingMigrationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "MigrationHttpService", url = "${service.userManagement.url}")
public interface MigrationHttpService {

    @PostMapping("/internal/migrate/users")
    void migrateUsers(@RequestBody List<UserDto> userDtos);

    @PostMapping("/internal/migrate/roles")
    void migrateRoles(@RequestBody RoleDto roleDto);

    @PostMapping("internal/migrate/userrolemapping")
    void migrateUserRoleMapping(@RequestBody UserMigrationRoleAndAssignmentDto userMigrationRoleAndAssignmentDto);

}
