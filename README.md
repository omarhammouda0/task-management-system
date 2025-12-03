# Task Management System

A production-ready, enterprise-grade task management REST API built with Spring Boot 3 and PostgreSQL, designed to demonstrate advanced backend development skills for professional software engineering roles.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📋 Table of Contents

- [Overview]
- [Key Features]
- [Technology Stack]
- [Architecture]
- [Getting Started]
- [API Documentation]
- [Database Schema]
- [Authentication & Authorization]
- [Testing]
- [Deployment]
- [Project Structure]
- [Development Roadmap]
- [Contributing]
- [Contact]

## 🎯 Overview

This Task Management System is a comprehensive backend application designed to showcase production-ready software engineering practices. The system enables teams to manage projects and tasks with robust authentication, multi-level authorization, and complete audit trails.

**Project Goals:**
- Demonstrate enterprise-grade Spring Boot development
- Showcase clean architecture and SOLID principles
- Provide RESTful API design best practices
- Implement comprehensive security and authorization
- Maintain high test coverage and code quality

**Target Audience:** This portfolio project is designed for junior to mid-level backend developer positions in Germany, particularly in consulting, startups, and enterprise software companies.

## ✨ Key Features

### Core Functionality
- ✅ **User Management** - Registration, authentication, and profile management
- ✅ **JWT Authentication** - Secure token-based authentication with refresh tokens
- ✅ **Team Management** - Multi-tenant team creation and member management
- ✅ **Project Management** - Complete project lifecycle with status tracking
- ✅ **Task Management** - Task creation, assignment, and status transitions
- ✅ **Role-Based Access Control** - Multi-level authorization (Admin, Owner, Admin, Member)

### Technical Features
- ✅ **Soft Delete Pattern** - Data preservation with logical deletion
- ✅ **Audit Trails** - Complete tracking of who created/updated what and when
- ✅ **State Machine Validation** - Controlled status transitions for projects and tasks
- ✅ **Pagination Support** - Efficient handling of large datasets
- ✅ **Database Migrations** - Version-controlled schema management with Flyway
- ✅ **Comprehensive Validation** - Input validation at DTO and service layers
- ✅ **Exception Handling** - Custom exceptions with meaningful error messages
- ✅ **API Documentation** - Interactive Swagger/OpenAPI documentation

### Business Logic
- ✅ **Title Uniqueness** - Tasks unique per project, projects unique per team
- ✅ **Team Membership Validation** - Only team members can access team resources
- ✅ **Assignment Rules** - Tasks can only be assigned to team members
- ✅ **Status Transitions** - Validated state changes (TO_DO → IN_PROGRESS → DONE)
- ✅ **Last Owner Protection** - Prevents removing the last team owner
- ✅ **Active Project Requirement** - Tasks can only be created in active projects

## 🛠️ Technology Stack

### Backend Framework
- **Java 17** - Latest LTS version with modern language features
- **Spring Boot 3.2.5** - Production-ready application framework
- **Spring Security** - Comprehensive authentication and authorization
- **Spring Data JPA** - Database abstraction and ORM
- **Hibernate** - JPA implementation with PostgreSQL dialect

### Database
- **PostgreSQL 15** - Robust relational database
- **Flyway** - Database migration and version control
- **HikariCP** - High-performance connection pooling

### Security
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing algorithm
- **JJWT 0.12.3** - JWT creation and validation

### Development Tools
- **Maven** - Dependency management and build automation
- **Lombok** - Boilerplate code reduction
- **Docker** - Containerization for development and deployment
- **Git** - Version control

### Testing
- **JUnit 5** - Unit testing framework
- **Mockito** - Mocking framework for isolated testing
- **Spring Boot Test** - Integration testing support
- **AssertJ** - Fluent assertion library
- **H2 Database** - In-memory database for testing

### Documentation & Monitoring
- **Swagger/OpenAPI 3** - Interactive API documentation
- **Spring Boot Actuator** - Application monitoring and metrics
- **SLF4J + Logback** - Comprehensive logging

## 🏗️ Architecture

