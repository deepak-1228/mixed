package com.stanzaliving.user.config;

import lombok.extern.log4j.Log4j2;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Log4j2
@Configuration
public class RedisConfig {

	@Value("${redis.server.address}")
	private String redisUrl;

	@Value("${redis.database:1}")
	private int redisDatabase;

	@Value("${redis.connection.pool.size:20}")
	private int redisConnectionPool;

	@Value("${redis.connection.pool.size.min:5}")
	private int redisMinConnectionPool;

	@Value("${redis.connection.timeout:5000}")
	private int redisConnectionTimeout;

	@Bean
	public RedissonClient redissonClient() {
		log.info("redisUrl :: {}", redisUrl);
		Config config = new Config();
		config.useSingleServer()
				.setAddress(redisUrl)
				.setDatabase(redisDatabase)
				.setConnectionPoolSize(redisConnectionPool)
				.setConnectionMinimumIdleSize(redisMinConnectionPool)
				.setConnectTimeout(redisConnectionTimeout);
		return Redisson.create(config);
	}
}