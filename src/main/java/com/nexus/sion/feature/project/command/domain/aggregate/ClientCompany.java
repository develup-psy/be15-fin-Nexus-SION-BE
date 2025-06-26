package com.nexus.sion.feature.project.command.domain.aggregate;

import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyUpdateRequest;
import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;

import lombok.*;

@Entity
@Table(name = "client_company")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientCompany extends BaseTimeEntity {

    @Id
    @Column(name = "client_code", length = 30)
    private String clientCode;

    @Column(name = "company_name", length = 100, nullable = false, unique = true)
    private String companyName;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "email", length = 30)
    private String email;

    @Column(name = "contact_number", length = 21)
    private String contactNumber;

    @Column(name = "domain_name", length = 30, nullable = false)
    private String domainName;

    // for testing
    public static ClientCompany of(ClientCompanyCreateRequest request, int serialNumber) {
        String prefix = request.getCompanyName().substring(0, 2).toLowerCase();
        String clientCode = String.format("%s_%03d", prefix, serialNumber);

        return ClientCompany.builder()
                .clientCode(clientCode)
                .companyName(request.getCompanyName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .contactNumber(request.getContactNumber())
                .domainName(request.getDomainName())
                .build();
    }

    public void update(ClientCompanyUpdateRequest request) {
        if (request.getCompanyName() != null) {
            this.companyName = request.getCompanyName();
        }
        if (request.getDomainName() != null) {
            this.domainName = request.getDomainName();
        }
        if (request.getContactPerson() != null) {
            this.contactPerson = request.getContactPerson();
        }
        if (request.getEmail() != null) {
            this.email = request.getEmail();
        }
        if (request.getContactNumber() != null) {
            this.contactNumber = request.getContactNumber();
        }
    }
}
