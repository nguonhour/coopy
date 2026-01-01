-- Create attendance_codes table for QR code / short-code attendance check-in (idempotent)
CREATE TABLE IF NOT EXISTS
    attendance_codes (
        id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
        schedule_id BIGINT NOT NULL,
        code VARCHAR(20) NOT NULL,
        issued_at BIGINT NOT NULL,
        created_by BIGINT,
        present_window_minutes INT,
        late_window_minutes INT,
        INDEX idx_attendance_codes_schedule (schedule_id)
    );