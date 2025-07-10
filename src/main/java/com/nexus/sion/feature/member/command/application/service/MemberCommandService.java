package com.nexus.sion.feature.member.command.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.*;
import jakarta.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.MemberAddRequest;
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.member.command.application.dto.request.MemberUpdateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.*;
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
  private final GradeRepository gradeRepository;
  private final MemberScoreHistoryRepository memberScoreHistoryRepository;
  private final DeveloperTechStackHistoryRepository developerTechStackHistoryRepository;

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
      if (birthday == null || birthday.isAfter(LocalDate.now()) || birthday.isBefore(LocalDate.of(1900, 1, 1))) {
        throw new BusinessException(ErrorCode.INVALID_BIRTHDAY);
      }

      int techStackCount = request.techStackNames() != null ? request.techStackNames().size() : 0;

      int initialScore =
              initialScoreRepository
                      .findByCareerYears(request.careerYears())
                      .map(InitialScore::getScore)
                      .orElse(0);

      int totalScore = initialScore * techStackCount;

      GradeCode gradeCode = calculateGradeByScore(totalScore);

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
                      .gradeCode(gradeCode)
                      .role(MemberRole.INSIDER)
                      .status(MemberStatus.AVAILABLE)
                      .password(passwordEncoder.encode(rawPassword))
                      .createdAt(LocalDateTime.now())
                      .updatedAt(LocalDateTime.now())
                      .build();

      memberRepository.save(member);

      // 기술스택 저장 및 developer_tech_stack_history 기록
      int totalTechStackScore = 0;
      if (request.techStackNames() != null) {
        List<DeveloperTechStack> techStacks = new ArrayList<>();
        List<DeveloperTechStackHistory> historyList = new ArrayList<>();

        for (String stackName : request.techStackNames()) {
          DeveloperTechStack techStack = DeveloperTechStack.builder()
                  .employeeIdentificationNumber(member.getEmployeeIdentificationNumber())
                  .techStackName(stackName)
                  .totalScore(initialScore)
                  .build();

          developerTechStackRepository.save(techStack);

          DeveloperTechStackHistory history = DeveloperTechStackHistory.builder()
                  .developerTechStackId(techStack.getId())
                  .addedScore(initialScore)
                  .projectCode(null)
                  .build();

          developerTechStackHistoryRepository.save(history);

          totalTechStackScore += initialScore;
        }
      }

      // member_score_history 저장
      MemberScoreHistory scoreHistory = MemberScoreHistory.builder()
              .employeeIdentificationNumber(member.getEmployeeIdentificationNumber())
              .totalTechStackScores(totalTechStackScore)
              .totalCertificateScores(0)
              .build();

      memberScoreHistoryRepository.save(scoreHistory);
    }
  }

  @Transactional
  public void updateMember(String employeeId, MemberUpdateRequest request) {
    // 1. 기존 멤버 조회
    Member member =
            memberRepository
                    .findById(employeeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    // 2. 이메일 형식 검증
    if (!Validator.isEmailValid(request.email())) {
      throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
    }

    // 3. 이메일 중복 검증 (본인 제외)
    if (!member.getEmail().equals(request.email())
            && memberRepository.existsByEmail(request.email())) {
      throw new BusinessException(ErrorCode.ALREADY_REGISTERED_EMAIL);
    }

    // 4. 핸드폰 번호 형식 검증
    if (!Validator.isPhonenumberValid(request.phoneNumber())) {
      throw new BusinessException(ErrorCode.INVALID_PHONE_NUMBER_FORMAT);
    }

    // 5. 생일 null 체크
    if (request.birthday() == null) {
      throw new BusinessException(ErrorCode.INVALID_BIRTHDAY);
    }

    // 6. Position 검증
    if (request.positionName() != null && !positionRepository.existsById(request.positionName())) {
      throw new BusinessException(ErrorCode.POSITION_NOT_FOUND);
    }

    // 7. Department 검증
    if (request.departmentName() != null
            && !departmentRepository.existsById(request.departmentName())) {
      throw new BusinessException(ErrorCode.DEPARTMENT_NOT_FOUND);
    }

    // 8. 필드 수정
    member.update(
            request.employeeName(),
            request.phoneNumber(),
            request.birthday(),
            request.joinedAt(),
            request.email(),
            request.careerYears(),
            request.positionName(),
            request.departmentName(),
            request.profileImageUrl(),
            request.salary());

    int initialScore =
            initialScoreRepository
                    .findByCareerYears(request.careerYears())
                    .map(InitialScore::getScore)
                    .orElse(0);

    // 9. 기술스택 수정 - 기존 점수 보존
    // 기존 기술스택 조회
    List<DeveloperTechStack> existingStacks =
            developerTechStackRepository.findAllByEmployeeIdentificationNumber(employeeId);

    Set<String> existingNames =
            existingStacks.stream()
                    .map(DeveloperTechStack::getTechStackName)
                    .collect(Collectors.toSet());

    Set<String> incomingNames =
            request.techStackNames() != null
                    ? new HashSet<>(request.techStackNames())
                    : Collections.emptySet();

    // 삭제 대상 = 기존에는 있었지만 요청에는 없음
    List<DeveloperTechStack> toDelete =
            existingStacks.stream()
                    .filter(stack -> !incomingNames.contains(stack.getTechStackName()))
                    .toList();
    developerTechStackRepository.deleteAll(toDelete);

    // 추가 대상 = 요청에는 있지만 기존에는 없던 스택만 insert
    List<DeveloperTechStack> toInsert =
            incomingNames.stream()
                    .filter(name -> !existingNames.contains(name))
                    .map(
                            name ->
                                    DeveloperTechStack.builder()
                                            .employeeIdentificationNumber(employeeId)
                                            .techStackName(name)
                                            .totalScore(initialScore)
                                            .build())
                    .toList();

    developerTechStackRepository.saveAll(toInsert);
  }

  @Transactional
  public void deleteMember(String employeeId) {
    Member member =
            memberRepository
                    .findById(employeeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    if (member.getDeletedAt() != null) {
      throw new BusinessException(ErrorCode.ALREADY_DELETED_USER);
    }

    if (member.getRole() == MemberRole.ADMIN) {
      throw new BusinessException(ErrorCode.CANNOT_DELETE_ADMIN);
    }

    member.markAsDeleted();
  }

  @Transactional
  public void updateMemberStatus(String employeeId, MemberStatus status) {
    Member member =
            memberRepository
                    .findById(employeeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

    member.updateStatus(status);
  }

  private GradeCode calculateGradeByScore(int totalScore) {
    List<Grade> grades = gradeRepository.findAllByOrderByScoreThresholdDesc();

    return grades.stream()
            .filter(g -> g.getScoreThreshold() > 0)
            .filter(g -> totalScore >= g.getScoreThreshold())
            .map(Grade::getGradeCode)
            .findFirst()
            .orElse(GradeCode.B);
  }
}
