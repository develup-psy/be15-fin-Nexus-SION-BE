package com.nexus.sion.feature.member.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.member.command.application.dto.request.GradeDto;
import com.nexus.sion.feature.member.command.application.dto.request.UnitPriceSetRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Grade;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.repository.GradeCommandRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class GradeCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private GradeCommandRepository gradeRepository;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void setUnitPriceByGrade_shouldCreateOrUpdateGrades() throws Exception {
    // given: 기존 Grade 하나 저장 (B등급)
    Grade existingGrade =
        Grade.builder()
            .gradeCode(GradeCode.B)
            .productivity(new BigDecimal("3"))
            .monthlyUnitPrice(50000)
            .build();
    gradeRepository.save(existingGrade);

    // 새로운 요청 payload 준비 (A:신규, B:업데이트)
    UnitPriceSetRequest request =
        UnitPriceSetRequest.builder()
            .grades(
                List.of(
                    GradeDto.builder()
                        .gradeCode(GradeCode.A)
                        .productivity(new BigDecimal("3"))
                        .monthlyUnitPrice(100000)
                        .build(),
                    GradeDto.builder()
                        .gradeCode(GradeCode.B)
                        .productivity(new BigDecimal("2.6"))
                        .build()))
            .build();

    // when: 요청 수행
    mockMvc
        .perform(
            post("/api/v1/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // then: DB에 결과 반영 확인
    Grade gradeA = gradeRepository.findByGradeCode(GradeCode.A).orElseThrow();
    assertThat(gradeA.getProductivity()).isEqualTo(new BigDecimal("3"));
    assertThat(gradeA.getMonthlyUnitPrice()).isEqualTo(100000);
    assertThat(gradeA.getRatio()).isEqualTo(new BigDecimal("0.2")); // ratio 값 확인 추가

    Grade gradeB = gradeRepository.findByGradeCode(GradeCode.B).orElseThrow();
    assertThat(gradeB.getProductivity()).isEqualTo(new BigDecimal("2.6")); // updated
    assertThat(gradeB.getMonthlyUnitPrice()).isEqualTo(50000); // not updated
    assertThat(gradeB.getRatio()).isEqualTo(new BigDecimal("0.2")); // ratio 값 확인 추가
  }
}
