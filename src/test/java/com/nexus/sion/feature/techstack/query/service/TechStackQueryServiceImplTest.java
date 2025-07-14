package com.nexus.sion.feature.techstack.query.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexus.sion.feature.techstack.query.repository.TechStackQueryRepository;

@ExtendWith(MockitoExtension.class)
class TechStackQueryServiceImplTest {
  @Mock private TechStackQueryRepository repository;

  @InjectMocks private TechStackQueryServiceImpl service;

  // 전체 기술 스택 이름 목록을 조회하는 기능을 테스트
  @Test
  void findAllStackNames_returnsList() {
    List<String> mockStacks = List.of("Java", "Spring", "Vue");
    when(repository.findAllStackNames()).thenReturn(mockStacks);

    List<String> result = service.findAllStackNames();

    assertEquals(3, result.size());
    assertTrue(result.contains("Spring"));
  }

  @Nested
  @DisplayName("autocomplete() - 기술스택 자동완성 조회")
  class AutocompleteTests {

    private String keyword;

    @BeforeEach
    void setUp() {
      keyword = "jav"; // 공통 키워드
    }

    @Test
    @DisplayName("성공: 입력한 키워드에 해당하는 기술스택 리스트 반환")
    void givenValidKeyword_whenAutocomplete_thenReturnsMatchingStacks() {
      // given
      List<String> mockResult = List.of("Java", "JavaScript");
      when(repository.findAutoCompleteTechStacks(keyword)).thenReturn(mockResult);

      // when
      List<String> result = service.autocomplete(keyword);

      // then
      assertThat(result).hasSize(2).containsExactly("Java", "JavaScript");
      verify(repository).findAutoCompleteTechStacks(keyword);
      verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("경계값: 일치하는 기술스택이 없으면 빈 리스트 반환")
    void givenNoMatchingKeyword_whenAutocomplete_thenReturnsEmptyList() {
      // given
      when(repository.findAutoCompleteTechStacks(keyword)).thenReturn(List.of());

      // when
      List<String> result = service.autocomplete(keyword);

      // then
      assertThat(result).isEmpty();
      verify(repository).findAutoCompleteTechStacks(keyword);
      verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("예외: repository에서 예외 발생 시 런타임 예외 발생")
    void givenRepositoryThrows_whenAutocomplete_thenThrowsException() {
      // given
      when(repository.findAutoCompleteTechStacks(keyword))
              .thenThrow(new RuntimeException("DB Error"));

      // when & then
      assertThrows(RuntimeException.class, () -> service.autocomplete(keyword));
      verify(repository).findAutoCompleteTechStacks(keyword);
      verifyNoMoreInteractions(repository);
    }

    @Test
    @DisplayName("Mock 검증: 정확한 키워드로 repository가 호출되는지 검증")
    void givenKeyword_whenAutocomplete_thenVerifyCorrectKeywordUsed() {
      // given
      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      when(repository.findAutoCompleteTechStacks(anyString())).thenReturn(List.of("Java"));

      // when
      service.autocomplete(keyword);

      // then
      verify(repository).findAutoCompleteTechStacks(captor.capture());
      assertThat(captor.getValue()).isEqualTo("jav");
      verifyNoMoreInteractions(repository);
    }
  }
}
