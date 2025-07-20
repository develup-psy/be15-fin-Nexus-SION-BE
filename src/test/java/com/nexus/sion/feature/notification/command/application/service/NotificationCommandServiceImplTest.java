package com.nexus.sion.feature.notification.command.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.notification.command.domain.aggregate.Notification;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.notification.command.domain.repository.NotificationRepository;
import com.nexus.sion.feature.notification.command.infrastructure.repository.SseEmitterRepository;
import com.nexus.sion.feature.notification.query.dto.NotificationDTO;

public class NotificationCommandServiceImplTest {

  private NotificationRepository notificationRepository;
  private SseEmitterRepository sseEmitterRepository;
  private MemberRepository memberRepository;
  private NotificationCommandServiceImpl service;

  @BeforeEach
  void setUp() {
    notificationRepository = mock(NotificationRepository.class);
    sseEmitterRepository = mock(SseEmitterRepository.class);
    memberRepository = mock(MemberRepository.class);
    ModelMapper modelMapper = spy(new ModelMapper());

    service = new NotificationCommandServiceImpl(
            notificationRepository, sseEmitterRepository, memberRepository, modelMapper);
  }

  @Test
  void createAndSendNotification_성공() {
    String senderId = "sender1";
    String receiverId = "receiver1";
    String linkedId = "content1";

    when(memberRepository.findEmployeeNameByEmployeeIdentificationNumber(senderId))
            .thenReturn(Optional.of("홍길동"));
    when(notificationRepository.save(any())).thenReturn(mock(Notification.class));
    when(sseEmitterRepository.findAllEmittersStartWithId(receiverId))
            .thenReturn(new ConcurrentHashMap<>());

    service.createAndSendNotification(
            senderId, receiverId, null, NotificationType.SQUAD_COMMENT, linkedId);

    verify(notificationRepository, times(1)).save(any());
  }

  @Test
  void createAndSendNotification_유저없음_예외() {
    when(memberRepository.findEmployeeNameByEmployeeIdentificationNumber("sender"))
            .thenReturn(Optional.empty());

    BusinessException exception = assertThrows(BusinessException.class,
            () -> service.createAndSendNotification(
                    "sender", "receiver", null, NotificationType.SQUAD_COMMENT, "id"));

    assertEquals(ErrorCode.USER_INFO_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void subscribe_성공_초기_연결() {
    when(sseEmitterRepository.save(anyString(), any(SseEmitter.class)))
            .thenAnswer(invocation -> invocation.getArgument(1));

    SseEmitter emitter = service.subscribe("user1", "");

    assertNotNull(emitter);
    verify(sseEmitterRepository).save(anyString(), any(SseEmitter.class));
  }

  @Test
  void subscribe_이전_이벤트_재전송() {
    String memberId = "user1";
    String lastEventId = memberId + "_uuid_123";

    NotificationDTO dto = new NotificationDTO();
    Map<String, NotificationDTO> cachedEvents = Map.of("user1_uuid_999", dto);

    when(sseEmitterRepository.save(anyString(), any(SseEmitter.class)))
            .thenReturn(new SseEmitter(10000L));
    when(sseEmitterRepository.findAllEventCacheStartWithId(memberId)).thenReturn(cachedEvents);

    SseEmitter emitter = service.subscribe(memberId, lastEventId);

    assertNotNull(emitter);
    verify(sseEmitterRepository).findAllEventCacheStartWithId(memberId);
  }

  @Test
  void readAllNotification_성공() {
    service.readAllNotification("user1");
    verify(notificationRepository).markAllAsRead("user1");
  }

  @Test
  void readNotification_성공() {
    Notification noti = Notification.builder()
            .senderId("sender").receiverId("user1").message("msg")
            .notificationType(NotificationType.SQUAD_COMMENT).build();

    when(notificationRepository.findByReceiverIdAndNotificationId("user1", 1L))
            .thenReturn(Optional.of(noti));

    service.readNotification("user1", 1L);

    verify(notificationRepository).save(noti);
  }

  @Test
  void readNotification_실패_존재하지_않는_알림() {
    when(notificationRepository.findByReceiverIdAndNotificationId("user1", 99L))
            .thenReturn(Optional.empty());

    BusinessException exception =
            assertThrows(BusinessException.class, () -> service.readNotification("user1", 99L));

    assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, exception.getErrorCode());
  }

  @Test
  void sendSquadShareNotification_성공() {
    String senderId = "sender1";
    List<String> receivers = List.of("recv1", "recv2");
    String squadCode = "SQ123";

    when(memberRepository.findEmployeeNameByEmployeeIdentificationNumber(senderId))
            .thenReturn(Optional.of("홍길동"));
    when(notificationRepository.saveAll(anyList())).thenReturn(List.of());

    service.sendSquadShareNotification(senderId, receivers, squadCode);

    verify(notificationRepository).saveAll(anyList());
  }

  @Test
  void send_알림전송_성공() {
    String employeeId = "user1";
    NotificationDTO dto = new NotificationDTO();

    SseEmitter emitter = mock(SseEmitter.class);
    Map<String, SseEmitter> emitters = Map.of("user1_uuid", emitter);

    when(sseEmitterRepository.findAllEmittersStartWithId(employeeId)).thenReturn(emitters);

    service.send(employeeId, dto);

    verify(sseEmitterRepository).saveEventCache(any(), eq(dto));
  }

  @Test
  void sendToClient_IOException_발생() throws IOException {
    // given
    SseEmitter emitter = mock(SseEmitter.class);

    // 정확한 타입 매칭: SseEmitter.SseEventBuilder 사용
    doThrow(new IOException())
            .when(emitter)
            .send(any(SseEmitter.SseEventBuilder.class));

    Map<String, SseEmitter> emitters = Map.of("user1_uuid", emitter);
    when(sseEmitterRepository.findAllEmittersStartWithId("user1")).thenReturn(emitters);

    // when
    service.send("user1", new NotificationDTO());

    // then
    verify(emitter).complete(); // emitter.send() 실패 시 complete() 호출 검증
  }

  @Test
  void sendToClient_RuntimeException_발생() throws IOException {
    // given
    SseEmitter emitter = mock(SseEmitter.class);

    // 정확한 타입으로 RuntimeException을 던지도록 설정
    doThrow(new RuntimeException("테스트용 런타임 예외"))
            .when(emitter)
            .send(any(SseEmitter.SseEventBuilder.class));

    Map<String, SseEmitter> emitters = Map.of("user1_uuid", emitter);
    when(sseEmitterRepository.findAllEmittersStartWithId("user1")).thenReturn(emitters);

    // when
    service.send("user1", new NotificationDTO());

    // then
    verify(emitter).complete();
  }


  @Test
  void cancelPing_정상동작() throws Exception {
    ScheduledFuture<?> mockFuture = mock(ScheduledFuture.class);

    Field pingFuturesField = NotificationCommandServiceImpl.class.getDeclaredField("pingFutures");
    pingFuturesField.setAccessible(true);
    Map<String, ScheduledFuture<?>> pingFutures = (Map<String, ScheduledFuture<?>>) pingFuturesField.get(service);
    pingFutures.put("user1-emitter", mockFuture);

    Method cancelPingMethod = NotificationCommandServiceImpl.class.getDeclaredMethod("cancelPing", String.class);
    cancelPingMethod.setAccessible(true);
    cancelPingMethod.invoke(service, "user1-emitter");

    verify(mockFuture).cancel(true);
  }

  @Test
  void shutdown_호출_성공() {
    service.shutdown();
    assertTrue(true);
  }
}
