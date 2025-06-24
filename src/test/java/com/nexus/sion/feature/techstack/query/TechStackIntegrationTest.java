package com.nexus.sion.feature.techstack.query;

import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TechStackIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TechStackRepository techStackRepository;

    @Test
    void 기술스택_전체조회_성공() throws Exception {
        // 테스트 데이터 저장
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
        techStackRepository.flush();

        // when & then
        mockMvc.perform(get("/api/v1/tech-stack")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.techStacks", hasItems(techStackName)));
    }
}