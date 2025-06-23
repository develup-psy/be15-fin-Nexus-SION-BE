package com.nexus.sion.feature.techstack.command.application.service;

import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackCreateRequest;
import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TechStackCommandServiceImpl implements TechStackCommandService {

    private final ModelMapper modelMapper;
    private final TechStackRepository techStackRepository;

    public void registerTechStack(TechStackCreateRequest request) {;
        // 기존에 존재하는 기술스택은 저장하지 않고 종료
        if(techStackRepository.existsById(request.getTechStackName())) {
            return;
        }

        TechStack techStack = modelMapper.map(request, TechStack.class);
        techStackRepository.save(techStack);
    }
}
