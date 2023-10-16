package com.stanzaliving.user.dto.userv2;


import com.stanzaliving.core.base.enums.Department;
import com.stanzaliving.core.user.enums.UserType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private Department department;
    private UserType userType;
    private UserType oldUserType;
    private String addressLine1;
    private String addressLine2;

}
