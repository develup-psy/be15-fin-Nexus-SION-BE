package com.nexus.sion.feature.member.query;

import static com.nexus.sion.feature.squad.query.util.JsonUtils.objectMapper;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackRepository;
import com.nexus.sion.feature.member.query.dto.request.MemberSquadSearchRequest;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private MemberRepository memberRepository;
    @Autowired
    private DeveloperTechStackRepository developerTechStackRepository;

  @BeforeEach
  void setUp() {
    memberRepository.save(
        Member.builder()
            .employeeIdentificationNumber("EMP001")
            .employeeName("홍길동")
            .password("encoded_password")
            .email("example@example.com")
            .phoneNumber("01011111111")
            .role(MemberRole.INSIDER)
            .status(MemberStatus.AVAILABLE)
              .gradeCode(GradeCode.B)
            .birthday(LocalDate.of(1990, 1, 1))
            .salary(50000000L)
            .build());

    memberRepository.flush();
  }

  @DisplayName("회원 목록을 정상적으로 조회한다")
  @WithMockUser(username = "testuser")
  @Test
  void getAllMembers_success() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/members")
                .param("page", "0")
                .param("size", "10")
                .param("status", "AVAILABLE"))
        .andExpect(status().isOk())
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
    mockMvc
        .perform(
            get("/api/v1/members/search")
                .param("keyword", keyword)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content", not(empty())))
        .andExpect(jsonPath("$.data.content[0].name", containsString("홍")))
        .andExpect(jsonPath("$.data.totalElements", greaterThan(0)))
        .andExpect(jsonPath("$.data.currentPage").value(page));
  }

  @DisplayName("사번으로 회원 상세 조회 성공")
  @WithMockUser(username = "testuser")
  @Test
  void getMemberDetail_success() throws Exception {
    // given
    String employeeId = "EMP001";

    // when & then
    mockMvc
        .perform(get("/api/v1/members/{employeeId}", employeeId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.employeeId").value(employeeId))
        .andExpect(jsonPath("$.data.name", not(emptyString())))
        .andExpect(jsonPath("$.data.email", containsString("@")))
        .andExpect(jsonPath("$.data.status", notNullValue()));
  }

  @DisplayName("존재하지 않는 사번으로 조회 시 404 응답")
  @WithMockUser(username = "testuser")
  @Test
  void getMemberDetail_notFound() throws Exception {
    // given
    String nonExistingId = "NOT_EXIST_999";

    // when & then
    mockMvc
        .perform(get("/api/v1/members/{employeeId}", nonExistingId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value("20001"));
  }

  @Test
  @DisplayName("스쿼드용 개발자 목록 조회 API는 정상 응답을 반환한다.")
  @WithMockUser
  void squadSearchDevelopers_success() throws Exception {

    memberRepository.save(
            Member.builder()
                    .employeeIdentificationNumber("12345678")
                    .employeeName("아무개")
                    .password("encoded_password")
                    .email("example@example.com")
                    .phoneNumber("01011111111")
                    .role(MemberRole.INSIDER)
                    .status(MemberStatus.AVAILABLE)
                    .gradeCode(GradeCode.B)
                    .birthday(LocalDate.of(1990, 1, 1))
                    .salary(50000000L)
                    .build());

    memberRepository.flush();

    DeveloperTechStack techStack = DeveloperTechStack.builder()
            .employeeIdentificationNumber("12345678")
            .techStackName("Java")
            .totalScore(90)
            .build();

    developerTechStackRepository.save(techStack);
    developerTechStackRepository.flush();


    // given
    MemberSquadSearchRequest request = MemberSquadSearchRequest.builder()
            .keyword("아무개")
            .grades(List.of("B"))
            .status("AVAILABLE")
            .stacks(List.of("Java"))
            .sortBy("grade")
            .sortDir("asc")
            .page(0)
            .size(10)
            .build();


    // when & then
    mockMvc.perform(post("/api/v1/members/squad-search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].employeeId").value("12345678"))
            .andExpect(jsonPath("$.data.content[0].name").value("아무개"))
            .andExpect(jsonPath("$.data.content[0].grade").value("B"))
            .andExpect(jsonPath("$.data.content[0].status").value("AVAILABLE"))
            .andExpect(jsonPath("$.data.content[0].topTechStackName").value("Java"))
            .andExpect(jsonPath("$.data.content[0].monthlyUnitPrice").isNumber())
            .andExpect(jsonPath("$.data.content[0].productivity").isNumber());
  }
}
