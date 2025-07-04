package com.nexus.sion.feature.project.query.mapper;

import com.nexus.sion.feature.project.query.dto.response.JobRequirement;
import com.nexus.sion.feature.project.query.dto.response.ProjectForSquadResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectQueryMapper {
  ProjectForSquadResponse findProjectInfo(@Param("projectCode") String projectCode);
  List<JobRequirement> findJobRequirements(@Param("projectCode") String projectCode);
}
