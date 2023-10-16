package com.stanzaliving.user.config;

import com.stanzaliving.core.leadservice.client.api.LeadserviceClientApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.stanzaliving.core.client.api.BookingDataControllerApi;
import com.stanzaliving.core.base.http.StanzaRestClient;
import com.stanzaliving.core.notificationv2.api.NotificationClientApi;
import com.stanzaliving.core.transformation.client.api.InternalDataControllerApi;
import com.stanzaliving.core.transformation.client.cache.TransformationCache;

@Configuration
public class ClientConfig {

    @Value("${service.transformationmaster.url}")
    private String transformationUrl;

    @Value("${service.notificationClient.url}")
	private String notificationClientUrl;

    @Value("${service.lead.url}")
    private String leadUrl;

    @Value("${service.booking.url}")
    private String bookingServiceUrl;

    @Bean
    public InternalDataControllerApi internalDataControllerApi() {
        return new InternalDataControllerApi(new StanzaRestClient(transformationUrl));
    }

    @Bean
    public TransformationCache transformationCache(InternalDataControllerApi internalDataControllerApi) {
        return new TransformationCache(internalDataControllerApi);
    }

    @Bean
    public NotificationClientApi notificationClientApi() {
        return new NotificationClientApi(new StanzaRestClient(notificationClientUrl));
    }


    @Bean
    public LeadserviceClientApi LeadserviceClientApi() {
        return new LeadserviceClientApi(new StanzaRestClient(leadUrl));
    }

    @Bean
    public BookingDataControllerApi bookingDataControllerApi() {
        return new BookingDataControllerApi(new StanzaRestClient(bookingServiceUrl));
    }
}
