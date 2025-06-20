package com.nexus.sion.feature.member.command.application.service;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.repository.MemberRepository;
import com.nexus.sion.feature.member.util.Validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberCommandService {

  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  @Transactional
  public void registerUser(MemberCreateRequest request) {
    // 핸드폰번호 체크 로직
    if (!Validator.isPhonenumberValid(request.getPhoneNumber())) {
      throw new BusinessException(ErrorCode.INVALID_PHONE_NUMBER_FORMAT);
    }
    // 이메일 형식 체크 로직
    if (!Validator.isEmailValid(request.getEmail())) {
      throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
    }
    // 비밀번호 형식 체크 로직
    if (!Validator.isPasswordValid(request.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT);
    }
    // 중복 회원 체크 로직
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMAIL);
    }
    if (memberRepository.existsByEmployeeIdentificationNumber(
        request.getEmployeeIdentificationNumber())) {
      throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMPLOYEE_IDENTIFICATION_NUMBER);
    }

    Member member = modelMapper.map(request, Member.class);
    member.setEncodedPassword(passwordEncoder.encode(request.getPassword()));
    member.setAdminRole();
    memberRepository.save(member);
  }
}
