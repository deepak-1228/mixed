package com.stanzaliving.user.acl.adapters;


import com.stanzaliving.core.user.acl.request.dto.AddUserDeptLevelRequestDto;
import com.stanzaliving.user.acl.entity.UserDepartmentLevelEntity;
import lombok.experimental.UtilityClass;

import java.util.TreeSet;
import java.util.stream.Collectors;

@UtilityClass
public class UserDepartmentLevelAdapter {

    public static UserDepartmentLevelEntity getEntityFromRequest(AddUserDeptLevelRequestDto addUserDeptLevelRequestDto) {
        return UserDepartmentLevelEntity.builder()
                .userUuid(addUserDeptLevelRequestDto.getUserUuid())
                .department(addUserDeptLevelRequestDto.getDepartment())
                .accessLevel(addUserDeptLevelRequestDto.getAccessLevel())
                .csvAccessLevelEntityUuid(String.join(",",
                        addUserDeptLevelRequestDto.getAccessLevelEntityListUuid().stream().collect(Collectors.toCollection(TreeSet::new)).stream().collect(Collectors.toList())))
                .build();
    }

}
