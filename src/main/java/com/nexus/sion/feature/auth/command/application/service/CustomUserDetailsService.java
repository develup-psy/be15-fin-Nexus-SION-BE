package com.nexus.sion.feature.auth.command.application.service;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String employeeIdentificationNumber)
      throws UsernameNotFoundException {
    Member member =
        memberRepository
            .findById(employeeIdentificationNumber)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    return new org.springframework.security.core.userdetails.User(
        member.getEmployeeIdentificationNumber(),
        member.getPassword(),
        Collections.singleton(new SimpleGrantedAuthority(member.getRole().name())));
  }
}
