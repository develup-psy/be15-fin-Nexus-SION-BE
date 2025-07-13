package com.nexus.sion.feature.squad.command.application.service;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadCommentRegisterRequest;

public interface SquadCommentCommandService {

  void registerComment(String squadCode, SquadCommentRegisterRequest request, String employeeIdentificationNumber);

  void deleteComment(String squadCode, Long commentId);
}
