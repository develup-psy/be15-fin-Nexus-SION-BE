// TechStackCareerDto.java
package com.nexus.sion.feature.statistics.query.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TechStackCareerDto {
    private String techStackName;
    private Double averageCareer;
    private Double minCareer;
    private Double maxCareer;
    private Integer count;
}