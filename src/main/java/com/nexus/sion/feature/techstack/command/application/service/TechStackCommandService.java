package com.nexus.sion.feature.techstack.command.application.service;

import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackRequest;


public interface TechStackCommandService {
 void registerTechStack(TechStackRequest request);

 void removeTechStack(TechStackRequest request);
}