package com.stanzaliving.user.dto.userv2;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UpdateUserDto {
    private Long mobile;
    private Boolean mobileVerified;
    private Boolean emailVerified;
    private String firstName;
    private String lastName;
    private Boolean status;
}
