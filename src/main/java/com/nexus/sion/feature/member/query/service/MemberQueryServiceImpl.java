package com.nexus.sion.feature.member.query.service;

import static com.example.jooq.generated.tables.Member.MEMBER;

import java.util.List;

import org.jooq.Condition;
import org.jooq.SortField;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.generated.enums.MemberRole;
import com.example.jooq.generated.enums.MemberStatus;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.query.dto.request.MemberListRequest;
import com.nexus.sion.feature.member.query.dto.response.MemberListResponse;
import com.nexus.sion.feature.member.query.repository.MemberQueryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberQueryRepository memberQueryRepository;

    @Override
    public PageResponse<MemberListResponse> getAllMembers(MemberListRequest request) {
        int page=request.getPage();int size=request.getSize();String sortBy=request.getSortBy()!=null?request.getSortBy():"employeeName";String sortDir=request.getSortDir()!=null?request.getSortDir():"asc";

        // 기본 조건
        Condition condition=MEMBER.DELETED_AT.isNull().and(MEMBER.ROLE.eq(MemberRole.INSIDER));

        // 상태 필터
        if(request.getStatus()!=null){try{condition=condition.and(MEMBER.STATUS.eq(MemberStatus.valueOf(request.getStatus().toUpperCase())));}catch(IllegalArgumentException e){throw new BusinessException(ErrorCode.INVALID_MEMBER_STATUS);}}

        // 정렬 필드
        SortField<?>sortField=switch(sortBy){case"employeeId"->"desc".equalsIgnoreCase(sortDir)?MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.desc():MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER.asc();case"joinedAt"->"desc".equalsIgnoreCase(sortDir)?MEMBER.JOINED_AT.desc():MEMBER.JOINED_AT.asc();default->"desc".equalsIgnoreCase(sortDir)?MEMBER.EMPLOYEE_NAME.desc():MEMBER.EMPLOYEE_NAME.asc();};

        long total=memberQueryRepository.countMembers(condition);var content=memberQueryRepository.findAllMembers(request,condition,sortField);

        return PageResponse.fromJooq(content,total,page,size);
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberListResponse> searchMembers(String keyword, int page, int size) {
        int offset = page * size;
        List<MemberListResponse> content =
                        memberQueryRepository.searchMembers(keyword, offset, size);
        int total = memberQueryRepository.countSearchMembers(keyword);
        return PageResponse.fromJooq(content, total, page, size);
    }
}
