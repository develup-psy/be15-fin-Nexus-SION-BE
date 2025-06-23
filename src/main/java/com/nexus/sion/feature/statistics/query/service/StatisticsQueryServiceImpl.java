package com.nexus.sion.feature.statistics.query.service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;
import com.nexus.sion.feature.statistics.query.repository.StatisticsQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsQueryServiceImpl implements StatisticsQueryService {

    private final StatisticsQueryRepository repository;

    @Override
    public List<TechStackCountDto> getStackMemberCounts(List<String> stackNames) {
        return repository.findStackMemberCount(stackNames);
    }

    @Override
    public List<String> findAllStackNames() {
        return repository.findAllStackNames();
    }

    @Override
    public PageResponse<DeveloperDto> getAllDevelopers(int page, int size) {
        return repository.findAllDevelopers(page, size);
    }

}
