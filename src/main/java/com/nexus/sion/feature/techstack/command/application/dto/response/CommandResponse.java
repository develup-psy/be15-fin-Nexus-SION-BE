package com.nexus.sion.feature.techstack.command.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommandResponse {
  /* CoffeechatCommandResponse 등으로 이름 변경하여 사용 */
  private final Long coffeechatId;
}
