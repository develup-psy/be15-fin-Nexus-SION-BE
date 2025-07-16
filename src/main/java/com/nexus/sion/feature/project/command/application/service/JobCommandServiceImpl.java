package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.feature.project.command.domain.repository.ProjectAndJobRepository;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.JobRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Job;
import com.nexus.sion.feature.project.command.repository.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobCommandServiceImpl implements JobCommandService {

  private final ModelMapper modelMapper;
  private final JobRepository jobRepository;
  private final ProjectAndJobRepository projectAndJobRepository;

  @Override
  public void registerJob(JobRequest request) {
    // 기존에 존재하는 직무 예외처리
    if (jobRepository.existsById(request.getName())) {
      throw new BusinessException(ErrorCode.JOB_ALREADY_EXIST);
    }

    Job job = modelMapper.map(request, Job.class);
    jobRepository.save(job);
  }

  @Override
  public void removeJob(String jobName) {
    // 기존에 해당 직무가 없으면 에러
    if (!jobRepository.existsById(jobName)) {
      throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
    }

    if(projectAndJobRepository.existsByJobName(jobName)) {
      throw new BusinessException(ErrorCode.JOB_DELETE_CONSTRAINT);
    }

    jobRepository.deleteById(jobName);
  }
}
