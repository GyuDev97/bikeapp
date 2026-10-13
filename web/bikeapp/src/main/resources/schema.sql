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

-- V20261013_1: riding_record weather snapshot (same as db/migration/V20261013_1__riding_record_weather_snapshot.sql)
-- Weather snapshot of a ride, captured once when the record is saved (never refreshed on read).
-- All columns are nullable: existing rides, rides without coordinates, a missing KMA key and
-- external API failures simply leave them NULL.
-- sky/precipitation hold the normalised app values (CLEAR, PARTLY_CLOUDY, CLOUDY, UNKNOWN /
-- NONE, RAIN, RAIN_SNOW, SNOW, SHOWER), never raw KMA codes. rr_weather_observed_at is Asia/Seoul
-- wall-clock time. Do not put weather into rr_high (accumulated climb) or rr_area.
-- IF NOT EXISTS (MariaDB) keeps the statement safe to run on every start-up via schema.sql.
ALTER TABLE riding_record
    ADD COLUMN IF NOT EXISTS rr_weather_observed_at DATETIME NULL,
    ADD COLUMN IF NOT EXISTS rr_weather_nx INT NULL,
    ADD COLUMN IF NOT EXISTS rr_weather_ny INT NULL,
    ADD COLUMN IF NOT EXISTS rr_weather_temperature_c DECIMAL(4,1) NULL,
    ADD COLUMN IF NOT EXISTS rr_weather_sky_code VARCHAR(16) NULL,
    ADD COLUMN IF NOT EXISTS rr_weather_precipitation_code VARCHAR(16) NULL,
    ADD COLUMN IF NOT EXISTS rr_weather_label VARCHAR(40) NULL;
