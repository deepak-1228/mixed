package com.stanzaliving.user.dto.userv2;


import com.stanzaliving.core.user.acl.dto.RoleDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserMigrationRoleAndAssignmentDto {
    private RoleDto roleDto;
    private UserRoleMappingMigrationDto userRoleMappingMigrationDto;
}
