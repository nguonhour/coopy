-- Seed enrollments for test students into the first course offering
-- This allows testing of the attendance and students pages

-- Enroll all test students (student01, student02, student03) into the GIC25DB course offering
INSERT INTO enrollments (student_id, offering_id, status, enrolled_at)
SELECT 
    u.id,
    (SELECT co.id FROM course_offerings co 
     JOIN courses c ON co.course_id = c.id 
     WHERE c.course_code = 'GIC25DB' LIMIT 1),
    'ENROLLED',
    NOW()
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE r.role_code = 'STUDENT'
  AND NOT EXISTS (
      SELECT 1 FROM enrollments e 
      WHERE e.student_id = u.id 
        AND e.offering_id = (SELECT co.id FROM course_offerings co 
                             JOIN courses c ON co.course_id = c.id 
                             WHERE c.course_code = 'GIC25DB' LIMIT 1)
  );
