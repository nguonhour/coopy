-- Create course_lecturers table (from V1__init_schema.sql)
CREATE TABLE IF NOT EXISTS
    course_lecturers (
        id BIGINT AUTO_INCREMENT PRIMARY KEY,
        offering_id BIGINT NOT NULL,
        lecturer_id BIGINT NOT NULL,
        is_primary BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (offering_id) REFERENCES course_offerings (id) ON DELETE CASCADE,
        FOREIGN KEY (lecturer_id) REFERENCES users (id) ON DELETE CASCADE,
        UNIQUE KEY uk_course_lecturer (offering_id, lecturer_id),
        INDEX idx_lecturer_id (lecturer_id),
        INDEX idx_lecturer_primary (is_primary),
        INDEX idx_lecturer_offering (offering_id)
    );