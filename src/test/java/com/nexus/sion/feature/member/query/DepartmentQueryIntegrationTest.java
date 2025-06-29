package com.nexus.sion.feature.member.query;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Department;
import com.nexus.sion.feature.member.command.domain.repository.DepartmentRepository;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackRepository;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DepartmentQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private DepartmentRepository departmentRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private DeveloperTechStackRepository developerTechStackRepository;

  @BeforeEach
  void setUp() {
    developerTechStackRepository.deleteAll();
    memberRepository.deleteAll();

    if (!departmentRepository.existsById("백엔드팀")) {
      departmentRepository.save(Department.builder().departmentName("백엔드팀").build());
    }

    departmentRepository.flush();
  }

  @Test
  @DisplayName("부서 목록 조회 API - 응답에 '백엔드팀' 포함 확인")
  void getAllDepartments_success_containsExpectedDepartments() throws Exception {
    mockMvc
        .perform(get("/api/v1/departments").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].departmentName", hasItems("백엔드팀")));
  }
}
