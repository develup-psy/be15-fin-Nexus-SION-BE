package com.nexus.sion.feature.squad.query.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import com.nexus.sion.feature.squad.query.dto.response.JobAndCount;
import com.nexus.sion.feature.squad.query.dto.response.JobInfo;

@Mapper
public interface SquadQueryMapper {


  List<DeveloperSummary> findDevelopersByStacksPerJob(
      @Param("projectAndJobId") Long projectAndJobId, @Param("projectId") String projectId);

  List<JobInfo> findJobsByProjectId(@Param("projectId") String projectId);

  List<JobAndCount> findRequiredMemberCountByRoles(@Param("projectId") String projectId);
}
