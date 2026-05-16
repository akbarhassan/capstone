# LearnHub API Documentation

Base URL: `http://localhost:8080`

All responses follow the structure:
```json
{
  "status": <http_status_code>,
  "message": "<description>",
  "data": <payload_or_null>,
  "timestamp": "<ISO_8601>"
}
```

---

## Authentication

All auth endpoints are **public** (no token required).

### POST /api/v1/auth/register

Register a new user account. Sends a verification email.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "Password1"
}
```

**Validation:**
- `email` — valid email format, required
- `password` — minimum 8 characters, required

**Response (201):**
```json
{
  "status": 201,
  "message": "Registration successful. Please check your email to verify your account.",
  "data": {
    "id": 2,
    "email": "user@example.com",
    "status": "PENDING",
    "emailVerified": false
  }
}
```

---

### POST /api/v1/auth/login

Authenticate and receive a JWT token.

**Request Body:**
```json
{
  "email": "admin@learnhub.com",
  "password": "Admin123!"
}
```

**Response (200):**
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

**Error Cases:**
- `404` — User not found
- `401` — Email not verified / Account banned / Account deleted / Incorrect password

---

### GET /api/v1/auth/verify-email?token={token}

Verify the user's email address using the token from the verification email.

**Response (200):**
```json
{
  "status": 200,
  "message": "Email verified successfully. You can now log in.",
  "data": null
}
```

---

### POST /api/v1/auth/resend-verification?email={email}

Resend the verification email.

**Response (200):**
```json
{
  "status": 200,
  "message": "Verification email resent. Please check your inbox.",
  "data": null
}
```

---

### POST /api/v1/auth/request-password-reset

Request a password reset link via email.

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response (200):**
```json
{
  "status": 200,
  "message": "If an account exists with this email, a password reset link has been sent.",
  "data": null
}
```

---

### POST /api/v1/auth/reset-password

Reset password using the token from the reset email.

**Request Body:**
```json
{
  "token": "reset-token-from-email",
  "password": "NewPassword1"
}
```

**Response (200):**
```json
{
  "status": 200,
  "message": "Password reset successful. You can now log in with your new password.",
  "data": null
}
```

**Error Cases:**
- `401` — Invalid/expired token, token already used, password recently used

---

## Categories

All category endpoints require authentication via `Authorization: Bearer <token>`.

### POST /api/v1/categories

Create a new category. Requires `category:create` permission.

**Request Body:**
```json
{
  "name": "Programming",
  "description": "Learn programming languages and concepts"
}
```

**Response (201):**
```json
{
  "status": 201,
  "message": "Category created successfully",
  "data": {
    "id": 1,
    "name": "Programming",
    "description": "Learn programming languages and concepts",
    "categoryImage": null
  }
}
```

---

### GET /api/v1/categories

Get all categories. Requires `category:read` permission.

**Response (200):**
```json
{
  "status": 200,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": 1,
      "name": "Programming",
      "description": "Learn programming languages and concepts",
      "categoryImage": null
    }
  ]
}
```

---

### GET /api/v1/categories/{id}

Get a single category. Requires `category:read` permission.

---

### PUT /api/v1/categories/{id}

Update a category. Requires `category:update` permission.

**Request Body:**
```json
{
  "name": "Programming Updated",
  "description": "Updated description"
}
```

---

### POST /api/v1/categories/{id}/image

Upload a category image. Requires `category:update` permission.

**Request:** `multipart/form-data`
- `file` — image file (PNG, JPG, etc.)

---

### DELETE /api/v1/categories/{id}

Delete a category. Requires `category:delete` permission.

---

## Courses

All course endpoints require authentication.

### POST /api/v1/courses?categoryId={categoryId}

Create a new course. The authenticated user becomes the instructor. Requires `course:create` permission.

**Query Params:**
- `categoryId` — the category to assign the course to

**Request Body:**
```json
{
  "title": "Java Basics",
  "description": "Learn Java from scratch"
}
```

**Response (201):**
```json
{
  "status": 201,
  "message": "Course created successfully",
  "data": {
    "id": 1,
    "title": "Java Basics",
    "description": "Learn Java from scratch",
    "thumbnail": null,
    "deleted": false,
    "category": { "id": 1, "name": "Programming" },
    "instructor": { "id": 1, "email": "admin@learnhub.com" }
  }
}
```

---

### GET /api/v1/courses

Get all active (non-deleted) courses. Requires `course:read` permission.

---

### GET /api/v1/courses/{id}

Get a single course by ID. Requires `course:read` permission.

---

### PUT /api/v1/courses/{id}

Update a course. Requires `course:update` permission.

**Request Body:**
```json
{
  "title": "Advanced Java",
  "description": "Deep dive into Java"
}
```

---

### POST /api/v1/courses/{id}/thumbnail

Upload a course thumbnail image. Requires `course:update` permission.

**Request:** `multipart/form-data`
- `file` — image file

---

### DELETE /api/v1/courses/{id}

Soft-delete a course (sets `deleted=true`). Requires `course:delete` permission.

---

### GET /api/v1/courses/my-courses

Get all courses created by the authenticated instructor. Requires `course:read` permission.

---

### GET /api/v1/courses/by-category/{categoryId}

Get all courses in a specific category. Requires `course:read` permission.

---

## Lessons

Lessons are nested under courses. All endpoints require authentication.

### POST /api/v1/courses/{courseId}/lessons

Create a new lesson in a course. Requires `lesson:create` permission.

**Request Body:**
```json
{
  "title": "Introduction to Java",
  "content": "Welcome to the first lesson of this course.",
  "sortOrder": 1
}
```

**Response (201):**
```json
{
  "status": 201,
  "message": "Lesson created successfully",
  "data": {
    "id": 1,
    "title": "Introduction to Java",
    "content": "Welcome to the first lesson of this course.",
    "sortOrder": 1
  }
}
```

---

### GET /api/v1/courses/{courseId}/lessons

Get all lessons for a course, ordered by `sortOrder`. Requires `lesson:read` permission.

---

### GET /api/v1/courses/{courseId}/lessons/{id}

Get a single lesson by ID. Requires `lesson:read` permission.

---

### PUT /api/v1/courses/{courseId}/lessons/{id}

Update a lesson. Requires `lesson:update` permission.

**Request Body:**
```json
{
  "title": "Updated Lesson Title",
  "content": "Updated content here",
  "sortOrder": 2
}
```

---

### DELETE /api/v1/courses/{courseId}/lessons/{id}

Delete a lesson. Requires `lesson:delete` permission.

---

## Enrollments

All enrollment endpoints require authentication.

### POST /api/v1/enrollments/{courseId}

Enroll the authenticated student in a course. Requires `enrollment:create` permission.

**Response (201):**
```json
{
  "status": 201,
  "message": "Enrolled successfully",
  "data": {
    "id": 1,
    "status": "ACTIVE",
    "student": { "id": 2, "email": "student@example.com" },
    "course": { "id": 1, "title": "Java Basics" }
  }
}
```

**Error Cases:**
- `409` — Already enrolled in this course
- `404` — Course not found

---

### GET /api/v1/enrollments/my

Get all enrollments for the authenticated student. Requires `enrollment:read` permission.

---

### GET /api/v1/enrollments

Get all enrollments (admin view). Requires `enrollment:read` permission.

---

### DELETE /api/v1/enrollments/{id}

Drop an enrollment (changes status to DROPPED). Only the enrollment owner can drop. Requires `enrollment:delete` permission.

---

## Error Responses

All errors follow a consistent format:

```json
{
  "status": 404,
  "message": "Resource not found: Course with id 99",
  "data": null,
  "timestamp": "2024-01-01T00:00:00"
}
```

### Common Error Codes

| Code | Meaning |
|------|---------|
| 400 | Validation error / Bad request |
| 401 | Authentication failed |
| 403 | Insufficient permissions |
| 404 | Resource not found |
| 409 | Resource already exists (duplicate) |
| 500 | Internal server error |
