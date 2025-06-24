package com.nexus.sion.feature.squad.command.application.service;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadCommentRegisterRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadComment;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SquadCommentCommandService {

    private final SquadCommentRepository squadCommentRepository;

    public void registerComment(String squadCode, SquadCommentRegisterRequest request) {
        // squadCode는 여기서 엔티티에 세팅
        SquadComment comment = SquadComment.builder()
                .squadCode(squadCode)
                .employeeIdentificationNumber(request.getEmployeeIdentificationNumber())
                .content(request.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        squadCommentRepository.save(comment);
    }

}
