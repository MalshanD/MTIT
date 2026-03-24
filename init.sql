-- ============================================
-- Online Learning Platform - Database Init
-- Creates separate databases for each service
-- ============================================

CREATE DATABASE IF NOT EXISTS auth_db;
CREATE DATABASE IF NOT EXISTS student_db;
CREATE DATABASE IF NOT EXISTS course_db;
CREATE DATABASE IF NOT EXISTS instructor_db;
CREATE DATABASE IF NOT EXISTS enrollment_db;
CREATE DATABASE IF NOT EXISTS quiz_db;
CREATE DATABASE IF NOT EXISTS certificate_db;

-- Grant all privileges to the application user
GRANT ALL PRIVILEGES ON auth_db.* TO 'learning_user'@'%';
GRANT ALL PRIVILEGES ON student_db.* TO 'learning_user'@'%';
GRANT ALL PRIVILEGES ON course_db.* TO 'learning_user'@'%';
GRANT ALL PRIVILEGES ON instructor_db.* TO 'learning_user'@'%';
GRANT ALL PRIVILEGES ON enrollment_db.* TO 'learning_user'@'%';
GRANT ALL PRIVILEGES ON quiz_db.* TO 'learning_user'@'%';
GRANT ALL PRIVILEGES ON certificate_db.* TO 'learning_user'@'%';

FLUSH PRIVILEGES;
