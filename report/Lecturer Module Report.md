# Lecturer Module Report (Role: LECTURER)

**Project:** Course Enrollment and Classroom Scheduling System  
**Tech Stack:** Spring Boot 3.5.0, Java 21, Spring MVC, Spring Security (JWT), Spring Data JPA, Thymeleaf, MySQL, Flyway  
**Report Date:** 2026-01-04

---

## 1) Purpose & Scope

The **Lecturer Module** provides lecturer-only features for managing teaching activities and monitoring course performance. It focuses on:

- Lecturer dashboard navigation
- Viewing assigned courses and schedules
- Viewing enrolled students for lecturer-owned course offerings
- Attendance management (view, create, update, delete)
- Lecturer analytics & reporting (attendance trends, pass rate, course summary)
- Exporting lecturer data as CSV/PDF

This module is secured so that a lecturer can access **only** data that belongs to course offerings they teach.

---

## 2) Lecturer Features (Functional Requirements)

### 2.1 Lecturer Portal Pages

Lecturer pages are rendered using Thymeleaf and are grouped under `/lecturer/**`.

- **Dashboard**: quick overview and navigation
- **Courses**: list of courses/offerings taught by lecturer
- **Students**: view enrolled students per lecturer-owned course offering
- **Schedule**: view class schedules for lecturer-owned offerings
- **Attendance**: manage attendance records for a selected schedule/offering
- **Reports & Analytics**: data-driven summary cards + charts + course detail

Key server-side view controller:

- `src/main/java/com/course/controller/LecturerViewController.java`

Key lecturer UI templates:

- `src/main/resources/templates/views/lecturer/attendance.html`
- `src/main/resources/templates/views/lecturer/reports.html`

---

## 3) Attendance Management (Lecturer)

### 3.1 Overview

Lecturers can:

- List attendance records (per schedule / offering)
- Add attendance for a student
- Edit an existing attendance record
- Delete an attendance record
- Approve/Reject (workflow action endpoint)

The attendance UI uses a modal + JavaScript `fetch()` calls to lecturer REST endpoints.

Template:

- `src/main/resources/templates/views/lecturer/attendance.html`

### 3.2 APIs Used (Attendance)

The attendance page communicates with REST endpoints (typical patterns):

- `POST /api/lecturer/attendance?studentId={id}&status={status}`
- `PUT /api/lecturer/attendance/{attendanceId}?lecturerId={lecturerId}`
- `DELETE /api/lecturer/attendance/{attendanceId}?lecturerId={lecturerId}`
- `POST /api/lecturer/attendance/{attendanceId}/action?action=approve|reject`

> Notes:
>
> - The `lecturerId` query parameter is used for ownership checks in lecturer flows.
> - Editing and deleting validate that the attendance record belongs to an offering owned by the lecturer.

### 3.3 Data Access Layer (Attendance)

Repository additions support reporting and filtering by offering/date range:

- `src/main/java/com/course/repository/AttendanceRepository.java`
  - `countByOfferingAndDateRange(...)`
  - `countByOfferingAndDateRangeWithStatuses(...)`
  - `countByOfferingIdsBetween(...)`

---

## 4) Reports & Analytics (Lecturer-only)

### 4.1 Overview

The lecturer report page is designed to give a lecturer insight into:

- **Average Attendance (%)** (filtered by date range, optionally by course offering)
- **Pass Rate (%)** (based on student grades in an offering)
- **Active enrollments** (counts by offering(s) and enrollment status)
- **Total classes** (count of schedules across lecturer offerings)
- **Attendance trend chart** (counts by date)
- **Course performance chart** (avg grade by course OR grade distribution when a course is selected)

View route:

- `GET /lecturer/reports?lecturerId=...&offeringId=...&from=YYYY-MM-DD&to=YYYY-MM-DD&studentStatus=...`

Server-side controller logic:

- `src/main/java/com/course/controller/LecturerViewController.java` (method `reports(...)`)

### 4.2 Report Filters

Filters are implemented directly on the reports page:

- Date range: `from`, `to`
- Course filter: `offeringId` (All Courses or a specific course offering)
- Student/enrollment status: `studentStatus` (e.g., ENROLLED, COMPLETED, DROPPED, FAILED)

UI template:

- `src/main/resources/templates/views/lecturer/reports.html`

### 4.3 Metrics Computation (Service Layer)

