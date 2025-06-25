package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.feature.project.query.repository.DomainQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DomainQueryServiceImpl implements DomainQueryService {

    private final DomainQueryRepository domainQueryRepository;

    @Override
    public List<String> findAllDomains() {
        return domainQueryRepository.findAllDomains();
    }
}
