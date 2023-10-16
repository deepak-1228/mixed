package com.stanzaliving.user.service.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.stanzaliving.notification.dto.NotificationDTO;
import com.stanzaliving.notification.dto.UserParams;
import com.stanzaliving.user.service.PayloadService;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class PayloadServiceImpl implements PayloadService {
	
	@Value("${application.name}")
    private String appName;
	
	@Value("${birthday.deep-link.url}")
    private String deepLinkUrl;
	
	@Value("${birthday.notification.title}")
    private String title;
	
	@Value("${birthday.notification.description}")
    private String message;
	
	
	@Override
	public NotificationDTO getNotificationRegistryPayload(List<String> userList, Date scheduledDate) {
		
		List<UserParams> userParamsList=userList.stream().map(user -> {
            UserParams userParams=new UserParams();
            userParams.setUserId(user);
            return userParams;
        }).collect(Collectors.toList());

        Map<String,String> data=new LinkedHashMap<>();
        data.put("deepLinkUrl", deepLinkUrl);
        data.put("source","SNS");

        log.info("Adding Payload for Notifications");
        return NotificationDTO.builder()
                .appName(appName)
                .callToActionUrl(null)
                .title(title)
                .timeToLive(5L)
                .date(scheduledDate)
                .message(message)
                .userParamsList(userParamsList)
                .data(data)
                .build();
	}

}
