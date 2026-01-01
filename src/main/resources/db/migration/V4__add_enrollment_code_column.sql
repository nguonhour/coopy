-- Add enrollment_code column to course_offerings (MySQL 8.0 compatible)
-- Check if column exists before adding
SET
    @dbname = DATABASE();

SET
    @tablename = 'course_offerings';

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
                    ' VARCHAR(16)'
                )
            )
    );

PREPARE alterIfNotExists
FROM
    @preparedStatement;

EXECUTE alterIfNotExists;

DEALLOCATE PREPARE alterIfNotExists;