### Design Principles
- **Clean Architecture** - Clear separation of concerns across layers
- **SOLID Principles** - Maintainable and extensible code design
- **RESTful Design** - Standard HTTP methods and status codes
- **DRY (Don't Repeat Yourself)** - Reusable components and utilities

### Layer Structure
```
┌─────────────────────────────────────┐
│     Controller Layer (REST API)     │  ← HTTP Endpoints
├─────────────────────────────────────┤
│        Service Layer (Business)     │  ← Business Logic
├─────────────────────────────────────┤
│      Repository Layer (Data)        │  ← Database Access
├─────────────────────────────────────┤
│       Entity Layer (Domain)         │  ← Domain Models
└─────────────────────────────────────┘
```

### Package Organization
Feature-based package structure for better modularity:
```
com.taskmanagement
├── auth/           # Authentication & JWT
├── user/           # User management
├── team/           # Team management
├── project/        # Project management
├── task/           # Task management
├── common/         # Shared utilities
│   ├── config/     # Configuration classes
│   ├── exception/  # Custom exceptions
│   ├── security/   # Security components
│   └── entity/     # Base entities
```

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- PostgreSQL 15+
- Docker (optional, for containerized database)
- Git

### Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/omarhammouda0/task-management-system.git
cd task-management-system
```

#### 2. Setup Database

**Option A: Using Docker (Recommended)**
```bash
# Start PostgreSQL in Docker
docker-compose up -d postgres

# Verify database is running
docker ps
```

**Option B: Local PostgreSQL**
```sql
-- Create database
CREATE DATABASE taskmanagement;

-- Create user
CREATE USER taskadmin WITH PASSWORD 'Task2024!';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE taskmanagement TO taskadmin;
```

#### 3. Configure Application
Update `src/main/resources/application.yml` if needed:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/taskmanagement
    username: taskadmin
    password: Task2024!
```

#### 4. Build the Project
```bash
# Clean and build
mvn clean install

# Skip tests for faster build
mvn clean install -DskipTests
```

#### 5. Run the Application
```bash
# Run with Maven
mvn spring-boot:run

# Or run the JAR
java -jar target/task-management-system-1.0.0.jar
```

#### 6. Verify Installation
```bash
# Check application health
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### First Time Setup

#### 1. Access Swagger UI
Navigate to: http://localhost:8080/swagger-ui.html

#### 2. Register a User
```bash
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "Admin123!",
  "firstName": "Admin",
  "lastName": "User"
}
```

#### 3. Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "Admin123!"
}
```

#### 4. Use Access Token
Copy the `accessToken` from login response and use in subsequent requests:
```bash
Authorization: Bearer <your-access-token>
```

## 📚 API Documentation

### Interactive Documentation
Access Swagger UI at: **http://localhost:8080/swagger-ui.html**

### API Endpoints Overview

#### Authentication & Authorization
```
POST   /api/auth/register          - Register new user
POST   /api/auth/login             - User login
POST   /api/auth/logout            - User logout
POST   /api/auth/refresh-token     - Refresh access token
```

#### User Management
```
GET    /api/users/{id}             - Get user by ID
GET    /api/users                  - Get all users (Admin)
PATCH  /api/users/{id}             - Update user
DELETE /api/users/{id}             - Deactivate user
POST   /api/users/{id}/activate    - Activate user (Admin)
```

#### Team Management
```
POST   /api/teams                  - Create team
GET    /api/teams/{id}             - Get team by ID
GET    /api/teams                  - Get all teams
PATCH  /api/teams/{id}             - Update team
DELETE /api/teams/{id}             - Delete team
```

#### Team Members
```
POST   /api/team-members           - Add member to team
GET    /api/team-members/team/{id} - Get team members
PATCH  /api/team-members/{id}/role - Change member role
DELETE /api/team-members/{id}      - Remove member
POST   /api/team-members/{id}/leave- Leave team
```

#### Project Management
```
POST   /api/projects               - Create project
GET    /api/projects/{id}          - Get project by ID
GET    /api/projects/team/{id}     - Get team projects
PATCH  /api/projects/{id}          - Update project
DELETE /api/projects/{id}          - Delete project
POST   /api/projects/{id}/activate - Activate project (Admin)
POST   /api/projects/{id}/archive  - Archive project
POST   /api/projects/{id}/restore  - Restore project (Admin)
POST   /api/projects/{id}/transfer - Transfer to another team (Admin)
```

#### Task Management
```
POST   /api/tasks                  - Create task
GET    /api/tasks/{id}             - Get task by ID
GET    /api/tasks/project/{id}     - Get project tasks
PATCH  /api/tasks/{id}             - Update task
DELETE /api/tasks/{id}             - Delete task
POST   /api/tasks/{id}/assign      - Assign task
POST   /api/tasks/{id}/unassign    - Unassign task
GET    /api/tasks/my-tasks         - Get my assigned tasks
GET    /api/tasks/admin/all        - Get all tasks (Admin)
```

