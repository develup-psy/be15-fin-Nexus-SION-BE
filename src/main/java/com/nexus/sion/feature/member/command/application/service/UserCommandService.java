package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.util.Validator;
import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.UserCreateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandService {

  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  @Transactional
  public void registerUser(UserCreateRequest request) {
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
