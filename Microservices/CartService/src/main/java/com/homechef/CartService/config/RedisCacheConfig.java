package com.homechef.CartService.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homechef.CartService.model.Cart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Handles Java 8 types like LocalDateTime
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL); // Optional, but helpful

        Jackson2JsonRedisSerializer<Cart> cartSerializer = new Jackson2JsonRedisSerializer<>(Cart.class);
        cartSerializer.setObjectMapper(objectMapper);

        Jackson2JsonRedisSerializer<String> stringSerializer = new Jackson2JsonRedisSerializer<>(String.class);
        stringSerializer.setObjectMapper(objectMapper);

        Jackson2JsonRedisSerializer<Object> defaultSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        defaultSerializer.setObjectMapper(objectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(defaultSerializer));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put("cartCache",
                defaultConfig.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(cartSerializer)));
        cacheConfigurations.put("user_cart_map",
                defaultConfig.serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(stringSerializer)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}