package com.nexus.sion.feature.techstack.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackRequest;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TechStackCommandServiceImpl implements TechStackCommandService {

  private final ModelMapper modelMapper;
  private final TechStackRepository techStackRepository;

    public void registerTechStack(TechStackRequest request) {
        // 기존에 존재하는 기술스택은 저장하지 않고 종료
        if(techStackRepository.existsById(request.getTechStackName())) {
            return;
        }

        TechStack techStack = modelMapper.map(request, TechStack.class);
        techStackRepository.save(techStack);
    }

    @Override
    public void removeTechStack(TechStackRequest request) {
        // 기존에 해당 기술스택이 없으면 에러
        if(!techStackRepository.existsById(request.getTechStackName())) {
            throw new BusinessException(ErrorCode.TECH_STACK_NOT_FOUND);
        }

        // 해당 기술스택 삭제
        techStackRepository.deleteById(request.getTechStackName());
    }
}
