package com.nexus.sion.feature.auth.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;

class CustomUserDetailsServiceTest {

    private MemberRepository memberRepository;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        memberRepository = mock(MemberRepository.class);
        service = new CustomUserDetailsService(memberRepository);
    }

    @Test
    @DisplayName("loadUserByUsername - 정상적으로 사용자 정보를 반환한다")
    void loadUserByUsername_success() {
        // given
        Member member = Member.builder()
                .employeeIdentificationNumber("EMP001")
                .password("encodedPassword")
                .role(MemberRole.INSIDER)
                .build();

        when(memberRepository.findById("EMP001")).thenReturn(Optional.of(member));

        // when
        UserDetails userDetails = service.loadUserByUsername("EMP001");

        // then
        assertThat(userDetails.getUsername()).isEqualTo("EMP001");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("INSIDER");
    }

    @Test
    @DisplayName("loadUserByUsername - 존재하지 않는 사용자 예외 처리")
    void loadUserByUsername_userNotFound() {
        // given
        when(memberRepository.findById("EMP002")).thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.loadUserByUsername("EMP002"));

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
