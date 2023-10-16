package com.stanzaliving.user.service;

import com.stanzaliving.user.dto.request.CashReconReceiverRequest;
import com.stanzaliving.user.dto.response.CashReconReceiverInfo;
import com.stanzaliving.user.dto.response.NodalOfficerInfo;

import java.util.List;

public interface CashReconService {
    List<CashReconReceiverInfo> getReceiverList(CashReconReceiverRequest cashReceiverRequest);

    List<NodalOfficerInfo> getNodalOfficersList();
}
