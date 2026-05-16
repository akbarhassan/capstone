# LearnHub — Setup Guide

## Table of Contents

- [Docker Setup](#docker-setup)
- [PgAdmin Setup](#pgadmin-setup)
- [Postman Setup](#postman-setup)

---

## Docker Setup

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

### Start the Application

```bash
# Build and start all services (PostgreSQL + Spring Boot app)
docker compose up --build
```

This will:
1. Start a PostgreSQL 16 database on port `5432`
2. Build the Spring Boot application
3. Start the app on port `8080`
4. Automatically seed the database with roles, permissions, and a default admin user

### Verify It's Running

```bash
# Check running containers
docker compose ps
```

You should see two containers:
- `learnhub-db` — PostgreSQL database
- `learnhub-app` — Spring Boot application

### Stop the Application

```bash
# Stop all services
docker compose down

# Stop and remove all data (volumes)
docker compose down -v
```

### Run in Background

```bash
docker compose up --build -d
```

### View Logs

```bash
# All services
docker compose logs -f

# Only the app
docker compose logs -f app
```

### Environment Variables

The `docker-compose.yml` uses sensible defaults. To customize JWT secret or mail settings, create a `.env` file in the project root (see `.env.example`).

---

## PgAdmin Setup

### Step 1: Install PgAdmin

Download from [https://www.pgadmin.org/download/](https://www.pgadmin.org/download/) or use the Docker version.

### Step 2: Connect to the Database

1. Open PgAdmin
2. Right-click **Servers** → **Register** → **Server...**
3. In the **General** tab:
   - **Name:** `LearnHub Local`
4. In the **Connection** tab:
   - **Host:** `localhost`
   - **Port:** `5432`
   - **Maintenance database:** `capstone`
   - **Username:** `postgres`
   - **Password:** `12345678` (or your `DB_PASSWORD` from `.env`)
   - Check **Save password**
5. Click **Save**

### Step 3: Browse the Database

1. Expand **Servers** → **LearnHub Local** → **Databases** → **capstone**
2. Expand **Schemas** → **public** → **Tables**
3. You should see tables like:
   - `users`
   - `roles`
   - `permissions`
   - `categories`
   - `courses`
   - `lessons`
   - `enrollments`
   - `email_verification_tokens`
   - `password_reset_tokens`
   - `password_histories`

### Step 4: Run Queries

Right-click on the `capstone` database → **Query Tool**, then:

```sql
-- View all users
SELECT * FROM users;

-- View all roles with permissions
SELECT r.name, p.action
FROM roles r
JOIN role_permissions rp ON r.id = rp.role_id
JOIN permissions p ON p.id = rp.permission_id
ORDER BY r.name, p.action;

-- View all enrollments
SELECT u.email, c.title, e.status
FROM enrollments e
JOIN users u ON e.student_id = u.id
JOIN courses c ON e.course_id = c.id;
```

---

## Postman Setup

### Step 1: Import the Collection

1. Open Postman
2. Click **Import** (top left)
3. Drag and drop `LearnHub.postman_collection.json` from the project root
4. The collection "LearnHub API" will appear in your sidebar

### Step 2: Set Base URL

The collection uses a variable `{{baseUrl}}`. It defaults to `http://localhost:8080`. To change it:

1. Click the collection name **LearnHub API**
2. Go to the **Variables** tab
3. Set `baseUrl` current value to your server URL

### Step 3: Login and Get Token

1. Open **Auth** → **Login**
2. Use the default admin credentials:
   ```json
   {
     "email": "admin@learnhub.com",
     "password": "Admin123!"
   }
   ```
3. Click **Send**
4. The JWT token is automatically saved to the `{{token}}` collection variable

All other requests will use this token automatically via the `Authorization: Bearer {{token}}` header.

### Step 4: Test the Endpoints

Follow this order for a complete workflow:

#### 1. Create a Category
- **POST** `/api/v1/categories`
- Body:
  ```json
  {
    "name": "Programming",
    "description": "Learn programming"
  }
  ```

#### 2. Create a Course
- **POST** `/api/v1/courses?categoryId=1`
- Body:
  ```json
  {
    "title": "Java Basics",
    "description": "Learn Java from scratch"
  }
  ```

#### 3. Create a Lesson
- **POST** `/api/v1/courses/1/lessons`
- Body:
  ```json
  {
    "title": "Introduction",
    "content": "Welcome to the course",
    "sortOrder": 1
  }
  ```

#### 4. Register a Student
- **POST** `/api/v1/auth/register`
- Body:
  ```json
  {
    "email": "student@example.com",
    "password": "Student1"
  }
  ```

#### 5. Enroll in a Course
- Login as the student first, then:
- **POST** `/api/v1/enrollments/1`

### Step 5: File Uploads

For image upload endpoints (category image, course thumbnail):

1. Select the request (e.g., **Upload Category Image**)
2. In the **Body** tab, select **form-data**
3. For the `file` key, click the dropdown and select **File**
4. Choose an image file from your computer
5. Click **Send**

### Troubleshooting

| Issue | Solution |
|-------|----------|
| `401 Unauthorized` | Token expired — re-run the Login request |
| `403 Forbidden` | Current user lacks the required permission |
| `Connection refused` | Make sure `docker compose up` is running |
| Empty response | Check the app logs with `docker compose logs -f app` |
