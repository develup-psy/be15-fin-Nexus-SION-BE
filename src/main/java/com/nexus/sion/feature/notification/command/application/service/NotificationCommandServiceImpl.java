package com.nexus.sion.feature.notification.command.application.service;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
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
import com.nexus.sion.feature.notification.query.dto.NotificationDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationCommandServiceImpl implements NotificationCommandService {

  private final NotificationRepository notificationRepository;
  private final SseEmitterRepository sseEmitterRepository;
  private final MemberRepository memberRepository;
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final ModelMapper modelMapper;

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간

  @Override
  @Transactional
  public void createAndSendNotification(
      String senderId, String receiverId, String message, NotificationType type, String linkedContentId) {

    // null 값 safe 처리
    String senderName =
        senderId != null
            ? memberRepository
                .findEmployeeNameByEmployeeIdentificationNumber(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND))
            : "";

    String notificationMessage = message == null? type.generateMessage(senderName) : message;
    Notification notification =
        Notification.builder()
            .senderId(senderId)
            .receiverId(receiverId)
            .message(notificationMessage)
            .notificationType(type)
            .linkedContentId(linkedContentId)
            .build();

    Notification saved = notificationRepository.save(notification);

    NotificationDTO dto = modelMapper.map(saved, NotificationDTO.class);
    dto.setSenderName(senderName); // Entity에 없으니 수동으로

    send(receiverId, dto);
  }

  @Override
  public SseEmitter subscribe(String employeeIdentificationNumber, String lastEventId) {

    String emitterId =
        employeeIdentificationNumber + "_" + UUID.randomUUID() + "_" + System.currentTimeMillis();
    SseEmitter emitter = sseEmitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

    emitter.onCompletion(
        () -> {
          sseEmitterRepository.deleteById(emitterId);
          System.out.println("onCompletion - emitter 삭제: " + emitterId);
        });

    emitter.onTimeout(
        () -> {
          sseEmitterRepository.deleteById(emitterId);
          System.out.println("onTimeout - emitter 삭제: " + emitterId);
        });

    emitter.onError(
        (e) -> {
          sseEmitterRepository.deleteById(emitterId);
          System.out.println("onError - emitter 삭제: " + emitterId);
        });

    sendToClient(
        emitterId,
        emitter,
        "initial-connect",
        "알림 서버 연결 성공. [memberId = " + employeeIdentificationNumber + "]");

    // ping 이벤트 30초마다 보내기
    scheduler.scheduleAtFixedRate(
        () -> {
          try {
            emitter.send(SseEmitter.event().name("ping").data("ping"));
          } catch (IOException e) {
            sseEmitterRepository.deleteById(emitterId);
            System.out.println("ping 전송 실패 - emitter 삭제: " + emitterId);
          }
        },
        30,
        30,
        TimeUnit.SECONDS);

    // 기존 last event 복구 로직
    if (!lastEventId.isEmpty()) {
      Map<String, NotificationDTO> events =
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
          .forEach(entry -> sendToClient(entry.getKey(), emitter, "sse", entry.getValue()));
    }

    return emitter;
  }

  @Async
  public void send(String employeeIdentificationNumber, NotificationDTO data) {
    Map<String, SseEmitter> emitters =
        sseEmitterRepository.findAllEmittersStartWithId(employeeIdentificationNumber);
    emitters.forEach(
        (emitterId, emitter) -> {
          sseEmitterRepository.saveEventCache(emitterId, data);
          sendToClient(emitterId, emitter, "sse", data);
        });
  }

  private void sendToClient(String emitterId, SseEmitter emitter, String name, Object data) {
    try {
      emitter.send(SseEmitter.event().id(emitterId).name(name).data(data));
    } catch (IOException e) {
      sseEmitterRepository.deleteById(emitterId);
      log.error("SSE 연결 오류: emitterId={}, error={}", emitterId, e.getMessage());
    }
  }
}
