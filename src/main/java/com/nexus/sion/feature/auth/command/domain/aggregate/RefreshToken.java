package com.nexus.sion.feature.auth.command.domain.aggregate;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken implements Serializable {
  private String token;
}
