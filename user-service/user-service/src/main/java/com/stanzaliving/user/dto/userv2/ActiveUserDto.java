package com.stanzaliving.user.dto.userv2;


import com.stanzaliving.core.user.enums.UserType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ActiveUserDto {
    private String uuid;
    private UserType userType;
    private Long mobileNumber;
    private String isoCode;
    private String emailId;
}
