CREATE TABLE
    `user_certificate_history` (
                                   `user_certificate_history_id` BIGINT NOT NULL AUTO_INCREMENT,
                                   `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   `updated_by` VARCHAR(30) NULL,
                                   `certificate_name` VARCHAR(100) NOT NULL,
                                   `employee_identification_number` VARCHAR(30) NOT NULL,
                                   PRIMARY KEY (`user_certificate_history_id`)
);

CREATE TABLE
    `job` (
              `name` VARCHAR(30) NOT NULL,
              `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
              `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
              PRIMARY KEY (`name`)
);

CREATE TABLE
    `project_evaluation` (
                             `project_evaluation_id` BIGINT NOT NULL,
                             `project_code` VARCHAR(30) NOT NULL,
                             `developer_id` BIGINT NOT NULL,
                             `estimator_id` BIGINT NOT NULL,
                             `tech_fit` INT NOT NULL,
                             `communication` INT NOT NULL,
                             `problem_solving` INT NOT NULL,
                             `cooperation_attitude` INT NOT NULL,
                             `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`project_evaluation_id`)
);

CREATE TABLE
    `project_fp_summary` (
                             `project_fp_summary_id` BIGINT NOT NULL AUTO_INCREMENT,
                             `total_fp` INT NOT NULL,
                             `avg_effort_per_fp` INT NOT NULL,
                             `total_effort` DECIMAL(6, 2) NULL,
                             `estimated_duration` DECIMAL(5, 2) NULL,
                             `estimated_cost` DECIMAL(12, 2) NULL,
                             `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `project_code` VARCHAR(30) NOT NULL,
                             PRIMARY KEY (`project_fp_summary_id`)
);

CREATE TABLE
    `project_function_estimate` (
                                    `project_function_estimate_id` BIGINT NOT NULL AUTO_INCREMENT,
                                    `function_name` VARCHAR(100) NOT NULL,
                                    `function_type` ENUM ('EI', 'EO', 'EQ', 'ILF', 'EIF') NOT NULL,
                                    `complexity` ENUM ('SIMPLE', 'MEDIUM', 'COMPLEX') NOT NULL,
                                    `function_score` INT NOT NULL,
                                    `description` TEXT NULL,
                                    `related_tables_count` INT NOT NULL,
                                    `related_fields_count` INT NULL,
                                    `created_at` TIMESTAMP NULL,
                                    `project_fp_summary_id` BIGINT NOT NULL,
                                    PRIMARY KEY (`project_function_estimate_id`)
);

CREATE TABLE
    `initial_score` (
                        `id` BIGINT NOT NULL,
                        `years` INT NOT NULL,
                        `score` INT NOT NULL,
                        `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`)
);

CREATE TABLE
    `squad_comment` (
                        `comment_id` BIGINT NOT NULL,
                        `squad_code` VARCHAR(30) NOT NULL,
                        `employee_identification_number` VARCHAR(30) NOT NULL,
                        `content` TEXT NOT NULL,
                        `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`comment_id`)
);

CREATE TABLE
    `project` (
                  `project_code` VARCHAR(30) NOT NULL,
                  `description` TEXT NOT NULL,
                  `title` VARCHAR(30) NOT NULL,
                  `budget` BIGINT NOT NULL,
                  `start_date` DATE NOT NULL,
                  `expected_end_date` DATE NOT NULL,
                  `actual_end_date` DATE NOT NULL,
                  `status` ENUM (
                      'WAITING',
                      'IN_PROGRESS',
                      'COMPLETE',
                      'INCOMPLETE'
                      ) NOT NULL,
                  `number_of_members` INT NULL,
                  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  `deleted_at` TIMESTAMP NULL,
                  `client_code` VARCHAR(30) NOT NULL,
                  `request_specification_url` VARCHAR(255) NOT NULL,
                  `name` VARCHAR(30) NOT NULL,
                  PRIMARY KEY (`project_code`)
);