The `LecturerService` provides aggregated metrics with ownership verification.

- Interface:
  - `src/main/java/com/course/service/LecturerService.java`
- Implementation:
  - `src/main/java/com/course/service/impl/LecturerServiceImpl.java`

Key methods:

- `calculateAverageAttendance(lecturerId, from, to, offeringId, studentStatus)`
- `calculatePassRate(lecturerId, offeringId, studentStatus)`
- `getCourseReports(lecturerId, from, to, studentStatus)`
- `getDetailedCourseReport(lecturerId, offeringId, from, to, studentStatus)`
- `getAttendanceCountsByDateRange(lecturerId, from, to, offeringId, studentStatus)`

### 4.4 DTOs (Reports)

Report DTOs are used to keep the view clean and avoid leaking entity graphs:

- `src/main/java/com/course/dto/lecturer/LecturerCourseReportDTO.java`
- `src/main/java/com/course/dto/lecturer/LecturerCourseDetailDTO.java`

These DTOs support:

- Course summary rows
- Grade distribution (A/B/C/D/F/W/I)
- Student grade list

---

## 5) Export Features (CSV/PDF)

### 5.1 Overview

The lecturer can export report data:

- Attendance CSV (filtered by date range + student status)
- Grades CSV (filtered by student status)
- PDF report summary (selected offering optional)

Controller:

- `src/main/java/com/course/controller/LecturerExportController.java`

Routes:

- `GET /lecturer/reports/export/attendance.csv?offeringId=...&lecturerId=...&from=...&to=...&studentStatus=...`
- `GET /lecturer/reports/export/grades.csv?offeringId=...&lecturerId=...&studentStatus=...`
- `GET /lecturer/reports/export/report.pdf?lecturerId=...&offeringId=...&from=...&to=...&studentStatus=...`

### 5.2 PDF Generation Library

PDF export uses **Apache PDFBox**:

- Dependency: `org.apache.pdfbox:pdfbox:2.0.30`
- Added in: `pom.xml`

The PDF includes:

- Lecturer ID
- Filter settings (date range, course filter, status filter)
- Summary metrics (avg attendance, pass rate)
- Top course performance listing

---

## 6) Security & Access Control

### 6.1 Lecturer-only Access

The lecturer module is protected using Spring Security. `/lecturer/**` endpoints require the lecturer role (e.g., `ROLE_LECTURER`). Authentication is based on JWT.

Key points:

- Lecturer views and exports are designed not to depend on admin-only methods.
- Ownership verification is performed for offering-specific operations (for example, when exporting or viewing course-specific reports).

### 6.2 Ownership Verification

For actions that reference a course offering (reports, attendance export, grades export), the service verifies that:

- The offering belongs to (is taught by) the lecturer

This prevents lecturers from accessing data of other lecturers’ course offerings.

---

## 7) Database Tables (High-level)

The lecturer module depends on the typical academic structure:

- Users / Roles (lecturer identity + permissions)
- Courses and course offerings (course + term + active)
- Course lecturers (mapping lecturer ↔ offering)
- Enrollments (student ↔ offering + grade + status)
- Class schedules (offering sessions)
- Attendance (per student per schedule/date)

> Exact table names and schema details are defined in Flyway migrations under:
> `src/main/resources/db/migration/`

---

## 8) How To Use (Demo Steps)

1. Login as a lecturer user.
2. Open Lecturer Reports:
   - `http://localhost:8080/lecturer/reports?lecturerId=2`
   - Set filters (date range, course, student status) and click **Apply**.
3. Export report:
   - Use **Export Report** (PDF) and per-course exports (Grades CSV, Attendance CSV).
4. Attendance management:
   - `http://localhost:8080/lecturer/attendance?lecturerId=2`
   - Select schedule/offering as needed, then create/edit/delete attendance records.

---

## 9) Testing / Verification Checklist

Recommended checks before submission:

- Reports page loads for lecturer and does not show admin-only data.
- Filters change the summary metrics correctly.
- Course detail section appears when selecting one course offering.
- Exports download successfully and contain expected rows.
- Attendance edit/delete buttons work without JavaScript errors.

---

## 10) Conclusion

The lecturer module provides a complete lecturer-only workflow: managing attendance, viewing students/schedules, generating analytics, and exporting reports. The implementation enforces role-based security and offering ownership checks to ensure lecturers only access their own teaching data.
