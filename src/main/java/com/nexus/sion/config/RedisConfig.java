package com.nexus.sion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.nexus.sion.feature.auth.command.domain.aggregate.RefreshToken;

@Configuration
public class RedisConfig {

  @Value("${spring.data.redis.host}")
  private String redisHost;

  @Value("${spring.data.redis.port}")
  private String redisPort;

  @Value("${spring.data.redis.password}")
  private String redisPassword;

  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    // Redis 서버와 연결
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();

    redisStandaloneConfiguration.setHostName(redisHost);
    redisStandaloneConfiguration.setPort(Integer.parseInt(redisPort));
    redisStandaloneConfiguration.setPassword(redisPassword);

    return new LettuceConnectionFactory(redisStandaloneConfiguration);
  }

  @Bean
  public RedisTemplate<String, RefreshToken> redisTemplate() {
    // RedisTemplate을 RefreshToken 전용으로 만들어 등록
    RedisTemplate<String, RefreshToken> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory());
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    //        redisTemplate.setValueSerializer(new StringRedisSerializer());

    // 값은 JSON 형태로 직렬화
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    return redisTemplate;
  }
}
