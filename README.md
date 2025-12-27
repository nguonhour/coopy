# Course Enrollment and Classroom Scheduling System

A comprehensive Spring Boot backend application for managing course enrollment, classroom scheduling, and attendance tracking with role-based access control.

## 🎯 Features

### Three User Roles

#### 👨‍💼 **ADMIN**
- Manage all users (Create, Read, Update, Delete)
- Manage courses and course offerings
- Manage academic terms/semesters
- Manage classrooms/rooms
- Assign lecturers to courses
- View system-wide reports
- Full system access

#### 👨‍🏫 **LECTURER**
- View assigned courses
- Manage class schedules
- Take student attendance
- View enrolled students roster
- Generate course reports
- View personal teaching schedule

#### 👨‍🎓 **STUDENT**
- Browse available courses
- Enroll in courses
- Drop courses
- View personal schedule
- View attendance records
- Manage personal profile
- Join waitlist for full courses

## 🏗️ Architecture

### Technology Stack
- **Framework:** Spring Boot 3.5.0
- **Java Version:** 21
- **Database:** MySQL
- **ORM:** JPA/Hibernate
- **Security:** Spring Security + JWT
- **Migration:** Flyway
- **Build Tool:** Maven
- **Lombok:** For boilerplate reduction

### Project Structure
```
src/
├── main/
│   ├── java/com/cource/
│   │   ├── config/          # Security, JWT, JPA configurations
│   │   ├── controller/      # REST API endpoints
│   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── auth/        # Authentication DTOs
│   │   │   ├── user/        # User DTOs
│   │   │   ├── course/      # Course & Term DTOs
│   │   │   ├── enrollment/  # Enrollment DTOs
│   │   │   ├── schedule/    # Schedule & Room DTOs
│   │   │   └── attendance/  # Attendance DTOs
│   │   ├── entity/          # JPA Entities (12 tables)
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic
│   │   ├── exception/       # Custom exceptions
│   │   └── util/            # Utility classes
│   └── resources/
│       ├── application.properties
│       └── db/migration/    # Flyway migration scripts
└── test/                    # Unit and integration tests
```

## 📊 Database Schema

### Core Tables (12)

1. **roles** - User roles (STUDENT, LECTURER, ADMIN)
2. **users** - System users with authentication
3. **user_profiles** - Extended user information
4. **academic_terms** - Academic semesters/terms
5. **courses** - Course catalog
6. **course_offerings** - Course instances per term
7. **course_lecturers** - Lecturer assignments
8. **enrollments** - Student course enrollments
9. **waitlist** - Course waitlist management
10. **rooms** - Physical classrooms
11. **class_schedules** - Class meeting times
12. **attendance** - Attendance tracking

### Entity Relationships
- Users → Roles (Many-to-One)
- UserProfiles → Users (One-to-One)
- CourseOfferings → Courses & Terms (Many-to-One each)
- CourseLecturers → CourseOfferings & Users (Many-to-One each)
- Enrollments → Users & CourseOfferings (Many-to-One each)
- Waitlist → Users & CourseOfferings (Many-to-One each)
- ClassSchedules → CourseOfferings & Rooms (Many-to-One each)
- Attendance → Enrollments, ClassSchedules & Users (Many-to-One each)

