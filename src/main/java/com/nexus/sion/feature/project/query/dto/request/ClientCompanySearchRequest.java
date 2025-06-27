package com.nexus.sion.feature.project.query.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientCompanySearchRequest {
    @Min(0)
    private int page = 0;

    @Min(1)
    private int size = 10;

    private String companyName;
}
