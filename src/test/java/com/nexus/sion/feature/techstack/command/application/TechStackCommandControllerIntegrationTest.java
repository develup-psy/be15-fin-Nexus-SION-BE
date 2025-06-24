package com.nexus.sion.feature.techstack.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackRequest;
import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TechStackCommandControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private TechStackRepository techStackRepository;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("새로운 기술 스택을 등록하면 201이 반환된다.")
  void registerNewTechStack_returnsCreated() throws Exception {
    // given
    String techStackName = "test";
    TechStackRequest request = new TechStackRequest(techStackName);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/tech-stack")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated());

    // then - DB에 저장되었는지 확인
    assertThat(techStackRepository.findById(techStackName)).isPresent();
  }

  @Test
  @DisplayName("이미 존재하는 기술 스택은 저장하지 않는다.")
  void registerExistingTechStack_doesNotSaveAgain() throws Exception {
    // given
    String existingTechStackName = "techStackName";
    Constructor<TechStack> constructor = TechStack.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    TechStack existing = constructor.newInstance();

    // id 필드 설정
    Field idField = TechStack.class.getDeclaredField(existingTechStackName);
    idField.setAccessible(true);
    idField.set(existing, existingTechStackName);

    techStackRepository.save(existing);
    int existingCount = techStackRepository.findAll().size();

    TechStackRequest request = new TechStackRequest(existingTechStackName);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/tech-stack")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated()); // 응답은 성공

    // then - 여전히 하나만 존재
    assertThat(techStackRepository.findAll().size()).isEqualTo(existingCount);
  }

  @Test
  @DisplayName("기술 스택을 삭제하면 201이 반환되고 DB에서 제거된다.")
  void deleteExistingTechStack_returnsDeleted() throws Exception {
    // given
    String techStackName = "test";
    String techStackColumn = "techStackName";

    Constructor<TechStack> constructor = TechStack.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    TechStack existing = constructor.newInstance();

    // id 필드 설정
    Field idField = TechStack.class.getDeclaredField(techStackColumn);
    idField.setAccessible(true);
    idField.set(existing, techStackName);

    techStackRepository.save(existing);

    // when & then
    mockMvc
        .perform(delete("/api/v1/tech-stack/{techstackName}", techStackName))
        .andExpect(status().isNoContent());

    // then: DB에서 해당 기술 스택이 제거되었는지 확인한다.
    assertThat(techStackRepository.findById(techStackName)).isNotPresent();
  }

  @Test
  @DisplayName("존재하지 않는 기술 스택은 에러를 반환한다.")
  void deleteExistingTechStack_returnsError() throws Exception {
    // given
    String techStackName = "test";

    // when & then
    mockMvc
        .perform(delete("/api/v1/tech-stack/{techstackName}", techStackName))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.TECH_STACK_NOT_FOUND.getCode()))
        .andExpect(jsonPath("$.message").value(ErrorCode.TECH_STACK_NOT_FOUND.getMessage()))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
