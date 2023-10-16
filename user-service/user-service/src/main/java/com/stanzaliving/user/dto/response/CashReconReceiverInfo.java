package com.stanzaliving.user.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashReconReceiverInfo {

    private String userUuid;

    private String name;

    private String phone;

    private String email;
}
