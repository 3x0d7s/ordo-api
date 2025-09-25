# Ordo - Project Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Latest-blue.svg)](https://www.postgresql.org/)
[![Vue.js](https://img.shields.io/badge/Vue.js-3-green.svg)](https://vuejs.org/)

Ordo is a modern project management system inspired by Trello, built with Spring Boot and Vue.js. It provides a collaborative workspace for teams to organize projects using boards, lists, and cards with real-time updates.

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

### Frontend (Vue.js)
```
src/
├── components/        # Reusable UI components
├── views/            # Page-level components
├── stores/           # Pinia state management
├── services/         # API communication
├── composables/      # Vue composition utilities
└── router/           # Route definitions
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Node.js 18+
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

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd ordo-frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure environment variables**
   ```bash
   # Create .env file
   echo "VITE_API_URL=http://localhost:8080" > .env
   echo "VITE_GOOGLE_CLIENT_ID=your_google_client_id" >> .env
   ```

4. **Start development server**
   ```bash
   npm run dev
   ```

The frontend will be available at `http://localhost:5173`

## 🧪 Testing

### Backend Testing

**Run all tests with H2 (fast)**
```bash
./mvnw test
```

**Run integration tests with PostgreSQL Testcontainers**
```bash
./mvnw test -Dspring.profiles.active=testcontainers
```

**Run specific test class**
```bash
./mvnw test -Dtest=WorkspaceServiceTest
```

### Frontend Testing
```bash
cd ordo-frontend
npm run lint
```

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

### Frontend
- **Framework**: Vue.js 3
- **State Management**: Pinia
- **Routing**: Vue Router 4
- **Styling**: TailwindCSS 4
- **HTTP Client**: Axios
- **Build Tool**: Vite
- **Real-time**: STOMP.js + SockJS

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
- **test** - H2 in-memory database for fast testing
- **testcontainers** - PostgreSQL with Docker for integration testing

### Key Configuration Files
- `application.yml` - Main application configuration
- `application-test.yml` - Test-specific settings
- `application-testcontainers.yml` - Testcontainers configuration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Spring Boot best practices
- Write tests for new features
- Use conventional commit messages
- Ensure code passes linting and tests

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

### 🚧 In Progress
- Drag and drop functionality
- File attachments
- Email notifications
- Performance optimizations

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋‍♂️ Support

If you have questions or need help, please:
1. Check the [API documentation](http://localhost:8080/swagger-ui)
2. Review the [testing guide](TESTING-WITH-TESTCONTAINERS.md)
3. Open an issue on GitHub

---

**Built with ❤️ using Spring Boot and Vue.js**
