# School Management System Implementation Plan

## Overview

This project aims to develop a simplified School Management System using Java Swing for the UI, MySQL for the database, and following an MVC architecture. Key features include user authentication (Admin, Teacher, Student roles), course management, student enrollment, basic user management, and potentially messaging and profile features.

## Notes for Assistant (Yourself)

This file, `plan.md`, serves as the high-level checklist for implementing the School Management System. Refer back to this plan to track progress and understand the sequence of development steps derived from the initial requirements document. Each numbered item represents a significant milestone or component to be built.

---

## Implementation Steps

1.  **Setup Project Structure:** Create the base directories (`model`, `view`, `controller`, `util`) as outlined in the requirements.
2.  **Database Connection Utility:** Implement the `util/database/DatabaseConnection.java` class to manage connections to the `MySQL` database (`school_management` on `localhost:3306`, user `root`, password `17Issure!`).
3.  **Password Security Utility:** Implement the `util/security/PasswordUtil.java` class for password hashing (`bcrypt` recommended) and verification, and random password generation.
4.  **Input Validation Utility:** Implement the `util/validation/InputValidator.java` class with static methods for validating email, required fields, etc.
5.  **User Entity & Enum:** Create the `model/entities/User.java` entity and the `model/entities/UserRole.java` enum.
6.  **Course Entity:** Create the `model/entities/Course.java` entity.
7.  **Enrollment Entity:** Create the `model/entities/Enrollment.java` entity.
8.  **Message Entity:** Create the `model/entities/Message.java` entity.
9.  **UserDAO Implementation:** Implement the `model/dao/UserDAO.java` class with methods for CRUD operations on the `Users` table, using `PreparedStatements`. Include methods for finding users by email, role, and handling password reset tokens.
10. **CourseDAO Implementation:** Implement the `model/dao/CourseDAO.java` class for CRUD operations on the `Courses` table, handling teacher assignments and enrollment counts.
11. **EnrollmentDAO Implementation:** Implement the `model/dao/EnrollmentDAO.java` class for managing student enrollments in courses.
12. **MessageDAO Implementation:** Implement the `model/dao/MessageDAO.java` class for handling messages between users.
13. **User Session Management:** Implement the `util/UserSession.java` class (using static fields/methods for simplicity) to track the currently logged-in user.
14. **Login View & Controller:** Implement the `view/auth/LoginView.java` (`Swing` components) and the basic login/logout logic in `controller/auth/AuthController.java`. Integrate with `UserDAO` and `PasswordUtil`.
15. **Main Application Frame & AppController:** Implement `view/common/MainApplicationFrame.java` to hold different views and `controller/AppController.java` to manage view transitions and application flow (login -> dashboard).
16. **Admin Module (Core):**
    *   Implement `view/admin/AdminDashboardView.java`.
    *   Implement `view/admin/UserManagementPanel.java` and `view/admin/CourseManagementPanel.java`.
    *   Implement basic view switching and data loading logic in `controller/admin/AdminController.java`.
17. **Admin Module (Forms & Dialogs):**
    *   Implement `view/admin/UserFormDialog.java` and `view/admin/CourseFormDialog.java`.
    *   Implement `view/admin/AssignTeacherDialog.java`.
    *   Integrate form handling (add/edit/delete/assign/unassign) logic into `AdminController`, interacting with DAOs.
18. **Teacher Module:**
    *   Implement `view/teacher/TeacherDashboardView.java`, `view/teacher/AssignedCoursesPanel.java`, and `view/teacher/CourseStudentsPanel.java`.
    *   Implement `controller/teacher/TeacherController.java` to handle loading assigned courses and viewing enrolled students.
19. **Student Module:**
    *   Implement `view/student/StudentDashboardView.java`, `view/student/AvailableCoursesPanel.java`, and `view/student/EnrolledCoursesPanel.java`.
    *   Implement `controller/student/StudentController.java` for viewing/enrolling in courses and viewing enrolled courses.
20. **Advanced Features & Refinements:**
    *   Implement common UI components (`DataTablePanel`, dialogs).
    *   Implement messaging functionality (e.g., `SendMessageDialog`, `TeacherMessagesPanel`).
    *   Implement profile management (`UserProfilePanel`, change password, picture upload using `FileUploader`).
    *   Implement password recovery (`PasswordRecoveryView`, `EmailService`).
    *   Add search/filtering to tables and refine UI/UX based on guidelines. 