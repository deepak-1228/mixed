package com.stanzaliving.user.service;

import java.util.Date;
import java.util.List;

import com.stanzaliving.notification.dto.NotificationDTO;

public interface PayloadService {

	 NotificationDTO getNotificationRegistryPayload(List<String> userList, Date scheduledDate);
	
}
