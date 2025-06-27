package com.nexus.sion.feature.project.query.repository;

import static com.example.jooq.generated.tables.ClientCompany.CLIENT_COMPANY;
import static com.nexus.sion.common.constants.CollationConstants.UTF8MB4_UNICODE_520_CI;

import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.project.query.dto.response.ClientCompanyDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ClientCompanyQueryRepository {
  private final DSLContext dsl;


  public List<ClientCompanyDto> findAllByCondition(Condition condition, SortField<?> sortField, int page, int size) {
    return dsl.select(
                    CLIENT_COMPANY.CLIENT_CODE,
                    CLIENT_COMPANY.COMPANY_NAME,
                    CLIENT_COMPANY.DOMAIN_NAME,
                    CLIENT_COMPANY.CONTACT_PERSON,
                    CLIENT_COMPANY.EMAIL,
                    CLIENT_COMPANY.CONTACT_NUMBER
            )
            .from(CLIENT_COMPANY)
            .where(condition)
            .orderBy(sortField)
            .offset(page * size)
            .limit(size)
            .fetchInto(ClientCompanyDto.class);
  }

  public long countByCondition(Condition condition) {
    Long count = dsl.selectCount()
            .from(CLIENT_COMPANY)
            .where(condition)
            .fetchOne(0, long.class); // 첫 번째 열을 Long으로 가져옴
    return count != null ? count : 0L;
  }
}
