package com.nexus.sion.feature.squad.query.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DbDiagnosticInfo {
    private String connectedDatabase;
    private String connectedUser;
}