CREATE TABLE
    `grade` (
                `grade_code` ENUM ('S', 'A', 'B', 'C', 'D') NOT NULL,
                `min_score` INT NOT NULL,
                `max_score` INT NULL,
                `productivity` DECIMAL(10, 4) NOT NULL,
                `monthly_unit_price` INT NOT NULL,
                `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                PRIMARY KEY (`grade_code`)
);

CREATE TABLE
    `project_and_job` (
                          `project_and_job_id` BIGINT NOT NULL,
                          `required_number` INT NOT NULL,
                          `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          `project_code` VARCHAR(30) NOT NULL,
                          `job_name` VARCHAR(30) NOT NULL,
                          PRIMARY KEY (`project_and_job_id`)
);

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

CREATE TABLE
    `developer_tech_stack_history` (
                                       `developer_tech_stack_history_id` BIGINT NOT NULL,
                                       `developer_tech_stack_id` BIGINT NOT NULL,
                                       `added_score` INT NOT NULL,
                                       `cumulative_score` INT NOT NULL,
                                       `related_table` ENUM (
                                           'PROJECT',
                                           'TRAINING_PROGRAM',
                                           'USER_CERTIFICATE_HISTORY'
                                           ) NOT NULL,
                                       `related_id` BIGINT NOT NULL,
                                       `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       PRIMARY KEY (`developer_tech_stack_history_id`)
);

CREATE TABLE
    `developer_tech_stack` (
                               `developer_tech_stack_id` BIGINT NOT NULL,
                               `tech_stack_total_scores` INT NOT NULL,
                               `employee_identification_number` VARCHAR(30) NOT NULL,
                               `tech_stack_name` VARCHAR(30) NOT NULL,
                               `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                               PRIMARY KEY (`developer_tech_stack_id`)
);

CREATE TABLE
    `domain` (
                 `name` VARCHAR(30) NOT NULL,
                 `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                 `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                 PRIMARY KEY (`name`)
);

CREATE TABLE
    `job_and_tech_stack` (
                             `job_and_tech_stack_id` BIGINT NOT NULL,
                             `tech_stack_name` VARCHAR(30) NOT NULL,
                             `project_and_job_id` BIGINT NOT NULL,
                             `priority` INT NULL,
                             `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             PRIMARY KEY (`job_and_tech_stack_id`)
);

CREATE TABLE
    `squad` (
                `squad_code` VARCHAR(30) NOT NULL,
                `project_code` VARCHAR(30) NOT NULL,
                `title` VARCHAR(30) NOT NULL,
                `description` VARCHAR(255) NULL,
                `is_active` BOOLEAN NOT NULL,
                `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                `estimated_duration` DECIMAL(5, 2) NULL,
                `estimated_cost` DECIMAL(12, 2) NULL,
                `origin_type` ENUM ('AI', 'MANUAL') NOT NULL,
                `recommendation_reason` TEXT NULL,
                PRIMARY KEY (`squad_code`)
);

CREATE TABLE
    `tech_stack` (
                     `tech_stack_name` VARCHAR(30) NOT NULL,
                     `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                     `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                     PRIMARY KEY (`tech_stack_name`)
);

CREATE TABLE
    `user_training_history` (
                                `user_trainig_history_id` BIGINT NOT NULL,
                                `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                `updated_by` VARCHAR(30) NOT NULL,
                                `training_name` VARCHAR(100) NOT NULL,
                                `employee_identification_number` VARCHAR(30) NOT NULL,
                                PRIMARY KEY (`user_trainig_history_id`)
);

CREATE TABLE
    `training_program` (
                           `training_name` VARCHAR(100) NOT NULL,
                           `description` VARCHAR(255) NULL,
                           `training_type` ENUM ('IN', 'OUT') NOT NULL,
                           `organizer` VARCHAR(30) NOT NULL,
                           `started_at` TIMESTAMP NOT NULL,
                           `ended_at` TIMESTAMP NOT NULL,
                           `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           PRIMARY KEY (`training_name`)
);

