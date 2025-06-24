package com.nexus.sion.feature.techstack.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackCreateRequest;
import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TechStackCommandControllerIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private TechStackRepository techStackRepository;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ModelMapper modelMapper;

  @BeforeEach
  void setUp() {
    techStackRepository.deleteAll(); // 초기화
  }

  @Test
  @DisplayName("새로운 기술 스택을 등록하면 201이 반환된다.")
  void registerNewTechStack_returnsCreated() throws Exception {
    // given
    String techStackName = "test";
    TechStackCreateRequest request = new TechStackCreateRequest(techStackName);

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

    TechStackCreateRequest request = new TechStackCreateRequest(existingTechStackName);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/tech-stack")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated()); // 응답은 성공

    // then - 여전히 하나만 존재
    assertThat(techStackRepository.findAll().size()).isEqualTo(1);
  }
}
