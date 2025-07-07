package com.nexus.sion.feature.notification.command.application.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import jakarta.transaction.Transactional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.notification.command.domain.aggregate.Notification;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.notification.command.domain.repository.NotificationRepository;
import com.nexus.sion.feature.notification.command.infrastructure.repository.SseEmitterRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCommandServiceImpl implements NotificationCommandService {

  private final NotificationRepository notificationRepository;
  private final SseEmitterRepository sseEmitterRepository;
  private final MemberRepository memberRepository;

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간

  @Override
  @Transactional
  public void createAndSendNotification(
      String senderId,
      String receiverId,
      String message,
      NotificationType type,
      String linkedContentId) {

    Notification notification =
        Notification.builder()
            .senderId(senderId)
            .receiverId(receiverId)
            .message(message)
            .notificationType(type)
            .linkedContentId(linkedContentId)
            .build();

    Notification saved = notificationRepository.save(notification);
    Long id = saved.getNotificationId();

    String senderName =
        memberRepository
            .findEmployeeNameByEmployeeIdentificationNumber(senderId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND));

    /* Query DTO와 구조 같아야 함 */
    /* TODO :  쿼리쪽 응답 response dto 로 대체 */
    Map<String, Object> payload =
        Map.of(
            "notificationId", id,
            "senderId", senderId,
            "senderName", senderName,
            "receiverId", receiverId,
            "message", message,
            "notificationType", type,
            "linkedContentId", linkedContentId,
            "isRead", false,
            "createdAt", LocalDateTime.now());

    send(receiverId, payload);
  }

  @Override
  public SseEmitter subscribe(String employeeIdentificationNumber, String lastEventId) {

    String emitterId =
        employeeIdentificationNumber + "_" + UUID.randomUUID() + "_" + System.currentTimeMillis();
    SseEmitter emitter = sseEmitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

    emitter.onCompletion(() -> sseEmitterRepository.deleteById(emitterId));
    emitter.onTimeout(() -> sseEmitterRepository.deleteById(emitterId));
    emitter.onError((e) -> sseEmitterRepository.deleteById(emitterId));

    sendToClient(
        emitterId, emitter, "알림 서버 연결 성공. [memberId = " + employeeIdentificationNumber + "]");

    if (!lastEventId.isEmpty()) {
      Map<String, Object> events =
          sseEmitterRepository.findAllEventCacheStartWithId(employeeIdentificationNumber);
      events.entrySet().stream()
          .filter(
              entry -> {
                String eventKey = entry.getKey(); // "{memberId}_{uuid}_{timestamp}"
                String[] parts = eventKey.split("_");
                String[] lastParts = lastEventId.split("_");

                if (parts.length < 3 || lastParts.length < 3) {
                  return false;
                }

                try {
                  long eventTimestamp = Long.parseLong(parts[2]);
                  long lastTimestamp = Long.parseLong(lastParts[2]);

                  return eventTimestamp > lastTimestamp;
                } catch (NumberFormatException e) {
                  return false; // 파싱 실패한 경우도 무시
                }
              })
          .forEach(entry -> sendToClient(entry.getKey(), emitter, entry.getValue()));
    }

    return emitter;
  }

  @Async
  public void send(String employeeIdentificationNumber, Object data) {
    Map<String, SseEmitter> emitters =
        sseEmitterRepository.findAllEmittersStartWithId(employeeIdentificationNumber);
    emitters.forEach(
        (emitterId, emitter) -> {
          sseEmitterRepository.saveEventCache(emitterId, data);
          sendToClient(emitterId, emitter, data);
        });
  }

  private void sendToClient(String emitterId, SseEmitter emitter, Object data) {
    try {
      emitter.send(SseEmitter.event().id(emitterId).name("sse").data(data));
    } catch (IOException e) {
      sseEmitterRepository.deleteById(emitterId);
      log.error("SSE 연결 오류: emitterId={}, error={}", emitterId, e.getMessage());
    }
  }
}
