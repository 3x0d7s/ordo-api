# Ordo - Project Management System

Ordo is a project management system inspired by Trello, built with Spring Boot. It provides a collaborative workspace for teams to organize projects using boards, lists, and cards with real-time updates and flexible functionalities of managing workspace/board member roles.

## ✨ Features

### Core Functionality
- 📋 **Workspace Management** - Create and manage collaborative workspaces
- 🎯 **Kanban Boards** - Visual project management with customizable boards
- 📝 **Lists & Cards** - Organize tasks with drag-and-drop functionality
- ✅ **Task Management** - Create and track tasks within cards
- 💬 **Comments** - Real-time commenting system
- 🔄 **Real-time Updates** - WebSocket-powered live collaboration

### Security & Authentication
- 🔐 **JWT Authentication** - Secure token-based authentication
- 🌐 **OAuth2 Integration** - Sign in with Google and GitHub
- 👥 **Role-Based Access Control** - Granular permissions system
- 🔒 **Workspace Invitations** - Secure team member invitations

### Developer Experience
- 📚 **OpenAPI Documentation** - Interactive API documentation
- 🧪 **Comprehensive Testing** - Unit and integration tests with Testcontainers
- 🐳 **Docker Support** - Containerized testing environment
- 🔄 **Database Migrations** - Version-controlled schema with Flyway

## 🏗️ Architecture

### Backend (Spring Boot)
```
src/main/java/com/kyut/ordo/
├── feature/           # Domain-driven feature modules
│   ├── workspace/     # Workspace management
│   ├── board/         # Board operations
│   ├── list/          # List management
│   ├── card/          # Card operations
│   ├── task/          # Task management
│   ├── comment/       # Comment system
│   └── user/          # User management
├── security/          # Authentication & authorization
├── websocket/         # Real-time communication
└── common/            # Shared utilities
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- PostgreSQL 13+
- Docker (for testing)

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd ordo
   ```

2. **Configure environment variables**
   ```bash
   # Database
   export DB_URL=jdbc:postgresql://localhost:5432/ordo
   export DB_USERNAME=your_username
   export DB_PASSWORD=your_password
   
   # JWT
   export JWT_SECRET=your_jwt_secret_key
   
   # OAuth2 (optional)
   export OAUTH2_GOOGLE_CLIENT_ID=your_google_client_id
   export OAUTH2_GOOGLE_CLIENT_SECRET=your_google_client_secret
   export OAUTH2_GITHUB_CLIENT_ID=your_github_client_id
   export OAUTH2_GITHUB_CLIENT_SECRET=your_github_client_secret
   
   # CORS
   export CORS_ALLOWED_ORIGINS=http://localhost:5173
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

The backend will be available at `http://localhost:8080`

## 📚 API Documentation

When the backend is running, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui
- **OpenAPI Spec**: http://localhost:8080/api-docs

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.4.1
- **Language**: Java 21
- **Database**: PostgreSQL
- **Security**: Spring Security + JWT + OAuth2
- **Documentation**: OpenAPI 3
- **Testing**: JUnit 5, Testcontainers, MockMvc
- **Build Tool**: Maven
- **Database Migration**: Flyway
- **Mapping**: MapStruct
- **Real-time**: WebSocket + STOMP

## 🗄️ Database Schema

### Core Entities
- **Users** - User accounts with OAuth2 support
- **Workspaces** - Team collaboration spaces
- **Boards** - Project boards within workspaces
- **Lists** - Task lists within boards
- **Cards** - Individual work items
- **Tasks** - Subtasks within cards
- **Comments** - Card discussions

### Key Relationships
```
Workspace 1:N Board 1:N List 1:N Card 1:N Task
                                 ↓
                              Comment
```

## 🔧 Configuration

### Application Profiles
- **default** - Production configuration
- **testcontainers** - PostgreSQL with Docker for integration testing

### Key Configuration Files
- `application.yml` - Main application configuration
- `application-testcontainers.yml` - Testcontainers configuration

## 📋 Development Status

**Current Version**: 0.0.1-SNAPSHOT

### ✅ Completed Features
- User authentication (JWT + OAuth2)
- Workspace and board management
- Real-time updates via WebSocket
- Comprehensive permission system
- Full CRUD operations for all entities
- Responsive UI with TailwindCSS
- Comprehensive testing strategy

