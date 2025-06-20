package com.nexus.sion.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "페이지네이션 응답 데이터 포맷")
public class PageResponse<T> {
    @Schema(description = "전체 데이터 수", example = "120")
    private long totalElements;

    @Schema(description = "전체 페이지 수", example = "12")
    private int totalPages;

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    private int currentPage;

    @Schema(description = "페이지당 데이터 수", example = "10")
    private int pageSize;

    @Schema(description = "현재 페이지의 데이터 목록")
    private List<T> content;

    @Schema(description = "다음 페이지 존재 여부")
    private boolean hasNext;

    @Schema(description = "이전 페이지 존재 여부")
    private boolean hasPrevious;

    /* JPA 사용 방법 */
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder().totalElements(page.getTotalElements())
                        .totalPages(page.getTotalPages()).currentPage(page.getNumber())
                        .pageSize(page.getSize()).content(page.getContent()).hasNext(page.hasNext())
                        .hasPrevious(page.hasPrevious()).build();
    }

    /* JOOQ 사용 방법 */
    public static <T> PageResponse<T> fromJooq(List<T> content, long totalElements, int currentPage,
                    int pageSize) {
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        boolean hasNext = (long) (currentPage + 1) * pageSize < totalElements;
        boolean hasPrevious = currentPage > 0;

        return PageResponse.<T>builder().totalElements(totalElements).totalPages(totalPages)
                        .currentPage(currentPage).pageSize(pageSize).content(content)
                        .hasNext(hasNext).hasPrevious(hasPrevious).build();
    }
}
