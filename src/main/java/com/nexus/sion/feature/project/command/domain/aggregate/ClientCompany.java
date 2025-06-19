package com.nexus.sion.feature.project.command.domain.aggregate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "client_company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCompany {

    @Id
    @Column(name = "client_code", length = 30)
    private String clientCode;

    @Column(name = "company_name", length = 100, nullable = false)
    private String companyName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "email", length = 30)
    private String email;

    @Column(name = "contact_number", length = 21)
    private String contactNumber;

    @Column(name = "domain_name", length = 30, nullable = false)
    private String domainName;
}
