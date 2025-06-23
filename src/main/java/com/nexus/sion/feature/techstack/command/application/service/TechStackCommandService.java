package com.nexus.sion.feature.techstack.command.application.service;

import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackCreateRequest;
import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;


public interface TechStackCommandService {
 void registerTechStack(TechStackCreateRequest request);
}