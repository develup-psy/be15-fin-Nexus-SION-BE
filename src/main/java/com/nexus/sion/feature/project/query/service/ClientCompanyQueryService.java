package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ClientCompanySearchRequest;
import com.nexus.sion.feature.project.query.dto.response.ClientCompanyDto;

public interface ClientCompanyQueryService {

  PageResponse<ClientCompanyDto> findClientCompanies(ClientCompanySearchRequest request);
}
