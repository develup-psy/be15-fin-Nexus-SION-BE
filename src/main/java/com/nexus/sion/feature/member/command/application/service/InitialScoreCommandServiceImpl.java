package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreDto;
import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreSetRequset;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.InitialScore;
import com.nexus.sion.feature.member.command.domain.repository.InitialScoreRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InitialScoreCommandServiceImpl implements InitialScoreCommandService {

    private final InitialScoreRepository initialScoreRepository;

    @Transactional
    @Override
    public void setInitialScores(InitialScoreSetRequset request) {
        List<InitialScoreDto> scores = request.getInitialScores();

        /* 유효성 검사
         * 1️⃣ 첫 번째 구간의 minYears 는 무조건 1 이어야 한다.
         * 2️⃣ 각 구간은 이전 maxYears 와 다음 minYears 가 정확히 1 차이가 나야 한다.
         * 3️⃣ 마지막 구간의 maxYears 는 null 이어야 한다.
         */

        // 1. 첫 번째 minYears == 1 체크
        if (scores.get(0).getMinYears() != 1) {
            throw new BusinessException(ErrorCode.FIRST_MIN_YEARS_SOULD_BE_1);
        }

        // 3. 각 구간이 연속되는지 체크 + 마지막 maxYears == null
        for (int i = 0; i < scores.size(); i++) {
            InitialScoreDto current = scores.get(i);

            // 마지막 구간이면 maxYears == null
            if (i == scores.size() - 1) {
                if (current.getMaxYears() == null) {
                    throw new BusinessException(ErrorCode.LAST_MAX_YEARS_SHOULD_BE_NULL);
                }
            } else {
                InitialScoreDto next = scores.get(i + 1);

                if (next.getMinYears() != current.getMaxYears() + 1) {
                    throw new BusinessException(ErrorCode.INTERVAL_YEARS_SHOULD_BE_CONTINUOUS);
                }
            }
        }

        // 기존 정보 remove
        initialScoreRepository.deleteAll();

        // 새로운 정보 등록
        scores.forEach(grade -> {
            InitialScore newScore = InitialScore.builder()
                    .minYears(grade.getMinYears())
                    .maxYears(grade.getMaxYears())
                    .score(grade.getScore())
                    .build();
            initialScoreRepository.save(newScore);
        });
    }
}
