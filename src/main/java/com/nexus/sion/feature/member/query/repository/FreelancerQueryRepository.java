package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.tables.Freelancer.FREELANCER;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.member.query.dto.response.FreelancerListResponse;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class FreelancerQueryRepository {

  private final DSLContext dsl;

  public PageResponse<FreelancerListResponse> getFreelancerList(int page, int size) {
    int offset = (page - 1) * size;

    List<FreelancerListResponse> content =
        dsl.select(
                FREELANCER.FREELANCER_ID,
                FREELANCER.NAME,
                FREELANCER.PROFILE_IMAGE_URL,
                FREELANCER.EMAIL,
                FREELANCER.CREATED_AT)
            .from(FREELANCER)
            .orderBy(FREELANCER.CREATED_AT.desc())
            .limit(size)
            .offset(offset)
            .fetch()
            .map(
                r ->
                    new FreelancerListResponse(
                        r.get(FREELANCER.FREELANCER_ID),
                        r.get(FREELANCER.NAME),
                        r.get(FREELANCER.PROFILE_IMAGE_URL),
                        r.get(FREELANCER.EMAIL),
                        Optional.ofNullable(r.get(FREELANCER.CREATED_AT, Timestamp.class))
                            .map(Timestamp::toLocalDateTime)
                            .orElse(null)));

    Long total =
        Optional.ofNullable(dsl.selectCount().from(FREELANCER).fetchOne(0, Long.class)).orElse(0L);

    return PageResponse.fromJooq(content, total, page, size);
  }
}