CREATE TABLE
    `squad_employee` (
                         `squad_employee_id` BIGINT NOT NULL,
                         `assigned_date` DATE NOT NULL,
                         `employee_identification_number` VARCHAR(30) NOT NULL,
                         `project_and_job_id` BIGINT NOT NULL,
                         `is_leader` BOOLEAN NOT NULL DEFAULT 0,
                         `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         `squad_code` VARCHAR(30) NOT NULL,
                         `total_skill_score` INT NULL,
                         PRIMARY KEY (`squad_employee_id`)
);

CREATE TABLE
    `member` (
                 `employee_identification_number` VARCHAR(30) NOT NULL,
                 `employee_name` VARCHAR(30) NOT NULL,
                 `password` VARCHAR(255) NOT NULL,
                 `profile_image_url` VARCHAR(100) NULL,
                 `phone_number` VARCHAR(11) NOT NULL,
                 `joined_at` TIMESTAMP NULL,
                 `email` VARCHAR(30) NOT NULL,
                 `career_years` INT NULL DEFAULT 1,
                 `salary` BIGINT NULL,
                 `status` ENUM ('AVAILABLE', 'UNAVAILABLE', 'IN_PROJECT') NULL,
                 `grade_code` ENUM ('S', 'A', 'B', 'C', 'D') NULL,
                 `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                 `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                 `deleted_at` TIMESTAMP NULL,
                 `role` ENUM ('ADMIN', 'INSIDER', 'OUTSIDER') NOT NULL,
                 PRIMARY KEY (`employee_identification_number`)
);

CREATE TABLE
    `certificate` (
                      `certificate_name` VARCHAR(100) NOT NULL,
                      `issuing_organization_id` TIMESTAMP NOT NULL,
                      `issue_date` TIMESTAMP NOT NULL,
                      `expiry_date` TIMESTAMP NOT NULL,
                      `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      PRIMARY KEY (`certificate_name`)
);

ALTER TABLE `user_certificate_history` ADD CONSTRAINT `FK_developer_TO_user_certificate_history_1` FOREIGN KEY (`updated_by`) REFERENCES `member` (`employee_identification_number`);

ALTER TABLE `user_certificate_history` ADD CONSTRAINT `FK_developer_TO_user_certificate_history_2` FOREIGN KEY (`employee_identification_number`) REFERENCES `member` (`employee_identification_number`);

ALTER TABLE `user_certificate_history` ADD CONSTRAINT `FK_certificate_TO_user_certificate_history_1` FOREIGN KEY (`certificate_name`) REFERENCES `certificate` (`certificate_name`);

ALTER TABLE `project_evaluation` ADD CONSTRAINT `FK_project_TO_project_evaluation_1` FOREIGN KEY (`project_code`) REFERENCES `project` (`project_code`);

ALTER TABLE `project_evaluation` ADD CONSTRAINT `FK_squad_employee_TO_project_evaluation_1` FOREIGN KEY (`developer_id`) REFERENCES `squad_employee` (`squad_employee_id`);

ALTER TABLE `project_evaluation` ADD CONSTRAINT `FK_squad_employee_TO_project_evaluation_2` FOREIGN KEY (`estimator_id`) REFERENCES `squad_employee` (`squad_employee_id`);

ALTER TABLE `project_fp_summary` ADD CONSTRAINT `FK_project_TO_project_fp_summary_1` FOREIGN KEY (`project_code`) REFERENCES `project` (`project_code`);

ALTER TABLE `project_function_estimate` ADD CONSTRAINT `FK_project_fp_summary_TO_project_function_estimate_1` FOREIGN KEY (`project_fp_summary_id`) REFERENCES `project_fp_summary` (`project_fp_summary_id`);

ALTER TABLE `squad_comment` ADD CONSTRAINT `FK_squad_TO_squad_comment_1` FOREIGN KEY (`squad_code`) REFERENCES `squad` (`squad_code`);

