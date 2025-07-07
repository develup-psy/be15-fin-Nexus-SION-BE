package com.nexus.sion.feature.notification.command.infrastructure.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class SseEmitterRepository {

  private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
  private final Map<String, Object> eventCache = new ConcurrentHashMap<>();

  public SseEmitter save(String emitterId, SseEmitter emitter) {
    emitterMap.put(emitterId, emitter);
    return emitter;
  }

  public void deleteById(String emitterId) {
    emitterMap.remove(emitterId);
    eventCache.remove(emitterId);
  }

  public Map<String, SseEmitter> findAllEmittersStartWithId(String employeeIdentificationNumber) {
    Map<String, SseEmitter> result = new HashMap<>();
    emitterMap.forEach(
        (key, emitter) -> {
          if (key.startsWith(employeeIdentificationNumber)) {
            result.put(key, emitter);
          }
        });
    return result;
  }

  public void saveEventCache(String emitterId, Object event) {
    eventCache.put(emitterId, event);
  }

  public Map<String, Object> findAllEventCacheStartWithId(String employeeIdentificationNumber) {
    Map<String, Object> result = new HashMap<>();
    eventCache.forEach(
        (key, event) -> {
          if (key.startsWith(employeeIdentificationNumber)) {
            result.put(key, event);
          }
        });
    return result;
  }
}
