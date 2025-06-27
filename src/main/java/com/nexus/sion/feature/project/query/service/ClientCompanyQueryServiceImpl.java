package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.feature.project.query.dto.response.ClientCompanyListResponse;
import com.nexus.sion.feature.project.query.repository.ClientCompanyQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientCompanyQueryServiceImpl implements ClientCompanyQueryService {

    private final ClientCompanyQueryRepository clientCompanyQueryRepository;

    @Override
    public ClientCompanyListResponse findAllClientCompany() {
         return new ClientCompanyListResponse(clientCompanyQueryRepository.findAllClientCompanies());
    }
}
