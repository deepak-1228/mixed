package com.stanzaliving.user.dto.userv2;


import com.stanzaliving.core.base.enums.AccessLevel;
import com.stanzaliving.core.base.enums.Department;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.common.protocol.types.Field;


@Builder
@Getter
@Setter
public class UserRoleMappingMigrationDto {

    private Department department;

    private AccessLevel accessLevel;

    private String accesslevelUuids;

    private String roleUuid;

    private String userUuid;

    private String permission;

}
