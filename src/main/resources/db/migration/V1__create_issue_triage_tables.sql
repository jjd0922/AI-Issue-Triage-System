CREATE TABLE `issue` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `source` VARCHAR(50) NOT NULL,
    `status` VARCHAR(50) NOT NULL,
    `failure_reason` VARCHAR(1000) NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    `analysis_requested_at` DATETIME(6) NULL,
    `analysis_started_at` DATETIME(6) NULL,
    `analysis_completed_at` DATETIME(6) NULL,
    `closed_at` DATETIME(6) NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_issue_status_created_at` (`status`, `created_at`),
    INDEX `idx_issue_source_created_at` (`source`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `issue_analysis` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `issue_id` BIGINT NOT NULL,
    `category` VARCHAR(50) NOT NULL,
    `priority` VARCHAR(50) NOT NULL,
    `summary` VARCHAR(1000) NOT NULL,
    `recommendation` VARCHAR(2000) NOT NULL,
    `confidence` DOUBLE NOT NULL,
    `model_name` VARCHAR(100) NOT NULL,
    `raw_response` TEXT NULL,
    `created_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_issue_analysis_issue_created_at` (`issue_id`, `created_at`),
    CONSTRAINT `fk_issue_analysis_issue`
        FOREIGN KEY (`issue_id`) REFERENCES `issue` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `knowledge_document` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(300) NOT NULL,
    `content` TEXT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_knowledge_document_title` (`title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `analysis_reference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `issue_analysis_id` BIGINT NOT NULL,
    `knowledge_document_id` BIGINT NOT NULL,
    `score` DOUBLE NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_analysis_reference_analysis` (`issue_analysis_id`),
    INDEX `idx_analysis_reference_document` (`knowledge_document_id`),
    CONSTRAINT `fk_analysis_reference_analysis`
        FOREIGN KEY (`issue_analysis_id`) REFERENCES `issue_analysis` (`id`),
    CONSTRAINT `fk_analysis_reference_document`
        FOREIGN KEY (`knowledge_document_id`) REFERENCES `knowledge_document` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
