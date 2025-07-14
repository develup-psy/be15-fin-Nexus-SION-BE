package com.nexus.sion.feature.member.command.application.service;

import static com.nexus.sion.feature.project.command.application.util.FPScoreUtils.classifyComplexity;
import static com.nexus.sion.feature.project.command.application.util.FPScoreUtils.getFpScore;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nexus.sion.common.fastapi.FastApiClient;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStackHistory;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.MemberScoreHistory;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.*;
import com.nexus.sion.feature.member.query.dto.response.FreelancerDetailResponse;
import com.nexus.sion.feature.member.query.repository.FreelancerQueryRepository;
import com.nexus.sion.feature.project.command.application.dto.FunctionScore;
import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FreelancerCommandServiceImpl implements FreelancerCommandService {

  private final FreelancerQueryRepository freelancerQueryRepository;
  private final MemberRepository memberRepository;
  private final FreelancerRepository freelancerRepository;
  private final PasswordEncoder passwordEncoder;

  private final DeveloperTechStackRepository developerTechStackRepository;
  private final DeveloperTechStackHistoryRepository developerTechStackHistoryRepository;
  private final MemberScoreHistoryRepository memberScoreHistoryRepository;
  private final FastApiClient fastApiClient;
  private final TechStackRepository techStackRepository;

  @Override
  public void registerFreelancerAsMember(String freelancerId, MultipartFile multipartFile) {
    FreelancerDetailResponse freelancer =
        freelancerQueryRepository.getFreelancerDetail(freelancerId);

    String rawPassword =
        (freelancer.birthday() != null)
            ? String.format(
                "%02d%02d%02d",
                freelancer.birthday().getYear() % 100,
                freelancer.birthday().getMonthValue(),
                freelancer.birthday().getDayOfMonth())
            : "000000";

    String encodedPassword = passwordEncoder.encode(rawPassword);

    Member member =
        memberRepository
            .findById(freelancer.freelancerId())
            .orElseGet(
                () -> {
                  Member newMember =
                      Member.builder()
                          .employeeIdentificationNumber(freelancer.freelancerId())
                          .employeeName(freelancer.name())
                          .password(encodedPassword)
                          .profileImageUrl(
                              freelancer.profileImageUrl() != null
                                  ? freelancer.profileImageUrl()
                                  : "https://api.dicebear.com/9.x/notionists/svg?seed="
                                      + freelancer.freelancerId())
                          .phoneNumber(freelancer.phoneNumber())
                          .email(freelancer.email())
                          .birthday(freelancer.birthday())
                          .careerYears(freelancer.careerYears())
                          .joinedAt(LocalDate.now())
                          .role(MemberRole.OUTSIDER)
                          .status(MemberStatus.AVAILABLE)
                          .build();
                  return memberRepository.save(newMember);
                });

    memberRepository.save(member);

    File tempFile;
    List<FunctionScore> functions;
    try {
      tempFile = File.createTempFile("input_", ".pdf");
      multipartFile.transferTo(tempFile);

      functions = fastApiClient.requestFpFreelencerInference(tempFile);
    } catch (IOException e) {
      log.error("[FP 분석 실패] {}", e.getMessage(), e);
      throw new BusinessException(ErrorCode.FP_ANALYZE_FAIL);
    }

    // FastAPI 분석 결과로 기술스택 점수 누적 계산
    Map<String, Integer> techStackTotalScoreMap = new HashMap<>();
    for (FunctionScore req : functions) {
      String complexity = classifyComplexity(req.getFpType(), req.getDet(), req.getFtrOrRet());
      int score = getFpScore(req.getFpType(), complexity);
      int perStackScore = score / req.getStacks().size();
      for (String stack : req.getStacks()) {
        techStackTotalScoreMap.merge(stack, perStackScore, Integer::sum);
      }
    }

    // 기술스택 및 이력 저장
    for (Map.Entry<String, Integer> entry : techStackTotalScoreMap.entrySet()) {
      String techStackName = entry.getKey();
      int addedScore = entry.getValue();

      techStackRepository
          .findById(techStackName)
          .orElseGet(
              () ->
                  techStackRepository.save(
                      TechStack.builder().techStackName(techStackName).build()));

      DeveloperTechStack stack =
          developerTechStackRepository
              .findByEmployeeIdentificationNumberAndTechStackName(
                  freelancer.freelancerId(), techStackName)
              .orElseGet(
                  () ->
                      developerTechStackRepository.save(
                          DeveloperTechStack.builder()
                              .employeeIdentificationNumber(freelancer.freelancerId())
                              .techStackName(techStackName)
                              .totalScore(0)
                              .build()));

      developerTechStackHistoryRepository.save(
          DeveloperTechStackHistory.builder()
              .addedScore(addedScore)
              .developerTechStackId(stack.getId())
              .build());

      stack.setTotalScore(stack.getTotalScore() + addedScore);
      developerTechStackRepository.save(stack);
    }

    // Member 점수 이력 갱신
    int totalStackScore =
        developerTechStackRepository
            .findAllByEmployeeIdentificationNumber(freelancer.freelancerId())
            .stream()
            .mapToInt(DeveloperTechStack::getTotalScore)
            .sum();

    MemberScoreHistory scoreHistory =
        memberScoreHistoryRepository
            .findByEmployeeIdentificationNumber(freelancer.freelancerId())
            .orElseGet(
                () ->
                    MemberScoreHistory.builder()
                        .employeeIdentificationNumber(freelancer.freelancerId())
                        .totalCertificateScores(0)
                        .totalTechStackScores(0)
                        .build());
    scoreHistory.setTotalTechStackScores(totalStackScore);
    memberScoreHistoryRepository.save(scoreHistory);

    freelancerRepository.deleteById(freelancer.freelancerId());
  }
}
