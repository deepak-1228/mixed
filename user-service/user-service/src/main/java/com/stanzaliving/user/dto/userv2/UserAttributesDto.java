package com.stanzaliving.user.dto.userv2;


import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAttributesDto {
    private String userUuid;
    private String propertyUuid;
    private String managerUuid;
    private String changedBy;
}
