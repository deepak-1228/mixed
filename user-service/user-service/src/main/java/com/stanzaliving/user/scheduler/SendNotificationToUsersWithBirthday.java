package com.stanzaliving.user.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.core.base.utils.DateUtil;
import com.stanzaliving.core.notificationv2.api.NotificationClientApi;
import com.stanzaliving.notification.dto.NotificationDTO;
import com.stanzaliving.user.service.PayloadService;
import com.stanzaliving.user.service.UserService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class SendNotificationToUsersWithBirthday {
	
	@Value("${jobs.enabled}")
	private boolean jobsEnabled;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private PayloadService payloadService;
	
    @Autowired
    private NotificationClientApi notificationClientApi;

	   @Scheduled(cron = "0 0 0 * * *")
	   public void cronJobSch() {

		 log.info("Send notification to users who have birthday today:: Starting send notifications to users having birthday today Job");
			
			if(jobsEnabled) {
				
				List<String> userIdList = userService.getUserProfileDtoWhoseBirthdayIsToday();
				
				if(userIdList.size() > 0) {
					
					log.info("List is: {}", userIdList.toString());
					
					NotificationDTO payload=payloadService.getNotificationRegistryPayload(userIdList,DateUtil.convertToDate(LocalDateTime.now()));
					log.info("Payload for Notification:{}",payload.getTitle());
					
					ResponseDto<NotificationDTO> saveGenericNotification = notificationClientApi.saveGenericNotification(payload);
			        
					if (saveGenericNotification.isStatus()) {
						NotificationDTO dto = saveGenericNotification.getData();
						log.info("Notification dto is: {}", dto);
					}
				
				}
				
				else {
					
					log.info("No elements found in list.");
					
				}
				
			}
			else {
				log.info("SendNotificationToUsersWhoHaveBirthdayTodayJob:: Jobs is disabled");
			}
			
			log.info("SendNotificationToUsersWhoHaveBirthdayTodayJob:: Finished User Sending Notification to users who have birthday today Job.");
	   }
	
	
	
	
	

}
