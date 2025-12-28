-- Assign lecturer_id = 1 to all offerings (no-op for existing rows)
INSERT INTO
    course_lecturers (offering_id, lecturer_id, is_primary, created_at)
SELECT
    id,
    1,
    TRUE,
    NOW()
FROM
    course_offerings ON DUPLICATE KEY
UPDATE is_primary =
VALUES
    (is_primary);