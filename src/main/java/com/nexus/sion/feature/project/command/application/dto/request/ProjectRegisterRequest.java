package com.nexus.sion.feature.project.command.application.dto.request;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRegisterRequest {

    private String projectCode;
    private String name;
    private String description;
    private String title;
    private Long budget;
    private LocalDate startDate;
    private LocalDate expectedEndDate;
    private String clientCode;
    private Integer numberOfMembers;
    private String requestSpecificationUrl; // S3에서 받은 URL

    private List<JobInfo> jobs;

    @Getter
    @Setter
    public static class JobInfo {
        private String jobName;
        private int requiredNumber;
        private List<TechStackInfo> techStacks;
    }

    @Getter
    @Setter
    public static class TechStackInfo {
        private String techStackId;
        private Integer priority;
    }
}
