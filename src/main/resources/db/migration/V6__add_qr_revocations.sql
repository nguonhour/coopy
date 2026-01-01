-- Create table to track revoked QR codes by schedule
CREATE TABLE IF NOT EXISTS
    qr_revocation (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        schedule_id BIGINT NOT NULL UNIQUE,
        revoked_at TIMESTAMP NOT NULL
    );