package com.stanzaliving.user.config;

import java.time.LocalDate;

import com.stanzaliving.core.base.http.StanzaRestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.stanzaliving.core.base.localdate.Java8LocalDateStdDeserializer;
import com.stanzaliving.core.base.localdate.Java8LocalDateStdSerializer;

@Configuration
public class UserConfiguration {

	@Value("${user.thread.pool.size:5}")
	private int userCorePoolSize;

	@Value("${user.thread.max.pool.size:10}")
	private int userMaxPoolSize;

	@Value("${user.thread.keepAliveSeconds:30}")
	private int userKeepAliveSeconds;

	@Value("${user.thread.queue.capacity:100000}")
	private int userQueueCapacity;

	@Value("${service.slack.url}")
	private String slackUrl;

	@Bean(name = "userExecutor")
	public ThreadPoolTaskExecutor userExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(userCorePoolSize);
		executor.setMaxPoolSize(userMaxPoolSize);
		executor.setQueueCapacity(userKeepAliveSeconds);
		executor.setKeepAliveSeconds(userQueueCapacity);

		return executor;
	}

	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);

		SimpleModule module = new SimpleModule();
		module.addSerializer(new Java8LocalDateStdSerializer());
		module.addDeserializer(LocalDate.class, new Java8LocalDateStdDeserializer());

		mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.registerModule(module);

		return mapper;
	}

	@Bean(name = "slackClient")
	public StanzaRestClient slackExceptionClient() {return new StanzaRestClient(slackUrl);}
}