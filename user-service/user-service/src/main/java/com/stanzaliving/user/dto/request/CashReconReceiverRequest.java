package com.stanzaliving.user.dto.request;

import com.stanzaliving.user.enums.TransferTo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashReconReceiverRequest {

    private String userUuid;

    private TransferTo transferTo;
}
