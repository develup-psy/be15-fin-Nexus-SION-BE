package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.feature.project.query.repository.JobQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobQueryServiceImpl implements JobQueryService {

    private final JobQueryRepository jobQueryRepository;

    @Override
    public List<String> findAllJobs() {
        return jobQueryRepository.findAllJobs();
    }
}