### Status Codes
- **200 OK** - Successful GET/PATCH request
- **201 Created** - Successful POST request
- **204 No Content** - Successful DELETE request
- **400 Bad Request** - Invalid input data
- **401 Unauthorized** - Missing or invalid authentication
- **403 Forbidden** - Insufficient permissions
- **404 Not Found** - Resource not found
- **409 Conflict** - Duplicate resource (e.g., email already exists)
- **500 Internal Server Error** - Server error

## 🗄️ Database Schema

### Entity Relationship Diagram
```
┌─────────┐         ┌──────────────┐         ┌─────────┐
│  User   │────────▶│ TeamMember   │◀────────│  Team   │
└─────────┘         └──────────────┘         └─────────┘
     │                                             │
     │                                             │
     ▼                                             ▼
┌─────────┐                                  ┌─────────┐
│  Task   │                                  │ Project │
│(assigned)                                  └─────────┘
└─────────┘                                       │
     ▲                                            │
     └────────────────────────────────────────────┘
```

### Core Entities

#### User
- **Fields**: id, email, password, firstName, lastName, role, status
- **Roles**: ADMIN, MEMBER
- **Status**: ACTIVE, INACTIVE, SUSPENDED, DELETED

#### Team
- **Fields**: id, name, description, status, createdBy, updatedBy
- **Status**: ACTIVE, INACTIVE, DELETED
- **Relationships**: Has many members, has many projects

#### TeamMember
- **Fields**: id, teamId, userId, role
- **Roles**: OWNER, ADMIN, MEMBER
- **Business Rules**: Cannot remove last owner

#### Project
- **Fields**: id, name, description, teamId, status, startDate, endDate
- **Status**: PLANNED, ACTIVE, ON_HOLD, COMPLETED, ARCHIVED, DELETED
- **Relationships**: Belongs to team, has many tasks

#### Task
- **Fields**: id, title, description, projectId, assignedTo, status, priority, dueDate, completedAt
- **Status**: TO_DO, IN_PROGRESS, IN_REVIEW, DONE, BLOCKED, DELETED
- **Priority**: LOW, MEDIUM, HIGH, URGENT
- **Relationships**: Belongs to project, assigned to user

### Database Migrations
All schema changes are managed through Flyway migrations in:
```
src/main/resources/db/migration/
├── V1__Create_Users_Table.sql
├── V2__Create_Teams_Table.sql
├── V3__Create_Team_Members_Table.sql
├── V4__Create_Projects_Table.sql
└── V6__Create_Tasks_Table.sql
```

## 🔐 Authentication & Authorization

### JWT Authentication Flow
```
1. User registers/logs in
2. Server generates JWT access token (1 hour) and refresh token (7 days)
3. Client stores tokens securely
4. Client includes access token in Authorization header
5. Server validates token on each request
6. Token expires → Client uses refresh token to get new access token
```

### Authorization Hierarchy

#### System Admin (ADMIN role)
- Full system access
- Can view/modify all resources
- Can access deleted items
- Override all restrictions

#### Team Owner
- Full control over team
- Manage team members
- Create/update/delete projects
- Manage all team tasks

#### Team Admin
- Manage team members (except owners)
- Create/update projects
- Manage team tasks
- Cannot delete projects

#### Team Member
- View team resources
- Create tasks
- Update assigned tasks
- Limited permissions

### Security Implementation
- **Password Hashing**: BCrypt with salt
- **JWT Signing**: HS256 algorithm with 256-bit secret
- **Token Storage**: Client-side (localStorage/sessionStorage)
- **CORS Configuration**: Configurable allowed origins
- **CSRF Protection**: Disabled for stateless API (JWT-based)
- **SQL Injection Prevention**: Parameterized queries with JPA
- **XSS Prevention**: Input validation and sanitization

## 🧪 Testing

### Test Coverage
```
Total Tests: 306+
├── User Module: 48 tests
├── Auth Module: 29 tests
├── Team Module: 85 tests
├── TeamMember Module: 88 tests
├── Project Module: 56 tests
└── Task Module: 61 tests
```

### Running Tests

#### Run All Tests
```bash
mvn test
```

#### Run Specific Test Class
```bash
mvn test -Dtest=TaskServiceImplementationTest
```

