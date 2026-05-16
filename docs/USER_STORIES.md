# LearnHub - User Stories

## Authentication & Account Management

| ID | Role | Story | Acceptance Criteria |
|---|---|---|---|
| AUTH-1 | Visitor | As a visitor, I want to register an account so that I can access the platform. | Email must be unique; validation email is sent on success. |
| AUTH-2 | Visitor | As a visitor, I want to verify my email so that my account becomes active. | Clicking the verification link within expiry activates the account. |
| AUTH-3 | Visitor | As a visitor, I want to resend the verification email if I didn't receive it. | A new token is generated and the old one is invalidated. |
| AUTH-4 | Visitor | As a visitor, I want to log in with my email and password so that I receive a JWT token. | Returns a JWT on valid credentials; rejects unverified or banned accounts. |
| AUTH-5 | User | As a user, I want to request a password reset so that I can recover my account. | A reset token is emailed to the registered address. |
| AUTH-6 | User | As a user, I want to reset my password using a token so that I regain access. | Token must be valid and not expired; new password must not match recent history. |
| AUTH-7 | User | As a user, I want to change my password while logged in. | Current password must be verified; new password must not match recent history. |

## User Profile

| ID | Role | Story | Acceptance Criteria |
|---|---|---|---|
| PROF-1 | User | As a user, I want to create my profile so that I can add personal details. | Profile is linked to my account; includes name, phone, address fields. |
| PROF-2 | User | As a user, I want to view my profile. | Returns profile data for the authenticated user. |
| PROF-3 | User | As a user, I want to update my profile information. | Only the profile owner can update; fields are optional. |

## Category Management

| ID | Role | Story | Acceptance Criteria |
|---|---|---|---|
| CAT-1 | Admin | As an admin, I want to create a category so that courses can be organized. | Name must be unique; image upload is optional. |
| CAT-2 | User | As a user, I want to browse all categories. | Returns a list of all categories. |
| CAT-3 | User | As a user, I want to view a single category by ID. | Returns category details. |
| CAT-4 | Admin | As an admin, I want to update a category. | Name uniqueness is enforced on update. |
| CAT-5 | Admin | As an admin, I want to upload an image for a category. | Image is stored and URL is persisted. |
| CAT-6 | Admin | As an admin, I want to delete a category. | Category and its associated courses are removed. |

## Course Management

| ID | Role | Story | Acceptance Criteria |
|---|---|---|---|
| CRS-1 | Instructor | As an instructor, I want to create a course under a category so that students can enroll. | Course is linked to the authenticated instructor and a valid category. |
| CRS-2 | User | As a user, I want to browse all active courses. | Soft-deleted courses are excluded. |
| CRS-3 | User | As a user, I want to view a single course with its details. | Returns course info including instructor and category. |
| CRS-4 | Instructor | As an instructor, I want to update my course. | Only the course owner or an admin can update. |
| CRS-5 | Instructor | As an instructor, I want to upload a thumbnail for my course. | Image is stored and URL is persisted on the course. |
| CRS-6 | Admin | As an admin, I want to soft-delete a course. | Course is marked as deleted but not removed from the database. |
| CRS-7 | Instructor | As an instructor, I want to view all courses I teach. | Returns only courses where I am the instructor. |
| CRS-8 | User | As a user, I want to filter courses by category. | Returns courses belonging to the specified category. |

## Lesson Management

| ID | Role | Story | Acceptance Criteria |
|---|---|---|---|
| LES-1 | Instructor | As an instructor, I want to add a lesson to my course. | Lesson is linked to the course with a sort order. |
| LES-2 | User | As a user, I want to view all lessons in a course. | Returns lessons ordered by `sortOrder`. |
| LES-3 | User | As a user, I want to view a single lesson. | Returns lesson content, title, and video URL. |
| LES-4 | Instructor | As an instructor, I want to update a lesson in my course. | Only the course owner or an admin can update. |
| LES-5 | Instructor | As an instructor, I want to delete a lesson from my course. | Lesson is permanently removed. |

## Enrollment

| ID | Role | Story | Acceptance Criteria |
|---|---|---|---|
| ENR-1 | Student | As a student, I want to enroll in a course. | Duplicate enrollment is prevented by a unique constraint; status defaults to ACTIVE. |
| ENR-2 | Student | As a student, I want to view my enrollments. | Returns all enrollments for the authenticated student. |
| ENR-3 | Admin | As an admin, I want to view all enrollments across the platform. | Returns the full enrollment list. |
| ENR-4 | Student | As a student, I want to drop an enrollment. | Enrollment status is changed or record is removed. |

## Administration

| ID | Role | Story | Acceptance Criteria |
|---|---|---|---|
| ADM-1 | Admin | As an admin, I want to manage roles. | CRUD operations on roles via the roles endpoint. |
| ADM-2 | Admin | As an admin, I want to manage permissions. | CRUD operations on permissions via the permissions endpoint. |
| ADM-3 | Admin | As an admin, I want to assign permissions to roles. | Permissions are linked to roles through the join table. |
| ADM-4 | Admin | As an admin, I want to manage users (view, update, delete). | Full user management with soft-delete support. |
