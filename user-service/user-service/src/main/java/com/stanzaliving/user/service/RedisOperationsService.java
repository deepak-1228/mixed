package com.stanzaliving.user.service;

import java.util.concurrent.TimeUnit;

public interface RedisOperationsService {

    Object getFromMap(String mapName, String key);

    boolean deleteFromMap(String mapName, String key);

    boolean putToMap(String mapName, String key, Object value, long ttl, TimeUnit timeUnit);

    void flushMap(String mapName);

    long getSizeofMap(String mapName);
}