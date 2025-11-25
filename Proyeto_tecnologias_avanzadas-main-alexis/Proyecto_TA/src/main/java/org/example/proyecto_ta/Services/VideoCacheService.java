package org.example.proyecto_ta.Services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class VideoCacheService {

    private final RedisTemplate<String, byte[]> redisTemplateBytes;

    public VideoCacheService(RedisTemplate<String, byte[]> redisTemplateBytes) {
        this.redisTemplateBytes = redisTemplateBytes;
    }

    public void putVideoBytes(String key, byte[] bytes, Duration ttl) {
        if (key == null || bytes == null) return;
        if (ttl == null) {
            redisTemplateBytes.opsForValue().set(key, bytes);
        } else {
            redisTemplateBytes.opsForValue().set(key, bytes, ttl.toMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public byte[] getVideoBytes(String key) {
        if (key == null) return null;
        return redisTemplateBytes.opsForValue().get(key);
    }

    public void evict(String key) {
        if (key == null) return;
        redisTemplateBytes.delete(key);
    }
}
