package com.nexus.sion.feature.project.query.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nexus.sion.feature.project.query.dto.response.JobRequirement;
import com.nexus.sion.feature.project.query.dto.response.ProjectForSquadResponse;

@Mapper
public interface ProjectQueryMapper {
  ProjectForSquadResponse findProjectInfo(@Param("projectCode") String projectCode);

  List<JobRequirement> findJobRequirements(@Param("projectCode") String projectCode);
}
