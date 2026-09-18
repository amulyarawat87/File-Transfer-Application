CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP(3) NOT NULL,
    locked_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS file_metadata (
    file_id VARCHAR(255) NOT NULL,
    short_code VARCHAR(6) NOT NULL,
    file_name VARCHAR(255),
    file_type VARCHAR(255),
    file_size BIGINT NOT NULL,
    expiry_date_time TIMESTAMP(3) NOT NULL,
    encryption_key TEXT,
    PRIMARY KEY (file_id),
    CONSTRAINT uk_file_metadata_short_code UNIQUE (short_code)
);