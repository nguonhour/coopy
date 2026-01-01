-- Add enrollment_code column to courses table (MySQL 8.0 compatible)
SET
    @dbname = DATABASE();

SET
    @tablename = 'courses';

SET
    @columnname = 'enrollment_code';

SET
    @preparedStatement = (
        SELECT
            IF(
                (
                    SELECT
                        COUNT(*)
                    FROM
                        INFORMATION_SCHEMA.COLUMNS
                    WHERE
                        TABLE_SCHEMA = @dbname
                        AND TABLE_NAME = @tablename
                        AND COLUMN_NAME = @columnname
                ) > 0,
                'SELECT 1',
                CONCAT(
                    'ALTER TABLE ',
                    @tablename,
                    ' ADD COLUMN ',
                    @columnname,
                    ' VARCHAR(10) UNIQUE'
                )
            )
    );

PREPARE alterIfNotExists
FROM
    @preparedStatement;

EXECUTE alterIfNotExists;

DEALLOCATE PREPARE alterIfNotExists;

-- Generate unique enrollment codes for existing courses
UPDATE courses
SET
    enrollment_code = CONCAT(
        SUBSTRING(course_code, 1, 3),
        LPAD(FLOOR(RAND() * 10000), 4, '0')
    )
WHERE
    enrollment_code IS NULL;