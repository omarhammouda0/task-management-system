# Task Management System

A production-ready, enterprise-grade task management REST API built with Spring Boot 3 and PostgreSQL, designed to demonstrate advanced backend development skills for professional software engineering roles.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Live Demo](https://img.shields.io/badge/Live%20Demo-Railway-blueviolet.svg)](https://task-management-system-production-db83.up.railway.app/)
[![API Docs](https://img.shields.io/badge/API%20Docs-Swagger-85EA2D.svg)](https://task-management-system-production-db83.up.railway.app/swagger-ui.html)

---

## 🌟 **LIVE & PRODUCTION-READY** 🚀

> **This is not just code - it's a fully deployed, working API you can test right now!**

### 🔗 Try the Live API
- **Production URL**: [https://task-management-system-production-db83.up.railway.app/](https://task-management-system-production-db83.up.railway.app/)
- **Interactive Swagger Docs**: [Try all 65+ endpoints here](https://task-management-system-production-db83.up.railway.app/swagger-ui.html)
- **No Setup Required**: Test immediately from your browser!

---

## 🎯 Why This Project Stands Out

<table>
<tr>
<td width="50%">

### 🚀 **Production Deployed**
✅ Live on Railway.app  
✅ PostgreSQL Database  
✅ MinIO Object Storage  
✅ 24/7 Available  

### 📊 **Enterprise Scale**
✅ 8 Modules  
✅ 65+ REST Endpoints  
✅ 350+ Unit Tests (85%+ coverage)  
✅ 8,000+ Lines of Code  

</td>
<td width="50%">

### 🔐 **Production Security**
✅ JWT Authentication  
✅ BCrypt Password Hashing  
✅ Role-Based Access Control  
✅ Refresh Token Support  

### 📚 **Professional Quality**
✅ Complete Swagger Documentation  
✅ Clean Architecture & SOLID  
✅ Comprehensive Testing  
✅ Best Practices Throughout  

</td>
</tr>
</table>

---

## ⚡ Quick Start - Test Live API Now!

### 1️⃣ Register a User
```bash
curl -X POST https://task-management-system-production-db83.up.railway.app/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test12345!",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### 2️⃣ Login & Get Token
```bash
curl -X POST https://task-management-system-production-db83.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Test12345!"
  }'
```

### 3️⃣ Explore with Swagger
👉 **[Open Interactive API Docs](https://task-management-system-production-db83.up.railway.app/swagger-ui.html)** - Click "Try it out" on any endpoint!

---

## 🏆 Project Highlights

### Core Features
- ✨ **8 Complete Modules**: User, Auth, Team, Project, Task, Comment, Attachment
- 🔐 **JWT Authentication**: Access & refresh tokens with role-based authorization
- 👥 **Team Management**: Multi-tenant with OWNER/ADMIN/MEMBER roles
- 📋 **Task System**: Create, assign, track with status transitions
- 💬 **Comments**: Team discussions on tasks
- 📎 **File Attachments**: Upload to MinIO (10MB, 20 files/task)

### Technical Excellence
- ✅ **350+ Tests**: Comprehensive unit tests with 85%+ coverage
- ✅ **Clean Architecture**: Layered design with SOLID principles
- ✅ **API Documentation**: Every endpoint documented with business logic
- ✅ **Database Migrations**: Version-controlled with Flyway
- ✅ **Soft Delete Pattern**: Data preservation throughout
- ✅ **Audit Trails**: Complete tracking of who did what when

### Tech Stack
**Backend**: Java 17, Spring Boot 3.2.5, Spring Security, Spring Data JPA  
**Database**: PostgreSQL 15, Flyway  
**Storage**: MinIO Object Storage  
**Security**: JWT (JJWT 0.12.3), BCrypt  
**Testing**: JUnit 5, Mockito, AssertJ  
**Deployment**: Railway.app (PaaS)  

---

## 📋 Table of Contents

- [Why This Project Stands Out](#-why-this-project-stands-out)
- [Quick Start - Try Live API](#-quick-start---test-live-api-now)
- [Project Highlights](#-project-highlights)
- [Overview & Target Audience](#-overview)
- [Recent Updates (December 2025)](#-recent-updates-december-2025)
- [Complete Features List](#-key-features)
- [Technology Stack](#️-technology-stack)
- [Architecture & Design](#️-architecture)
- [Local Setup Guide](#-getting-started)
- [API Documentation (65+ Endpoints)](#-api-documentation)
- [Database Schema](#️-database-schema)
- [Security & Authorization](#-authentication--authorization)
- [Testing (350+ Tests)](#-testing)
- [Deployment Guide](#-deployment)
- [Contact](#-contact)

---

## 🎯 Overview

**For: Junior to Mid-Level Backend Developer Positions in Germany** 🇩🇪  
*Fachinformatiker Anwendungsentwicklung (Ausbildung) Portfolio Project*

This is a **production-deployed**, enterprise-grade task management REST API that showcases professional Spring Boot development skills. Currently running live on Railway.app with PostgreSQL and MinIO storage.

### What Makes This Special:
- ✨ **Not Just Code**: Fully deployed and working - test it live right now!
- 🎯 **Production Skills**: Real cloud deployment, not just localhost
- 📊 **Enterprise Scale**: 8 modules, 65+ endpoints, 350+ tests (85%+ coverage)
- 🔐 **Security**: JWT authentication, BCrypt, role-based access control
- 📚 **Documentation**: Every endpoint documented with business logic
- 🧪 **Quality**: Comprehensive testing, clean architecture, SOLID principles

### Perfect For:
✅ Backend Developer positions (Java/Spring Boot)  
✅ Consulting firms (msg, Capgemini, Accenture)  
✅ Startups & product companies  
✅ Ausbildung graduate programs  

---

## 🆕 Recent Updates (December 2025)

### ✨ Latest Features Added:

<table>
<tr>
<td width="50%">

**💬 Comment System**
- Task discussions & collaboration
- Team-based access control
- 2000 character limit
- Soft delete with preservation
- Full CRUD operations

</td>
<td width="50%">

**📎 File Attachments**
- MinIO object storage
- 10MB per file, 20 files/task
- All file types supported
- Download tracking & audit
- Secure team access

</td>
</tr>
<tr>
<td width="50%">

**📚 Enhanced Documentation**
- Business logic for all endpoints
- Authorization rules detailed
- Validation constraints documented
- Request/response examples
- Error scenarios explained

</td>
<td width="50%">

**🧪 Testing Excellence**
- 350+ comprehensive tests
- 85%+ code coverage
- Edge cases covered
- Security & authorization testing
- Business logic validation

</td>
</tr>
</table>

---

## 📊 Key Metrics at a Glance

```
🏗️  8 Modules          📡  65+ REST Endpoints      🧪  350+ Unit Tests
💾  7 Database Tables   🔄  8 Flyway Migrations    📈  85%+ Test Coverage
💻  8,000+ Lines Code   🔐  JWT + BCrypt Security  ☁️  Live on Railway.app
```

---

## ✨ Key Features

### Core Functionality
- ✅ **User Management** - Registration, authentication, and profile management
- ✅ **JWT Authentication** - Secure token-based authentication with refresh tokens
- ✅ **Team Management** - Multi-tenant team creation and member management
- ✅ **Project Management** - Complete project lifecycle with status tracking
- ✅ **Task Management** - Task creation, assignment, and status transitions
- ✅ **Comment System** - Task discussions, updates, and feedback with team-based access control
- ✅ **File Attachments** - Upload, download, and manage files on tasks (MinIO storage, 10MB limit, 20 files/task)
- ✅ **Role-Based Access Control** - Multi-level authorization (Admin, Owner, Admin, Member)

### Technical Features
- ✅ **Soft Delete Pattern** - Data preservation with logical deletion
- ✅ **Audit Trails** - Complete tracking of who created/updated what and when
- ✅ **State Machine Validation** - Controlled status transitions for projects and tasks
- ✅ **Pagination Support** - Efficient handling of large datasets with configurable defaults
- ✅ **Database Migrations** - Version-controlled schema management with Flyway
- ✅ **Comprehensive Validation** - Input validation at DTO and service layers
- ✅ **Exception Handling** - Custom exceptions with meaningful error messages
- ✅ **API Documentation** - Comprehensive Swagger/OpenAPI documentation with business logic details
- ✅ **File Storage** - MinIO object storage for attachments with metadata tracking
- ✅ **Global Configuration** - Centralized pagination defaults (createdAt,DESC) preventing API errors

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
- **MinIO** - Object storage for file attachments

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
├── comment/        # Comment system for tasks
├── attachment/     # File attachment management
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

#### Comment System
```
POST   /api/comments               - Create comment on task
GET    /api/comments/{id}          - Get comment by ID
GET    /api/comments/task/{id}     - Get task comments (paginated)
PATCH  /api/comments/{id}          - Update comment (author only)
DELETE /api/comments/{id}          - Delete comment (author/team admin)
GET    /api/comments/my-comments   - Get my comments
GET    /api/comments/user/{id}     - Get user comments (Admin)
GET    /api/comments/admin/all     - Get all comments (Admin)
```

#### File Attachments
```
POST   /api/attachments/task/{id}  - Upload attachment (max 10MB)
GET    /api/attachments/{id}       - Get attachment metadata
GET    /api/attachments/task/{id}  - Get task attachments (paginated)
GET    /api/attachments/{id}/download - Download attachment file
DELETE /api/attachments/{id}       - Delete attachment (uploader/team admin)
GET    /api/attachments/my-attachments - Get my attachments
GET    /api/attachments/admin/all  - Get all attachments (Admin)
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

### API Documentation Quality
All endpoints include comprehensive Swagger documentation with:
- ✅ **Detailed business logic** explanations for each operation
- ✅ **Complete authorization rules** (who can access what)
- ✅ **Validation constraints** (field requirements, max lengths, formats)
- ✅ **Status transition rules** (valid state changes for tasks/projects)
- ✅ **Pagination details** with examples (page, size, sort)
- ✅ **Request/response examples** in JSON format
- ✅ **All HTTP status codes** with descriptions
- ✅ **File upload constraints** (size limits, format restrictions)
- ✅ **Default sorting** configured globally (createdAt,DESC) preventing API errors

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
│  Task   │◀─────────────────────────────────│ Project │
│(assigned)                                  └─────────┘
└─────────┘
     │
     ├──────────▶ ┌───────────┐
     │            │  Comment  │
     │            └───────────┘
     │
     └──────────▶ ┌─────────────┐
                  │ Attachment  │
                  └─────────────┘
```
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
- **Relationships**: Belongs to project, assigned to user, has many comments, has many attachments

#### Comment
- **Fields**: id, content, taskId, userId, status, createdBy, updatedBy
- **Status**: ACTIVE, DELETED
- **Max Content**: 2000 characters
- **Relationships**: Belongs to task, created by user
- **Business Rules**: Only author or team admin can update/delete

#### Attachment
- **Fields**: id, originalFilename, storedFilename, fileSize, contentType, taskId, userId, bucketName, objectKey, status
- **Status**: ACTIVE, DELETED
- **Max File Size**: 10MB
- **Max Files per Task**: 20
- **Storage**: MinIO object storage
- **Relationships**: Belongs to task, uploaded by user
- **Business Rules**: Only uploader or team admin can delete

### Database Migrations
All schema changes are managed through Flyway migrations in:
```
src/main/resources/db/migration/
├── V1__Create_Users_Table.sql
├── V3__Create_Refresh_Tokens_Table.sql
├── V4__Create_Teams_Table.sql
├── V5__Create_Team_Members_Table.sql
├── V6__Create_Projects_Table.sql
├── V7__Create_Tasks_Table.sql
├── V8__Create_Comments_Table.sql
└── V9__Create_Attachments_Table.sql
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
Total Tests: 350+
├── User Module: 48 tests
├── Auth Module: 29 tests
├── Team Module: 85 tests
├── TeamMember Module: 88 tests
├── Project Module: 56 tests
├── Task Module: 61 tests
├── Comment Module: 45+ tests (comprehensive edge cases)
└── Attachment Module: 40+ tests (file handling scenarios)
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
│   │   │   ├── comment/
│   │   │   ├── attachment/
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
│           ├── task/
│           ├── comment/
│           └── attachment/
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
- Comment system for task discussions
- File attachments with MinIO storage (10MB limit, 20 files/task)
- Comprehensive API documentation with business logic
- Global pagination configuration
- Comprehensive testing (350+ tests)

### 🔄 Phase 2: Advanced Features (In Progress)
- [x] Comment system for tasks ✅
- [x] File attachments (with MinIO) ✅
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
Modules: 8 (User, Auth, Team, TeamMember, Project, Task, Comment, Attachment)
Service Methods: 70+
REST Endpoints: 65+
Unit Tests: 350+
Lines of Code: 8000+
Test Coverage: High (85%+)
Database Tables: 7
Flyway Migrations: 8
Object Storage: MinIO for attachments
Documentation: Comprehensive Swagger with business logic
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