#### Run with Coverage Report
```bash
mvn clean test jacoco:report
```

### Test Structure
Each module follows the same testing pattern:
```
- Happy path scenarios
- Edge cases and boundary conditions
- Validation and error handling
- Authorization and security
- Business rule enforcement
- Null safety and defensive programming
```

### Testing Technologies
- **JUnit 5** - Test framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Spring Boot Test** - Integration testing
- **H2 Database** - In-memory test database

## 🚀 Deployment

### Docker Deployment

#### Build Docker Image
```bash
docker build -t task-management-system:latest .
```

#### Run with Docker Compose
```bash
docker-compose up -d
```

### Railway.app Deployment

#### Prerequisites
1. GitHub account with repository
2. Railway.app account (free tier available)

#### Deployment Steps
1. Push code to GitHub
2. Connect Railway to GitHub repository
3. Add PostgreSQL database service
4. Configure environment variables:
```
   SPRING_PROFILES_ACTIVE=production
   JWT_SECRET=your-secret-key-here
   DATABASE_URL=<provided-by-railway>
```
5. Deploy automatically on push

### Environment Variables
```bash
# Database
DATABASE_URL=jdbc:postgresql://host:5432/dbname
DATABASE_USERNAME=username
DATABASE_PASSWORD=password

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_ACCESS_TOKEN_EXPIRATION=3600000
JWT_REFRESH_TOKEN_EXPIRATION=604800000

# Server
PORT=8080
SPRING_PROFILES_ACTIVE=production
```

## 📁 Project Structure
```
task-management-system/
├── src/
│   ├── main/
│   │   ├── java/com/taskmanagement/
│   │   │   ├── auth/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── service/
│   │   │   │   └── ...
│   │   │   ├── user/
│   │   │   ├── team/
│   │   │   ├── project/
│   │   │   ├── task/
│   │   │   └── common/
│   │   │       ├── config/
│   │   │       ├── entity/
│   │   │       ├── exception/
│   │   │       └── security/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-production.yml
│   │       └── db/migration/
│   └── test/
│       └── java/com/taskmanagement/
│           ├── auth/
│           ├── user/
│           ├── team/
│           ├── project/
│           └── task/
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

## 🗺️ Development Roadmap

### ✅ Phase 1: Core Features (Completed)
- User authentication and authorization
- Team management
- Project lifecycle management
- Task management with assignments
- Comprehensive testing (306+ tests)

### 🔄 Phase 2: Advanced Features (Planned)
- [ ] Comment system for tasks
- [ ] File attachments (with MinIO)
- [ ] Activity logs and audit trails
- [ ] Email notifications
- [ ] Real-time updates (WebSockets)

### 📅 Phase 3: Performance & Scalability (Planned)
- [ ] Redis caching layer
- [ ] Elasticsearch for full-text search
- [ ] RabbitMQ for asynchronous processing
- [ ] Rate limiting and throttling
- [ ] Database query optimization

### 🎨 Phase 4: Enhanced Features (Future)
- [ ] Project templates
- [ ] Task dependencies
- [ ] Time tracking
- [ ] Reporting and analytics
- [ ] Export capabilities (PDF, Excel)
- [ ] Integration APIs (Slack, Jira, etc.)

## 📊 Project Statistics
```
Modules: 6 (User, Auth, Team, TeamMember, Project, Task)
Service Methods: 51
REST Endpoints: 51+
Unit Tests: 306+
Lines of Code: 5000+
Test Coverage: High
Database Tables: 5
Flyway Migrations: 5
```

## 🤝 Contributing

This is a portfolio project, but suggestions and feedback are welcome!

### How to Contribute
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow Java naming conventions
- Write meaningful commit messages
- Include unit tests for new features
- Update documentation as needed

## 📧 Contact

**Omar Hammouda**
- GitHub: [@omarhammouda0](https://github.com/omarhammouda0)
- Email: omarhamoda0@gmail.com
- Location: Dortmund, Germany

**Portfolio Project Context:**
This project demonstrates production-ready Spring Boot development skills for junior to mid-level backend developer positions in Germany. Built as part of Fachinformatiker vocational training (Ausbildung).

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- PostgreSQL community for the robust database
- All open-source contributors whose libraries made this possible

---

**⭐ If you find this project helpful, please consider giving it a star!**

Built with ❤️ in Germany 🇩🇪
