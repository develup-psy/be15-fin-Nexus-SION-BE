package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.common.fastapi.FastApiClient;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackHistoryRepository;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackRepository;
import com.nexus.sion.feature.member.command.domain.repository.MemberScoreHistoryRepository;
import com.nexus.sion.feature.project.command.application.dto.FunctionScore;
import com.nexus.sion.feature.project.command.application.dto.FunctionScoreDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@Transactional
class ProjectEvaluateCommandServiceImplTest {

    @InjectMocks
    private ProjectEvaluateCommandServiceImpl projectEvaluateCommandService;

    @Mock
    private DeveloperTechStackRepository developerTechStackRepository;
    @Mock
    private DeveloperTechStackHistoryRepository developerTechStackHistoryRepository;
    @Mock
    private MemberScoreHistoryRepository memberScoreHistoryRepository;
    @Mock
    private FastApiClient fastApiClient;

    @Test
    @DisplayName("기능별 점수 평가 성공")
    void evaluateFunctionScores_success() {
        // given
        String ein = "EMP001";
        String projectCode = "PJT001";

        FunctionScore functionScore = FunctionScore.builder()
                .functionName("회원가입")
                .description("회원 가입 처리")
                .fpType("ILF")
                .det(5)
                .ftrOrRet(2)
                .stacks(List.of("Java", "Spring"))
                .build();

        FunctionScoreDTO dto = FunctionScoreDTO.builder()
                .employeeIdentificationNumber(ein)
                .projectCode(projectCode)
                .functionScores(List.of(functionScore))
                .build();

        DeveloperTechStack savedStack = DeveloperTechStack.builder()
                .id(100L)
                .employeeIdentificationNumber(ein)
                .techStackName("Java")
                .totalScore(10)
                .build();

        // 개발자 기술스택 조회시 Optional.empty() 반환해서 새로 저장하도록
        given(developerTechStackRepository.findByEmployeeIdentificationNumberAndTechStackName(ein, "Java"))
                .willReturn(Optional.empty());
        given(developerTechStackRepository.findByEmployeeIdentificationNumberAndTechStackName(ein, "Spring"))
                .willReturn(Optional.empty());

        given(developerTechStackRepository.save(any())).willReturn(savedStack);

        // developerTechStackRepository.findAllByEmployeeIdentificationNumber
        given(developerTechStackRepository.findAllByEmployeeIdentificationNumber(ein))
                .willReturn(List.of(
                        DeveloperTechStack.builder().techStackName("Java").totalScore(50).build(),
                        DeveloperTechStack.builder().techStackName("Spring").totalScore(30).build()
                ));

        // memberScoreHistoryRepository 조회시 Optional.empty()로 새로 생성
        given(memberScoreHistoryRepository.findByEmployeeIdentificationNumber(ein))
                .willReturn(Optional.empty());

        // fastApiClient
        willDoNothing().given(fastApiClient).sendVectors(anyList());

        // when
        projectEvaluateCommandService.evaluateFunctionScores(dto);

        // then
        verify(developerTechStackRepository, times(2))
                .findByEmployeeIdentificationNumberAndTechStackName(anyString(), anyString());
        verify(developerTechStackRepository, atLeastOnce()).save(any());
        verify(developerTechStackHistoryRepository, atLeastOnce()).save(any());
        verify(memberScoreHistoryRepository, atLeastOnce()).save(any());
        verify(fastApiClient).sendVectors(anyList());
    }

}