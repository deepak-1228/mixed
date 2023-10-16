package com.stanzaliving.user.dto.userv2;

import com.stanzaliving.core.user.enums.BloodGroup;
import com.stanzaliving.core.user.enums.Gender;
import com.stanzaliving.core.user.enums.MaritalStatus;
import com.stanzaliving.core.user.enums.Nationality;
import lombok.*;

import java.time.LocalDate;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String userUuid;
    private boolean status;
    private String firstName;
    private String middleName;
    private String lastName;
    private String emailId;
    private String mobileNumber;
    private String isoCode;
    private boolean mobileVerified;
    private boolean emailVerified;
    private LocalDate birthday;
    private MaritalStatus maritalStatus;
    private Nationality nationality;
    private BloodGroup bloodGroup;
    private Gender gender;
    private UserProfileDto userProfileDto;
    private UserAttributesDto userAttributesDto;
}
