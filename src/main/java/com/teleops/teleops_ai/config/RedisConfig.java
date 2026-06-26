package com.teleops.teleops_ai.config;

import com.teleops.teleops_ai.common.util.CacheConstants;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Configuration (Updated with per-cache TTLs)
 *
 * Why per-cache TTLs?
 *   Different data has different staleness tolerance:
 *
 *   dashboard:stats = 60s
 *     A 1-minute lag on dashboard counts is acceptable.
 *
 *   devices:all = 120s
 *     Device inventory changes infrequently.
 *     Engineers don't expect real-time device list updates.
 *
 *   devices = 120s
 *     Individual device details are stable.
 *
 *   alarms:active = 30s
 *     Active alarms are more time-sensitive.
 *     Engineers need to see new alarms quickly.
 *     But 30s lag is still acceptable for a cached view.
 *     The alarm board on the frontend can poll more frequently.
 *
 * withCacheManagerBuilderConfigurer:
 *   Allows each cache to have its own RedisCacheConfiguration.
 *   This gives us fine-grained control over TTL per cache.
 */
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate for manual cache operations.
     * Used in services where @Cacheable is not flexible enough.
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * CacheManager with per-cache TTL configuration.
     *
     * withInitialCacheConfigurations() registers specific
     * TTL configurations for named caches.
     *
     * If a cache name is not listed here, it falls back
     * to the defaultConfig (60 seconds).
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory) {

        // Default configuration for unspecified caches
        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofSeconds(60))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(new StringRedisSerializer()))
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(
                                                new GenericJackson2JsonRedisSerializer()));

        // Per-cache TTL configurations
        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // Dashboard stats: 60 seconds
        cacheConfigs.put(
                CacheConstants.DASHBOARD_STATS,
                defaultConfig.entryTtl(Duration.ofSeconds(60))
        );

        // All devices list: 120 seconds
        cacheConfigs.put(
                CacheConstants.DEVICES_ALL,
                defaultConfig.entryTtl(Duration.ofSeconds(120))
        );

        // Individual devices: 120 seconds
        cacheConfigs.put(
                CacheConstants.DEVICES,
                defaultConfig.entryTtl(Duration.ofSeconds(120))
        );

        // Active alarms: 30 seconds (fresher data needed)
        cacheConfigs.put(
                CacheConstants.ALARMS_ACTIVE,
                defaultConfig.entryTtl(Duration.ofSeconds(30))
        );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}