ALTER TABLE `squad_comment` ADD CONSTRAINT `FK_member_TO_squad_comment_1` FOREIGN KEY (`employee_identification_number`) REFERENCES `member` (`employee_identification_number`);

ALTER TABLE `project` ADD CONSTRAINT `FK_client_company_TO_project_1` FOREIGN KEY (`client_code`) REFERENCES `client_company` (`client_code`);

ALTER TABLE `project` ADD CONSTRAINT `FK_domain_TO_project_1` FOREIGN KEY (`name`) REFERENCES `domain` (`name`);

ALTER TABLE `project_and_job` ADD CONSTRAINT `FK_project_TO_project_and_job_1` FOREIGN KEY (`project_code`) REFERENCES `project` (`name`);

ALTER TABLE `project_and_job` ADD CONSTRAINT `FK_job_TO_project_and_job_1` FOREIGN KEY (`job_name`) REFERENCES `job` (`name`);

ALTER TABLE `client_company` ADD CONSTRAINT `FK_domain_TO_client_company_1` FOREIGN KEY (`domain_name`) REFERENCES `domain` (`name`);

ALTER TABLE `developer_tech_stack_history` ADD CONSTRAINT `FK_developer_tech_stack_TO_developer_tech_stack_history_1` FOREIGN KEY (`developer_tech_stack_id`) REFERENCES `developer_tech_stack` (`developer_tech_stack_id`);

ALTER TABLE `developer_tech_stack` ADD CONSTRAINT `FK_developer_TO_developer_tech_stack_1` FOREIGN KEY (`employee_identification_number`) REFERENCES `member` (`employee_identification_number`);

ALTER TABLE `developer_tech_stack` ADD CONSTRAINT `FK_tech_stack_TO_developer_tech_stack_1` FOREIGN KEY (`tech_stack_name`) REFERENCES `tech_stack` (`tech_stack_name`);

ALTER TABLE `job_and_tech_stack` ADD CONSTRAINT `FK_tech_stack_TO_job_and_tech_stack_1` FOREIGN KEY (`tech_stack_name`) REFERENCES `tech_stack` (`tech_stack_name`);

ALTER TABLE `job_and_tech_stack` ADD CONSTRAINT `FK_project_and_job_TO_job_and_tech_stack_1` FOREIGN KEY (`project_and_job_id`) REFERENCES `project_and_job` (`project_and_job_id`);

ALTER TABLE `squad` ADD CONSTRAINT `FK_project_TO_squad_1` FOREIGN KEY (`project_code`) REFERENCES `project` (`project_code`);

ALTER TABLE `user_training_history` ADD CONSTRAINT `FK_developer_TO_user_training_history_1` FOREIGN KEY (`updated_by`) REFERENCES `member` (`employee_identification_number`);

ALTER TABLE `user_training_history` ADD CONSTRAINT `FK_developer_TO_user_training_history_2` FOREIGN KEY (`employee_identification_number`) REFERENCES `member` (`employee_identification_number`);

ALTER TABLE `user_training_history` ADD CONSTRAINT `FK_training_program_TO_user_training_history_1` FOREIGN KEY (`training_name`) REFERENCES `training_program` (`training_name`);

ALTER TABLE `squad_employee` ADD CONSTRAINT `FK_developer_TO_squad_employee_1` FOREIGN KEY (`employee_identification_number`) REFERENCES `member` (`employee_identification_number`);

ALTER TABLE `squad_employee` ADD CONSTRAINT `FK_project_and_job_TO_squad_employee_1` FOREIGN KEY (`project_and_job_id`) REFERENCES `project_and_job` (`project_and_job_id`);

ALTER TABLE `squad_employee` ADD CONSTRAINT `FK_squad_TO_squad_employee_1` FOREIGN KEY (`squad_code`) REFERENCES `squad` (`squad_code`);

ALTER TABLE `member` ADD CONSTRAINT `FK_grade_TO_developer_1` FOREIGN KEY (`grade_code`) REFERENCES `grade` (`grade_code`);