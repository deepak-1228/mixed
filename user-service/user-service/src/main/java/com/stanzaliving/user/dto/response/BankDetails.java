package com.stanzaliving.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetails {

    private String bankName;

    private String accountNumber;

    private String address;

    private String ifscCode;
}