## 🚀 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- MySQL 8.0+
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE enrollment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Update `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/enrollment_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Run Application

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Default Users

After first run, Flyway will seed the database with:

| Email | Password | Role |
|-------|----------|------|
| admin@test.com | password | ADMIN |
| lecturer@test.com | password | LECTURER |
| student01@test.com | password | STUDENT |
| student02@test.com | password | STUDENT |
| student03@test.com | password | STUDENT |

## 📡 API Documentation

### Authentication Endpoints
```
POST   /api/auth/register    - Register new user
POST   /api/auth/login       - User login
POST   /api/auth/logout      - User logout
```

### User Management (Admin)
```
GET    /api/users            - List all users
GET    /api/users/{id}       - Get user by ID
POST   /api/users            - Create new user
PUT    /api/users/{id}       - Update user
DELETE /api/users/{id}       - Delete user
GET    /api/users/role/{code} - Get users by role
```

### Course Management
```
GET    /api/courses          - List all courses
GET    /api/courses/{id}     - Get course details
POST   /api/courses          - Create course (Admin)
PUT    /api/courses/{id}     - Update course (Admin)
DELETE /api/courses/{id}     - Delete course (Admin)
```

### Academic Terms
```
GET    /api/terms            - List all terms
GET    /api/terms/active     - List active terms
POST   /api/terms            - Create term (Admin)
```

### Course Offerings
```
GET    /api/offerings/term/{id}    - Get offerings by term
POST   /api/offerings              - Create offering (Admin)
POST   /api/offerings/{id}/lecturer - Assign lecturer (Admin)
```

### Enrollments
```
GET    /api/enrollments/student/{id}  - Student enrollments
GET    /api/enrollments/offering/{id} - Offering enrollments
POST   /api/enrollments                - Enroll student
DELETE /api/enrollments/{id}           - Drop enrollment
PUT    /api/enrollments/{id}/grade     - Update grade (Lecturer)
```

### Schedules & Rooms
```
GET    /api/schedules/offering/{id}  - Get course schedules
POST   /api/schedules                 - Create schedule (Admin)
GET    /api/rooms                     - List rooms
POST   /api/rooms                     - Create room (Admin)
```

### Attendance
```
GET    /api/attendance/student/{id}    - Student attendance
GET    /api/attendance/schedule/{id}   - Schedule attendance
POST   /api/attendance                  - Record attendance (Lecturer)
```

## 🔒 Security

### Authentication
- JWT (JSON Web Token) based authentication
- BCrypt password encryption
- Token expiration and refresh mechanism

### Authorization
- Role-based access control (RBAC)
- Method-level security annotations
- Endpoint protection based on user roles

### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // Configure security filters
    // Set up authentication providers
    // Define access rules
}
```

## 📝 Business Logic

### Enrollment Flow
1. Student browses available courses
2. Student requests enrollment
3. System checks course capacity
4. If space available → Enroll student
5. If full → Add to waitlist
6. When student drops → Auto-enroll from waitlist

### Waitlist Management
- Automatic position assignment
- FIFO (First In, First Out) processing
- Notification system ready
- Automatic cleanup on enrollment

### Attendance Tracking
- Lecturers mark attendance per class session
- Multiple status options: PRESENT, ABSENT, LATE, EXCUSED
- Attendance reports generation
- Historical attendance tracking

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## 📦 Deployment

### Build for Production
```bash
mvn clean package -DskipTests
```

The JAR file will be in `target/` directory.

### Docker Deployment (Optional)
```dockerfile
FROM openjdk:21-jdk-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## ✅ Completed Components

- ✅ Database schema with 12 tables
- ✅ Flyway migration scripts
- ✅ All entity classes with Lombok
- ✅ Repository interfaces with custom queries
- ✅ Complete DTO structure (Request/Response)
- ✅ Core service implementations:
  - AuthService
  - UserService
  - CourseService
  - EnrollmentService
- ✅ Three-role access system
- ✅ Seed data for testing

## 🚧 Next Steps

1. Complete remaining service implementations:
   - StudentService
   - LecturerService
   - AdminService
   - ScheduleService
   - AttendanceService

2. Implement REST controllers

3. Add JWT token generation and validation

4. Complete security configuration

5. Add input validation and error handling

6. Write unit and integration tests

7. Add API documentation (Swagger/OpenAPI)

8. Implement email notifications

9. Add pagination and sorting

10. Performance optimization

## 📚 Dependencies

Key dependencies in `pom.xml`:
```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>spring-boot-starter-web</dependency>
    <dependency>spring-boot-starter-data-jpa</dependency>
    <dependency>spring-boot-starter-security</dependency>
    <dependency>spring-boot-starter-validation</dependency>
    
    <!-- Database -->
    <dependency>mysql-connector-j</dependency>
    <dependency>flyway-core</dependency>
    <dependency>flyway-mysql</dependency>
    
    <!-- Utilities -->
    <dependency>lombok</dependency>
    <dependency>spring-boot-devtools</dependency>
    
    <!-- Testing -->
    <dependency>spring-boot-starter-test</dependency>
    <dependency>spring-security-test</dependency>
</dependencies>
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is for educational purposes.

## 👥 Authors

- **Backend Development**: Spring Boot Team
- **Database Design**: Based on course enrollment requirements
- **API Design**: RESTful principles

## 📞 Support

For questions or issues, please create an issue in the repository.

---

**Last Updated**: December 21, 2025
**Version**: 1.0.0
**Status**: Development In Progress
