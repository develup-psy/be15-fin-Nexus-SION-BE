package com.nexus.sion.feature.member.query;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.save(Member.builder()
                .employeeIdentificationNumber("EMP001")
                .employeeName("홍길동")
                .password("encoded_password")
                .email("example@example.com")
                .phoneNumber("01011111111")
                .role(MemberRole.INSIDER)
                .status(MemberStatus.AVAILABLE)
                .birthday(LocalDate.of(1990, 1, 1))
                .salary(50000000L)
                .build());

        memberRepository.flush();
    }

    @DisplayName("회원 목록을 정상적으로 조회한다")
    @WithMockUser(username = "testuser")
    @Test
    void getAllMembers_success() throws Exception {
        mockMvc.perform(get("/members").param("page", "0").param("size", "10").param("status",
                        "AVAILABLE")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", not(empty())))
                .andExpect(jsonPath("$.data.totalElements", greaterThan(0)));
    }

    @DisplayName("키워드로 검색 시 정상적인 회원 목록을 반환한다")
    @WithMockUser(username = "testuser")
    @Test
    void searchAvailableMembers_success() throws Exception {
        // given
        String keyword = "홍"; // DB에 '홍길동'이 존재한다고 가정
        int page = 0;
        int size = 10;

        // when & then
        mockMvc.perform(get("/members/search").param("keyword", keyword)
                        .param("page", String.valueOf(page)).param("size", String.valueOf(size)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", not(empty())))
                .andExpect(jsonPath("$.data.content[0].name", containsString("홍")))
                .andExpect(jsonPath("$.data.totalElements", greaterThan(0)))
                .andExpect(jsonPath("$.data.currentPage").value(page));
    }
}
