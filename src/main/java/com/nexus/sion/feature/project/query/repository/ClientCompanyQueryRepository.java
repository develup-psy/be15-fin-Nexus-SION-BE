package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.tables.ClientCompany.CLIENT_COMPANY;
import static com.nexus.sion.common.constants.CollationConstants.UTF8MB4_UNICODE_520_CI;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.project.query.dto.response.ClientCompanyDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ClientCompanyQueryRepository {
  private final DSLContext dsl;

  public List<ClientCompanyDto> findAllClientCompanies() {
    return dsl.select(
            CLIENT_COMPANY.CLIENT_CODE,
            CLIENT_COMPANY.COMPANY_NAME,
            CLIENT_COMPANY.DOMAIN_NAME,
            CLIENT_COMPANY.CONTACT_PERSON,
            CLIENT_COMPANY.EMAIL,
            CLIENT_COMPANY.CONTACT_NUMBER)
        .from(CLIENT_COMPANY)
        .orderBy(CLIENT_COMPANY.COMPANY_NAME.collate(UTF8MB4_UNICODE_520_CI).asc())
        .fetchInto(ClientCompanyDto.class);
  }
}
