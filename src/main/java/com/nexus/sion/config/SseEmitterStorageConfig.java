package com.nexus.sion.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Configuration
public class SseEmitterStorageConfig {
  @Bean
  public Map<String, SseEmitter> sseEmitters() {
    return new ConcurrentHashMap<>();
  }
}
