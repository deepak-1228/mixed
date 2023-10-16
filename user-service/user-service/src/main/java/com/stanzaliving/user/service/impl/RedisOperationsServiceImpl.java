package com.stanzaliving.user.service.impl;

import com.stanzaliving.user.service.RedisOperationsService;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class RedisOperationsServiceImpl implements RedisOperationsService {

    private static final Logger logger = LoggerFactory.getLogger(RedisOperationsServiceImpl.class);

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Object getFromMap(String mapName, String key) {
        logger.info("Get From Map {} key {}", mapName, key);
        return redissonClient.getMapCache(mapName).get(key);
    }

    @Override
    public boolean deleteFromMap(String mapName, String key) {
        return Objects.nonNull(redissonClient.getMapCache(mapName).remove(key));
    }

    @Override
    public boolean putToMap(String mapName, String key, Object value, long ttl, TimeUnit timeUnit) {
        logger.info("Put to Map {} key {} value {}", mapName, key, value);
        Object object = redissonClient.getMapCache(mapName).put(key, value, ttl, timeUnit);
        return Objects.nonNull(object);
    }

    @Override
    public void flushMap(String mapName) {
        redissonClient.getMapCache(mapName).clear();
    }

    @Override
    public long getSizeofMap(String mapName) {
        return redissonClient.getMap(mapName).size();
    }
}
