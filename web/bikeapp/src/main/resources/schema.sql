CREATE TABLE IF NOT EXISTS trailgo_hazard_review (
    hazard_id INT NOT NULL,
    title VARCHAR(120) NULL,
    hazard_type VARCHAR(32) NOT NULL DEFAULT 'OTHER',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    approved_at DATETIME NULL,
    approved_by VARCHAR(100) NULL,
    rejection_reason VARCHAR(500) NULL,
    confirm_count INT NOT NULL DEFAULT 0,
    report_count INT NOT NULL DEFAULT 0,
    expires_at DATETIME NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    merged_into INT NULL,
    PRIMARY KEY (hazard_id),
    INDEX idx_trailgo_hazard_status (status),
    INDEX idx_trailgo_hazard_type (hazard_type),
    INDEX idx_trailgo_hazard_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trailgo_hazard_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    hazard_id INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_trailgo_hazard_image (hazard_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS trailgo_hazard_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    hazard_id INT NOT NULL,
    action VARCHAR(32) NOT NULL,
    previous_status VARCHAR(20) NULL,
    next_status VARCHAR(20) NULL,
    admin_id VARCHAR(100) NULL,
    reason VARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_trailgo_hazard_history (hazard_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT IGNORE INTO trailgo_hazard_review
    (hazard_id, title, hazard_type, status, created_at, updated_at, confirm_count, report_count, is_active)
SELECT idx,
       LEFT(COALESCE(da_content, '위험지역 제보'), 120),
       'OTHER',
       CASE da_status
           WHEN '1' THEN 'APPROVED'
           WHEN '2' THEN 'REJECTED'
           WHEN '3' THEN 'REVIEWING'
           WHEN '4' THEN 'INACTIVE'
           ELSE 'PENDING'
       END,
       COALESCE(create_date, CURRENT_TIMESTAMP),
       CURRENT_TIMESTAMP,
       0,
       0,
       CASE WHEN da_status = '4' THEN 0 ELSE 1 END
FROM dangerous_area;
