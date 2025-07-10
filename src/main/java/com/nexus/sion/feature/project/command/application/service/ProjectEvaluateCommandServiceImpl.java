package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.common.fastapi.FastApiClient;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStackHistory;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.MemberScoreHistory;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackHistoryRepository;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackRepository;
import com.nexus.sion.feature.member.command.domain.repository.MemberScoreHistoryRepository;
import com.nexus.sion.feature.project.command.application.dto.FunctionScore;
import com.nexus.sion.feature.project.command.application.dto.FunctionScoreDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nexus.sion.feature.project.command.application.util.FPScoreUtils.classifyComplexity;
import static com.nexus.sion.feature.project.command.application.util.FPScoreUtils.getFpScore;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProjectEvaluateCommandServiceImpl implements ProjectEvaluateCommandService {
    private final DeveloperTechStackRepository developerTechStackRepository;
    private final DeveloperTechStackHistoryRepository developerTechStackHistoryRepository;
    private final MemberScoreHistoryRepository memberScoreHistoryRepository;
    private final FastApiClient fastApiClient;

    @Override
    public void evaluateFunctionScores(FunctionScoreDTO dto) {
        String ein = dto.getEmployeeIdentificationNumber();
        String projectCode = dto.getProjectCode();
        List<FunctionScore> functions = dto.getFunctionScores();

        Map<String, Integer> techStackTotalScoreMap = new HashMap<>();
        List<Map<String, Object>> vectorPayloads = new ArrayList<>();

        for (FunctionScore req : functions) {
            String complexity = classifyComplexity(req.getFpType(), req.getDet(), req.getFtrOrRet());
            int score = getFpScore(req.getFpType(), complexity);

            // 균등 분배 방식: 스택 수만큼 나눔
            int perStackScore = score / req.getStacks().size();
            for (String stack : req.getStacks()) {
                techStackTotalScoreMap.merge(stack, perStackScore, Integer::sum); //stack이 일치하면 점수 합치기
            }

            // FastAPI에 벡터 저장
            Map<String, Object> payload = new HashMap<>();
            payload.put("function_name", req.getFunctionName());
            payload.put("description", req.getDescription());
            payload.put("fp_type", req.getFpType());
            payload.put("det", req.getDet());
            payload.put("ftr", req.getFtrOrRet());
            payload.put("complexity", complexity);
            vectorPayloads.add(payload);
        }

        // 기술스택 점수 저장
        for (Map.Entry<String, Integer> entry : techStackTotalScoreMap.entrySet()) {
            String techStackName = entry.getKey();
            int addedScore = entry.getValue();

            DeveloperTechStack stack = developerTechStackRepository
                    .findByEmployeeIdentificationNumberAndTechStackName(ein, techStackName)
                    .orElseGet(() -> {
                        DeveloperTechStack newStack = DeveloperTechStack.builder()
                                .employeeIdentificationNumber(ein)
                                .techStackName(techStackName)
                                .totalScore(0)
                                .build();
                        return developerTechStackRepository.save(newStack);
                    });


            DeveloperTechStackHistory history = DeveloperTechStackHistory.builder()
                    .projectCode(projectCode)
                    .addedScore(addedScore)
                    .developerTechStackId(stack.getId())
                    .build();
            developerTechStackHistoryRepository.save(history);

            stack.setTotalScore(stack.getTotalScore() + addedScore);
            developerTechStackRepository.save(stack);
        }

        List<DeveloperTechStack> developerTechStackList = developerTechStackRepository.findAllByEmployeeIdentificationNumber(ein);

        int totalStackScore = developerTechStackList.stream().mapToInt(DeveloperTechStack::getTotalScore).sum();

        MemberScoreHistory scoreHistory = memberScoreHistoryRepository
                .findByEmployeeIdentificationNumber(ein)
                .orElseGet(() -> MemberScoreHistory.builder().totalCertificateScores(0).totalTechStackScores(0).employeeIdentificationNumber(ein).build());

        scoreHistory.setTotalTechStackScores(totalStackScore);
        memberScoreHistoryRepository.save(scoreHistory);

        fastApiClient.sendVectors(vectorPayloads);

        log.info("개발자 {} 기술스택 점수 저장 완료: {}", ein, techStackTotalScoreMap);
    }
}
