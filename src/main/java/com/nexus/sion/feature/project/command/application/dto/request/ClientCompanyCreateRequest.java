package com.nexus.sion.feature.project.command.application.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
/*
*
CREATE TABLE
        `client_company` (
                `client_code` VARCHAR(30) NOT NULL,
                `company_name` VARCHAR(255) NULL,
                `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                `contact_person` VARCHAR(10) NULL,
                `email` VARCHAR(30) NULL,
                `contact_number` VARCHAR(21) NULL,
                `domain_name` VARCHAR(30) NOT NULL,
                PRIMARY KEY (`client_code`)
        );
* */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientCompanyCreateRequest {

}
