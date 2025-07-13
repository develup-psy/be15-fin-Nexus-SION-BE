package com.nexus.sion.feature.notification.command.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.notification.command.domain.aggregate.Notification;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.notification.command.domain.repository.NotificationRepository;
import com.nexus.sion.feature.notification.command.infrastructure.repository.SseEmitterRepository;
import com.nexus.sion.feature.notification.query.dto.NotificationDTO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
        ModelMapper modelMapper = mock(ModelMapper.class);
        modelMapper = spy(new ModelMapper()); // 여기서 spy로 변경

        service = new NotificationCommandServiceImpl(
                notificationRepository,
                sseEmitterRepository,
                memberRepository,
                modelMapper
        );
    }

    @Test
    void createAndSendNotification_성공() {
        // given
        String senderId = "sender1";
        String receiverId = "receiver1";
        String linkedId = "content1";

        when(memberRepository.findEmployeeNameByEmployeeIdentificationNumber(senderId))
                .thenReturn(Optional.of("홍길동"));

        Notification dummy = Notification.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .notificationType(NotificationType.SQUAD_COMMENT)
                .message("댓글이 등록되었습니다.")
                .linkedContentId(linkedId)
                .build();

        when(notificationRepository.save(any())).thenReturn(dummy);
        when(sseEmitterRepository.findAllEmittersStartWithId(receiverId))
                .thenReturn(new ConcurrentHashMap<>());

        // when
        service.createAndSendNotification(senderId, receiverId, null, NotificationType.SQUAD_COMMENT, linkedId);

        // then
        verify(notificationRepository, times(1)).save(any());
    }

    @Test
    void createAndSendNotification_유저없음_예외() {
        // given
        when(memberRepository.findEmployeeNameByEmployeeIdentificationNumber("sender"))
                .thenReturn(Optional.empty());

        // then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.createAndSendNotification("sender", "receiver", null, NotificationType.SQUAD_COMMENT, "id")
        );
        assertEquals(ErrorCode.USER_INFO_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void subscribe_성공_초기_연결_및_ping_테스트() {
        // given
        String memberId = "user1";
        when(sseEmitterRepository.save(anyString(), any(SseEmitter.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        SseEmitter emitter = service.subscribe(memberId, "");

        // then
        assertNotNull(emitter);
        verify(sseEmitterRepository, times(1)).save(anyString(), any(SseEmitter.class));
    }

    @Test
    void subscribe_이전_이벤트_재전송_성공() {
        // given
        String memberId = "user1";
        String lastEventId = memberId + "_uuid_" + (System.currentTimeMillis() - 1000);

        NotificationDTO dto = new NotificationDTO();
        Map<String, NotificationDTO> cachedEvents = Map.of(
                memberId + "_uuid_" + (System.currentTimeMillis()), dto
        );

        when(sseEmitterRepository.save(anyString(), any(SseEmitter.class)))
                .thenReturn(new SseEmitter(10000L));
        when(sseEmitterRepository.findAllEventCacheStartWithId(memberId))
                .thenReturn(cachedEvents);

        // when
        SseEmitter emitter = service.subscribe(memberId, lastEventId);

        // then
        assertNotNull(emitter);
        verify(sseEmitterRepository, times(1)).findAllEventCacheStartWithId(memberId);
    }

    @Test
    void readAllNotification_성공() {
        // when
        service.readAllNotification("user1");

        // then
        verify(notificationRepository, times(1)).markAllAsRead("user1");
    }

    @Test
    void readNotification_성공() {
        // given
        Notification noti = Notification.builder()
                .senderId("sender")
                .receiverId("user1")
                .message("msg")
                .notificationType(NotificationType.SQUAD_COMMENT)
                .build();

        when(notificationRepository.findByReceiverIdAndNotificationId("user1", 1L))
                .thenReturn(Optional.of(noti));

        // when
        service.readNotification("user1", 1L);

        // then
        verify(notificationRepository, times(1)).save(noti);
    }

    @Test
    void readNotification_실패_존재하지_않는_알림() {
        // given
        when(notificationRepository.findByReceiverIdAndNotificationId("user1", 99L))
                .thenReturn(Optional.empty());

        // then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.readNotification("user1", 99L)
        );

        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void sendSquadShareNotification_성공() {
        // given
        String senderId = "sender1";
        List<String> receivers = List.of("recv1", "recv2");
        String squadCode = "SQ123";

        when(memberRepository.findEmployeeNameByEmployeeIdentificationNumber(senderId))
                .thenReturn(Optional.of("홍길동"));

        List<Notification> dummyList = receivers.stream()
                .map(id -> Notification.builder()
                        .senderId(senderId)
                        .receiverId(id)
                        .notificationType(NotificationType.SQUAD_SHARE)
                        .message("홍길동님이 스쿼드를 공유했습니다.")
                        .linkedContentId(squadCode)
                        .build())
                .toList();

        when(notificationRepository.saveAll(anyList())).thenReturn(dummyList);
        when(sseEmitterRepository.findAllEmittersStartWithId(anyString()))
                .thenReturn(new ConcurrentHashMap<>());

        // when
        service.sendSquadShareNotification(senderId, receivers, squadCode);

        // then
        verify(notificationRepository, times(1)).saveAll(anyList());
    }
}
