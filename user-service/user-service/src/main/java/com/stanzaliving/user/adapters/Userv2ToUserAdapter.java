package com.stanzaliving.user.adapters;


import com.stanzaliving.core.sqljpa.entity.AddressEntity;
import com.stanzaliving.user.dto.userv2.UserAttributesDto;
import com.stanzaliving.user.dto.userv2.UserDto;
import com.stanzaliving.user.entity.UserEntity;
import com.stanzaliving.user.entity.UserManagerMappingEntity;
import com.stanzaliving.user.entity.UserProfileEntity;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class Userv2ToUserAdapter {
    public UserEntity getUserEntityFromUserv2(UserDto userv2){
        return UserEntity.builder()
                .uuid(userv2.getUserUuid())
                .status(userv2.isStatus())
                .department(userv2.getUserProfileDto().getDepartment())
                .email(userv2.getEmailId())
                .mobile(String.valueOf(userv2.getMobileNumber()))
                .userType(Objects.nonNull(userv2.getUserProfileDto().getOldUserType())?
                        userv2.getUserProfileDto().getOldUserType():
                        userv2.getUserProfileDto().getUserType())
                .isoCode("IN")
                .userProfile(UserProfileEntity.builder()
                        .bloodGroup(userv2.getBloodGroup())
                        .firstName(userv2.getFirstName())
                        .middleName(userv2.getMiddleName())
                        .lastName(userv2.getLastName())
                        .gender(userv2.getGender())
                        .maritalStatus(userv2.getMaritalStatus())
                        .nationality(userv2.getNationality())
                        .address(AddressEntity.builder()
                                .addressLine1(userv2.getUserProfileDto().getAddressLine1())
                                .addressLine2(userv2.getUserProfileDto().getAddressLine2())
                                .build())
                        .build())
                .build();
    }

    public UserManagerMappingEntity getUserManagerMappingFromUserV2(UserAttributesDto userAttributesDto){
        return UserManagerMappingEntity.builder()
                .managerId(userAttributesDto.getManagerUuid())
                .userId(userAttributesDto.getUserUuid())
                .createdBy(userAttributesDto.getChangedBy())
                .build();
    }
}