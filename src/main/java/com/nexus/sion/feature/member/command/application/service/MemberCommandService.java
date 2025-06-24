package com.nexus.sion.feature.member.command.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.MemberAddRequest;
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.InitialScore;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.DepartmentRepository;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackRepository;
import com.nexus.sion.feature.member.command.domain.repository.InitialScoreRepository;
import com.nexus.sion.feature.member.command.domain.repository.PositionRepository;
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
  private final DepartmentRepository departmentRepository;
  private final PositionRepository positionRepository;
  private final DeveloperTechStackRepository developerTechStackRepository;
  private final InitialScoreRepository initialScoreRepository;

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

  @Transactional
  public void addMembers(List<MemberAddRequest> requests) {
    for (MemberAddRequest request : requests) {

      // Position 검증
      if (request.positionName() != null
          && !positionRepository.existsById(request.positionName())) {
        throw new BusinessException(ErrorCode.POSITION_NOT_FOUND);
      }

      // Department 검증
      if (request.departmentName() != null
          && !departmentRepository.existsById(request.departmentName())) {
        throw new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
      }

      // 이메일 형식 체크 로직
      if (!Validator.isEmailValid(request.email())) {
        throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
      }

      // 중복 이메일 체크 로직
      if (memberRepository.existsByEmail(request.email())) {
        throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMAIL);
      }

      // 핸드폰번호 체크 로직
      if (!Validator.isPhonenumberValid(request.phoneNumber())) {
        throw new BusinessException(ErrorCode.INVALID_PHONE_NUMBER_FORMAT);
      }

      // 중복 사번 체크 로직
      if (memberRepository.existsByEmployeeIdentificationNumber(
          request.employeeIdentificationNumber())) {
        throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMPLOYEE_IDENTIFICATION_NUMBER);
      }

      // 생일 값 검증
      LocalDate birthday = request.birthday();
      if (birthday == null) {
        throw new BusinessException(ErrorCode.INVALID_BIRTHDAY);
      }
      if (birthday.isAfter(LocalDate.now())) {
        throw new BusinessException(ErrorCode.INVALID_BIRTHDAY);
      }
      if (birthday.isBefore(LocalDate.of(1900, 1, 1))) {
        throw new BusinessException(ErrorCode.INVALID_BIRTHDAY);
      }

      int initialScore =
          initialScoreRepository
              .findTopByYearsLessThanEqualOrderByYearsDesc(request.careerYears())
              .map(InitialScore::getScore)
              .orElse(0);

      // TODO: 계산한 점수 토대로 등급 산정 로직 추가


      // 생년월일 기반 임의 password 발급
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
      String rawPassword = request.birthday().format(formatter);

      // Member 저장
      Member member =
          Member.builder()
              .employeeIdentificationNumber(request.employeeIdentificationNumber())
              .employeeName(request.employeeName())
              .phoneNumber(request.phoneNumber())
              .birthday(request.birthday())
              .joinedAt(request.joinedAt())
              .email(request.email())
              .careerYears(request.careerYears())
              .positionName(request.positionName())
              .departmentName(request.departmentName())
              .profileImageUrl(request.profileImageUrl())
              .salary(request.salary())
              .gradeCode(GradeCode.D)
              .role(MemberRole.INSIDER)
              .status(MemberStatus.AVAILABLE)
              .password(passwordEncoder.encode(rawPassword))
              .createdAt(LocalDateTime.now())
              .updatedAt(LocalDateTime.now())
              .build();

      memberRepository.save(member);

      // TODO: TechStack 검증 로직 추가

      // 기술스택 저장
      if (request.techStackNames() != null) {
        List<DeveloperTechStack> techStacks =
            request.techStackNames().stream()
                .map(
                    stack ->
                        DeveloperTechStack.builder()
                            .employeeIdentificationNumber(member.getEmployeeIdentificationNumber())
                            .techStackName(stack)
                            .totalScore(initialScore) // 기본값
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                .collect(Collectors.toList());

        developerTechStackRepository.saveAll(techStacks);
      }
    }
  }
}
