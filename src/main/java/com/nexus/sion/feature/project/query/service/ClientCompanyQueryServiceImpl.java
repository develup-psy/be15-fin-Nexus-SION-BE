package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ClientCompanySearchRequest;
import com.nexus.sion.feature.project.query.dto.response.ClientCompanyDto;
import org.jooq.Condition;
import org.jooq.SortField;
import org.springframework.stereotype.Service;

import com.nexus.sion.feature.project.query.repository.ClientCompanyQueryRepository;

import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.example.jooq.generated.tables.ClientCompany.CLIENT_COMPANY;
import static com.nexus.sion.common.constants.CollationConstants.UTF8MB4_UNICODE_520_CI;

@Service
@RequiredArgsConstructor
public class ClientCompanyQueryServiceImpl implements ClientCompanyQueryService {

  private final ClientCompanyQueryRepository clientCompanyQueryRepository;

  @Override
  public PageResponse<ClientCompanyDto> findClientCompanies(ClientCompanySearchRequest request) {
    int page = request.getPage();
    int size = request.getSize();

    Condition condition = null;
    // 상태 필터
    if (request.getCompanyName() != null && !request.getCompanyName().isBlank()) {
      condition = CLIENT_COMPANY.COMPANY_NAME.likeIgnoreCase(request.getCompanyName() + "%");
    }

    // 정렬 필드
    SortField<?> sortField = CLIENT_COMPANY.COMPANY_NAME.collate(UTF8MB4_UNICODE_520_CI).asc();

    long total = clientCompanyQueryRepository.countByCondition(condition);
    List<ClientCompanyDto> content = clientCompanyQueryRepository.findAllByCondition(condition, sortField, page, size);

    return PageResponse.fromJooq(content, total, page, size);
  }
}
