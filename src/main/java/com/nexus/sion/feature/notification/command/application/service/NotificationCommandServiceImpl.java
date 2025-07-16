package com.nexus.sion.feature.notification.command.application.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import jakarta.annotation.PreDestroy;
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
  private final Map<String, ScheduledFuture<?>> pingFutures = new ConcurrentHashMap<>();

  private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 1시간

  @Override
  @Transactional
  public void createAndSendNotification(
      String senderId,
      String receiverId,
      String message,
      NotificationType type,
      String linkedContentId) {

    // null 값 safe 처리
    String senderName =
        senderId != null
            ? memberRepository
                .findEmployeeNameByEmployeeIdentificationNumber(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND))
            : "";

    String notificationMessage = message == null ? type.generateMessage(senderName) : message;
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
          cancelPing(emitterId);
          log.info("onCompletion - emitter 정리 완료: {}", emitterId);
        });

    emitter.onTimeout(
        () -> {
          log.info("onTimeout 발생: {}", emitterId);
          emitter.complete();
        });

    emitter.onError(
        (e) -> {
          if (e instanceof IOException) {
            log.info("✅ onError - SSE 연결 끊김: {}", emitterId);
          } else {
            log.warn("⚠️ onError - 예기치 않은 오류: {}", emitterId, e);
          }
          emitter.complete();
        });

    sendToClient(
        emitterId,
        emitter,
        "initial-connect",
        "알림 서버 연결 성공. [memberId = " + employeeIdentificationNumber + "]");

    // ping 이벤트 30초마다 보내기
    ScheduledFuture<?> pingFuture =
        scheduler.scheduleAtFixedRate(
            () -> {
              try {
                emitter.send(SseEmitter.event().name("ping").data("ping"));
              } catch (IOException | IllegalStateException e) {
                log.info("✅ ping 전송 실패 - emitter 정리 시작: {}", emitterId);
                emitter.complete();
              }
            },
            30,
            30,
            TimeUnit.SECONDS);

    pingFutures.put(emitterId, pingFuture);

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

  @Transactional
  @Override
  public Void readAllNotification(String employeeIdentificationNumber) {
    notificationRepository.markAllAsRead(employeeIdentificationNumber);
    return null;
  }

  @Transactional
  @Override
  public Void readNotification(String employeeIdentificationNumber, Long notificationId) {
    Notification notification =
        notificationRepository
            .findByReceiverIdAndNotificationId(employeeIdentificationNumber, notificationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

    notification.setHasRead();
    notificationRepository.save(notification);
    return null;
  }

  @Transactional
  @Override
  public void sendSquadShareNotification(
      String senderId, List<String> receivers, String squadCode) {

    final String senderName =
        senderId != null
            ? memberRepository
                .findEmployeeNameByEmployeeIdentificationNumber(senderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_INFO_NOT_FOUND))
            : "";

    final String notificationMessage = NotificationType.SQUAD_SHARE.generateMessage(senderName);

    List<Notification> notificationsToSave =
        receivers.stream()
            .map(
                receiverId ->
                    Notification.builder()
                        .senderId(senderId)
                        .receiverId(receiverId)
                        .message(notificationMessage)
                        .notificationType(NotificationType.SQUAD_SHARE)
                        .linkedContentId(squadCode)
                        .build())
            .toList();

    // 모두 저장
    List<Notification> savedNotifications = notificationRepository.saveAll(notificationsToSave);

    // SSE emiter 로 send
    savedNotifications.forEach(
        notification -> {
          NotificationDTO dto = modelMapper.map(notification, NotificationDTO.class);
          dto.setSenderName(senderName);
          send(notification.getReceiverId(), dto);
        });
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
      log.info("✅ SSE 연결 끊김으로 emitter 정리 시작: emitterId={}, reason={}", emitterId, e.getMessage());
      emitter.complete();
    } catch (Exception e) {
      log.error(
          "🚨 SSE 예기치 않은 오류로 emitter 정리 시작: emitterId={}, error={}", emitterId, e.getMessage(), e);
      emitter.complete();
    }
  }

  private void cancelPing(String emitterId) {
    ScheduledFuture<?> future = pingFutures.remove(emitterId);
    if (future != null) {
      future.cancel(true);
    }
  }

  @PreDestroy
  public void shutdown() {
    scheduler.shutdown();
    log.info("✅ SSE Ping Scheduler 종료");
  }
}
