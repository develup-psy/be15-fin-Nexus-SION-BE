package com.nexus.sion.feature.member.command.application.service;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.UserCreateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.repository.MemberRepository;
import com.nexus.sion.feature.member.util.PasswordValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserCommandService {

  private final ModelMapper modelMapper;
  private final PasswordEncoder passwordEncoder;
  private final MemberRepository memberRepository;

  @Transactional
  public void registerUser(UserCreateRequest request) {
    // 중복 회원 체크 로직
    if (!PasswordValidator.isValid(request.getPassword())) {
      throw new BusinessException(ErrorCode.INVALID_PASSWORD_FORMAT);
    }
    System.out.println(memberRepository.findAll());
    System.out.println("이메일 비교");
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMAIL);
    }
    System.out.println("사번 비교");
    if (memberRepository.existsByEmployeeIdentificationNumber(
        request.getEmployeeIdentificationNumber())) {
      throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMPLOYEE_IDENTIFICATION_NUMBER);
    }

    Member member = modelMapper.map(request, Member.class);
    member.setEncodedPassword(passwordEncoder.encode(request.getPassword()));
    memberRepository.save(member);
  }
}
