package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.application.dto.request.JobRequest;

public interface JobCommandService {
  boolean registerJob(JobRequest request);

  void removeJob(String jobName);
}
