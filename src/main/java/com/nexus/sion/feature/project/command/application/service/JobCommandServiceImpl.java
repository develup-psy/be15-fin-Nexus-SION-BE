package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.nexus.sion.feature.project.command.application.dto.request.JobRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Job;
import com.nexus.sion.feature.project.command.repository.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobCommandServiceImpl implements JobCommandService {

  private final ModelMapper modelMapper;
  private final JobRepository jobRepository;

  @Override
  public boolean registerJob(JobRequest request) {
    // 기존에 존재하는 도메인은 저장하지 않고 종료
    if (jobRepository.existsById(request.getName())) {
      return false;
    }

    Job job = modelMapper.map(request, Job.class);
    jobRepository.save(job);
    return true;
  }

  @Override
  public void removeJob(String jobName) {
    // 기존에 해당 직무가 없으면 에러
    if (!jobRepository.existsById(jobName)) {
      throw new BusinessException(ErrorCode.JOB_NOT_FOUND);
    }

    // 해당 도메인 삭제
    jobRepository.deleteById(jobName);
  }
}
