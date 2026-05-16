# LearnHub - Online Learning Platform API

A RESTful API for an online learning platform built with Spring Boot. LearnHub enables instructors to create and manage courses with lessons, while students can browse, enroll, and track their learning progress.

## Tech Stack

- **Java 17**
- **Spring Boot 4.0.6**
- **Spring Security** (JWT-based stateless authentication)
- **Spring Data JPA** (Hibernate ORM)
- **PostgreSQL** (production database)
- **H2** (in-memory test database)
- **Thymeleaf** (email templates)
- **Lombok** (boilerplate reduction)
- **Maven** (build tool)

## Features

- **Authentication & Authorization**
  - JWT-based stateless auth
  - Role-based access control (ADMIN, USER)
  - Permission-based method security (`@PreAuthorize`)
  - Email verification flow
  - Password reset with token expiry
  - Password history enforcement (prevents reuse)

- **Course Management**
  - Full CRUD with soft-delete
  - Thumbnail image upload
  - Filter by category or instructor

- **Lesson Management**
  - Nested under courses
  - Ordered by `sortOrder`

- **Category Management**
  - Full CRUD with image upload
  - Duplicate name prevention

- **Enrollment System**
  - Enroll/drop with status tracking (ACTIVE, DROPPED, COMPLETED)
  - Race condition protection (unique constraint + optimistic locking)

## Project Structure

```
src/main/java/com/ga/capstone/
├── config/              # App configuration
├── controllers/         # REST controllers
├── dto/                 # Request/response DTOs
├── enums/               # EnrollmentStatus, UserStatus
├── exceptions/          # Custom exceptions + global handler
├── models/              # JPA entities
├── repositories/        # Spring Data JPA repositories
├── response/            # Standardized API response
├── security/            # JWT filter, UserDetails, SecurityConfig
├── seeders/             # Database seeder (roles, permissions, admin)
├── services/            # Business logic
└── utils/               # Utility classes (ResponseBuilder)
```

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- SMTP server (for email features)

### Environment Setup

Create a `.env` file in the project root:

```env
DB_URL=jdbc:postgresql://localhost:5432/learnhub
DB_USERNAME=postgres
DB_PASSWORD=your_password

JWT_SECRET=your-jwt-secret-key-must-be-at-least-256-bits
JWT_EXPIRATION_MS=86400000

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@learnhub.com
```

### Run the Application

```bash
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`.

### Run Tests

```bash
./mvnw test
```

Tests use H2 in-memory database and do not require external services.

## Default Admin Account

On first startup, the database seeder creates:

| Email | Password | Role |
|-------|----------|------|
| `admin@learnhub.com` | `Admin123!` | ADMIN |

> **Warning:** Change the default admin password in production.

## API Endpoints

All endpoints return a standardized response:

```json
{
  "status": 200,
  "message": "Success message",
  "data": { ... },
  "timestamp": "2024-01-01T00:00:00"
}
```

### Authentication (Public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Login and receive JWT |
| GET | `/api/v1/auth/verify-email?token=` | Verify email address |
| POST | `/api/v1/auth/resend-verification?email=` | Resend verification email |
| POST | `/api/v1/auth/request-password-reset` | Request password reset |
| POST | `/api/v1/auth/reset-password` | Reset password with token |

### Categories (Authenticated)

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| POST | `/api/v1/categories` | `category:create` | Create category |
| GET | `/api/v1/categories` | `category:read` | List all categories |
| GET | `/api/v1/categories/{id}` | `category:read` | Get category by ID |
| PUT | `/api/v1/categories/{id}` | `category:update` | Update category |
| POST | `/api/v1/categories/{id}/image` | `category:update` | Upload category image |
| DELETE | `/api/v1/categories/{id}` | `category:delete` | Delete category |

### Courses (Authenticated)

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| POST | `/api/v1/courses?categoryId=` | `course:create` | Create course |
| GET | `/api/v1/courses` | `course:read` | List all active courses |
| GET | `/api/v1/courses/{id}` | `course:read` | Get course by ID |
| PUT | `/api/v1/courses/{id}` | `course:update` | Update course |
| POST | `/api/v1/courses/{id}/thumbnail` | `course:update` | Upload thumbnail |
| DELETE | `/api/v1/courses/{id}` | `course:delete` | Soft-delete course |
| GET | `/api/v1/courses/my-courses` | `course:read` | Get instructor's courses |
| GET | `/api/v1/courses/by-category/{categoryId}` | `course:read` | Get courses by category |

### Lessons (Authenticated)

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| POST | `/api/v1/courses/{courseId}/lessons` | `lesson:create` | Create lesson |
| GET | `/api/v1/courses/{courseId}/lessons` | `lesson:read` | List lessons by course |
| GET | `/api/v1/courses/{courseId}/lessons/{id}` | `lesson:read` | Get lesson by ID |
| PUT | `/api/v1/courses/{courseId}/lessons/{id}` | `lesson:update` | Update lesson |
| DELETE | `/api/v1/courses/{courseId}/lessons/{id}` | `lesson:delete` | Delete lesson |

### Enrollments (Authenticated)

| Method | Endpoint | Permission | Description |
|--------|----------|------------|-------------|
| POST | `/api/v1/enrollments/{courseId}` | `enrollment:create` | Enroll in course |
| GET | `/api/v1/enrollments/my` | `enrollment:read` | Get my enrollments |
| GET | `/api/v1/enrollments` | `enrollment:read` | Get all enrollments (admin) |
| DELETE | `/api/v1/enrollments/{id}` | `enrollment:delete` | Drop enrollment |

## Roles & Permissions

### ADMIN
Full access to all resources (all CRUD on all models).

### USER (Student/Instructor)
| Model | Permissions |
|-------|-------------|
| User Profile | create, read, update |
| Course | create, read, update |
| Lesson | create, read, update |
| Enrollment | create, read, delete |
| Category | read |

## Postman Collection

Import `LearnHub.postman_collection.json` from the project root into Postman.

The Login request auto-saves the JWT token to a collection variable, so subsequent requests are automatically authenticated.

## Testing

The project uses a two-tier testing approach:

- **Unit Tests (Mockito):** Controllers and services tested in isolation with `@ExtendWith(MockitoExtension.class)`, `@Mock`, `@InjectMocks`, and AssertJ assertions.
- **Context Load Test:** `CapstoneApplicationTests` verifies the Spring context loads correctly with `@TestPropertySource` providing H2 config inline.

```bash
# Run all tests
./mvnw test

# Run a specific test class
./mvnw test -Dtest=AuthServiceTest

# Run tests with verbose output
./mvnw test -Dsurefire.useFile=false
```
