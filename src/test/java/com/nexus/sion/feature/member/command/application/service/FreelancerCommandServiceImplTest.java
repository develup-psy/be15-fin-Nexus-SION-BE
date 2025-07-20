package com.nexus.sion.feature.member.command.application.service;

import com.example.jooq.generated.tables.records.DeveloperTechStackRecord;
import com.nexus.sion.common.fastapi.FastApiClient;
import com.nexus.sion.common.s3.service.DocumentS3Service;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.repository.*;
import com.nexus.sion.feature.member.query.dto.response.FreelancerDetailResponse;
import com.nexus.sion.feature.member.query.repository.FreelancerQueryRepository;
import com.nexus.sion.feature.project.command.application.dto.FunctionScore;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FreelancerCommandServiceImplTest {

    @InjectMocks
    private FreelancerCommandServiceImpl service;

    @Mock private FreelancerQueryRepository freelancerQueryRepository;
    @Mock private MemberRepository memberRepository;
    @Mock private FreelancerRepository freelancerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private DeveloperTechStackRepository developerTechStackRepository;
    @Mock private DeveloperTechStackHistoryRepository developerTechStackHistoryRepository;
    @Mock private MemberScoreHistoryRepository memberScoreHistoryRepository;
    @Mock private FastApiClient fastApiClient;
    @Mock private TechStackRepository techStackRepository;
    @Mock private MemberCommandService memberCommandService;
    @Mock private DocumentS3Service documentS3Service;

    @Test
    void registerFreelancerAsMember_success() throws Exception {
        // given
        String freelancerId = "F001";
        FreelancerDetailResponse freelancer = new FreelancerDetailResponse(
                freelancerId,
                "홍길동",
                "01012345678",
                "developer@example.com",
                5,
                "http://resume.url",
                null,
                LocalDate.of(1995, 5, 10)
        );

        File dummyFile = File.createTempFile("dummy", ".pdf");
        List<FunctionScore> functions = List.of(
                FunctionScore.builder()
                        .functionName("회원가입")
                        .description("프리랜서 회원가입 기능")
                        .fpType("EO")
                        .det(3)
                        .ftrOrRet(2)
                        .stacks(List.of("Java", "Spring"))
                        .build()
        );

        DeveloperTechStack techStack = DeveloperTechStack.builder()
                .employeeIdentificationNumber("F001")
                .techStackName("Java")
                .totalScore(20)
                .build();

        when(freelancerQueryRepository.getFreelancerDetail(freelancerId)).thenReturn(freelancer);
        when(passwordEncoder.encode(any())).thenReturn("encodedPw");
        when(documentS3Service.downloadFileFromUrl(any())).thenReturn(dummyFile);
        when(fastApiClient.requestFpFreelencerInference(dummyFile)).thenReturn(functions);
        when(techStackRepository.findById(any())).thenReturn(Optional.empty());
        when(techStackRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(developerTechStackRepository.findByEmployeeIdentificationNumberAndTechStackName(any(), any()))
                .thenReturn(Optional.empty());
        when(developerTechStackRepository.save(any())).thenAnswer(inv -> {
            DeveloperTechStack saved = inv.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });
        when(developerTechStackRepository.findAllByEmployeeIdentificationNumber(any()))
                .thenReturn(List.of(techStack));
        when(memberCommandService.calculateGradeByScore(anyInt())).thenReturn(GradeCode.A);
        when(memberRepository.findById(freelancerId)).thenReturn(Optional.empty());
        when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(memberScoreHistoryRepository.findByEmployeeIdentificationNumber(freelancerId))
                .thenReturn(Optional.empty());
        when(memberScoreHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // when
        service.registerFreelancerAsMember(freelancerId);

        // then
        verify(freelancerQueryRepository).getFreelancerDetail(freelancerId);
        verify(documentS3Service).downloadFileFromUrl(freelancer.resumeUrl());
        verify(fastApiClient).requestFpFreelencerInference(dummyFile);
        verify(freelancerRepository).deleteById(freelancerId);
        assertThat(dummyFile.exists()).isFalse();
    }
}