package com.nexus.sion.feature.member.command.application.service;

import org.springframework.web.multipart.MultipartFile;

public interface FreelancerCommandService {
  void registerFreelancerAsMember(String freelancerId, MultipartFile multipartFile);
}
