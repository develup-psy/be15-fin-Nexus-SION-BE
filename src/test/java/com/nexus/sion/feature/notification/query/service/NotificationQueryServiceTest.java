package com.nexus.sion.feature.notification.query.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.notification.query.dto.NotificationDTO;
import com.nexus.sion.feature.notification.query.repository.NotificationQueryRepository;

class NotificationQueryServiceTest {

  @Mock private NotificationQueryRepository notificationQueryRepository;

  @Mock private MemberRepository memberRepository;

  @InjectMocks private NotificationQueryService notificationQueryService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @DisplayName("특정 회원의 알림 목록을 정상적으로 가져온다")
  @Test
  void testGetNotifications() {
    // given
    String memberId = "member123";
    int page = 0;
    int size = 10;

    List<NotificationDTO> mockNotifications =
        List.of(
            new NotificationDTO(
                1L,
                NotificationType.SQUAD_COMMENT,
                "123L",
                "message1",
                false,
                LocalDateTime.now(),
                "sender1",
                "senderName1",
                "receiver1"),
            new NotificationDTO(
                2L,
                NotificationType.SQUAD_COMMENT,
                "123L",
                "message1",
                false,
                LocalDateTime.now(),
                "sender2",
                "senderName2",
                "receiver2"));

    given(notificationQueryRepository.selectNotifications(page, size, memberId))
        .willReturn(mockNotifications);
    given(notificationQueryRepository.countTotalNotifications(memberId)).willReturn(2L);

    // when
    PageResponse<NotificationDTO> result =
        notificationQueryService.getNotifications(memberId, page, size);

    // then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2L);
    assertThat(result.getContent().get(0).getMessage()).isEqualTo("message1");

    then(notificationQueryRepository).should().selectNotifications(page, size, memberId);
    then(notificationQueryRepository).should().countTotalNotifications(memberId);
  }

  @DisplayName("전체 알림 목록을 정상적으로 가져온다")
  @Test
  void testGetAllNotifications() {
    // given
    int page = 0;
    int size = 5;

    List<NotificationDTO> mockNotifications =
        List.of(
            new NotificationDTO(
                1L,
                NotificationType.SQUAD_COMMENT,
                "123L",
                "message1",
                false,
                LocalDateTime.now(),
                "sender1",
                "senderName1",
                "receiver1"),
            new NotificationDTO(
                2L,
                NotificationType.SQUAD_COMMENT,
                "123L",
                "message1",
                false,
                LocalDateTime.now(),
                "sender2",
                "senderName2",
                "receiver2"));

    given(notificationQueryRepository.selectAllNotifications(page, size))
        .willReturn(mockNotifications);
    given(notificationQueryRepository.countTotalAllNotifications()).willReturn(2L);

    // when
    PageResponse<NotificationDTO> result = notificationQueryService.getAllNotifications(page, size);

    // then
    assertThat(result.getContent()).hasSize(2);
    assertThat(result.getTotalElements()).isEqualTo(2L);
    assertThat(result.getContent().get(1).getMessage()).isEqualTo("message1");

    then(notificationQueryRepository).should().selectAllNotifications(page, size);
    then(notificationQueryRepository).should().countTotalAllNotifications();
  }
}
