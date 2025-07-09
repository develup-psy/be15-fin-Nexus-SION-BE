package com.nexus.sion.feature.notification.command.infrastructure.repository;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nexus.sion.feature.notification.query.dto.NotificationDTO;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SseEmitterRepository {

  private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();
  private final Map<String, NotificationDTO> eventCache = new ConcurrentHashMap<>();

  public SseEmitter save(String emitterId, SseEmitter emitter) {
    emitterMap.put(emitterId, emitter);
    return emitter;
  }

  public void deleteById(String emitterId) {
    emitterMap.remove(emitterId);
    eventCache.remove(emitterId);
  }

  public Map<String, SseEmitter> findAllEmittersStartWithId(String employeeIdentificationNumber) {
    return emitterMap.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(employeeIdentificationNumber))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  public void saveEventCache(String emitterId, NotificationDTO event) {
    eventCache.put(emitterId, event);
  }

  public Map<String, NotificationDTO> findAllEventCacheStartWithId(String employeeIdentificationNumber) {
    return eventCache.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(employeeIdentificationNumber))
        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
  }
}
