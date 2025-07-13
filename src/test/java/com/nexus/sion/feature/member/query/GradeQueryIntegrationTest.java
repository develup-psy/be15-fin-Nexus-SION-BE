package com.nexus.sion.feature.member.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.member.query.dto.response.GradeDto;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Grade;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.repository.GradeRepository; // ⭐️ JPA Repository

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GradeQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/v1/grades - 등급 조회 성공 (JPA로 insert, JOOQ로 조회)")
    void getUnitPriceByGrade_success() throws Exception {

        // when : 기존 데이터가 이미 존재한다는 전제
        mockMvc.perform(get("/api/v1/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", not(empty())))
                .andExpect(jsonPath("$.data[0].gradeCode").exists())
                .andExpect(jsonPath("$.data[0].monthlyUnitPrice").exists());

    }
}