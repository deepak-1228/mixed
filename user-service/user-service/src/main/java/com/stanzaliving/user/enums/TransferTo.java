package com.stanzaliving.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferTo {

    CLUSTER_MANAGER("Cluster Manager"),
    NODAL_OFFICER("Nodal Officer"),
    RESIDENT_CAPTAIN("Resident Captain"),
    STANZA_ACCOUNT("Stanza Account"),
    CITY_HEAD("City Head");

    private final String name;
}
