# Blog Platform - Spring Boot Backend

[![Java](https://img.shields.io/badge/Java-25-ED8B00.svg?style=flat&logo=openjdk&logoColor=white)](https://jdk.java.net/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-6DB33F.svg?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.5-02303A.svg?style=flat&logo=gradle&logoColor=white)](https://gradle.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1.svg?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-24.0-2496ED.svg?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)

A modern, production-ready blog platform backend built with Spring Boot, featuring advanced content management, real-time notifications, multi-channel authentication, and comprehensive monitoring.

---

## Table of Contents

1. [Overview](#overview)
2. [System Design & Architecture](#system-design--architecture)
3. [Tech Stack](#tech-stack)
4. [Key Features](#key-features)
5. [Implementation Highlights](#implementation-highlights)
6. [Challenges & Solutions](#challenges--solutions)
7. [Getting Started](#getting-started)
8. [API Documentation](#api-documentation)

---

## Overview

### Purpose & Core Functionality

Blog Platform is a comprehensive content management system designed for bloggers, content creators, and publishers. It provides a robust backend infrastructure supporting:

- **Content Management**: Create, edit, schedule, and publish blog posts with rich metadata
- **User Engagement**: Comments, ratings, likes, and bookmarking system
- **Multi-tenant Architecture**: Role-based access control for authors, admins, and readers
- **Distribution Channels**: Email newsletters, WebSocket notifications, and social media integration

### Target Users

| Role | Description |
|------|-------------|
| **Content Authors** | Write, edit, and publish articles with rich formatting |
| **Readers** | Browse, search, bookmark, and engage with content |
| **Administrators** | Moderate content, manage users, and access analytics |
| **API Consumers** | Third-party integrations via API keys |

### Core Value Propositions

- **Scalability**: Horizontal scaling support with Redis caching and stateless authentication
- **Security**: Enterprise-grade security with JWT tokens, API keys, and XSS prevention
- **Performance**: Optimized view counting, async processing, and database indexing
- **Observability**: Full monitoring with Prometheus, Grafana, and structured logging

---

## System Design & Architecture

### Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                              │
│         (React/Vue Frontend, Mobile Apps, Third-party)            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                      GATEWAY LAYER                             │
│         CORS Configuration, Rate Limiting, SSL                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐    │
│  │   Auth Layer   │  │   Business   │  │   Notification   │    │
│  │  JWT/API Keys  │  │    Logic     │  │   WebSocket/     │    │
│  │    API Keys    │  │   Services   │  │   Email/Kafka    │    │
│  └──────────────┘  └──────────────┘  └──────────────────┘    │
└─────────────────────────────────────────────────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          ▼                   ▼                   ▼
┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
│   MySQL 8.4     │ │  Valkey/Redis   │ │   File Storage  │
│  (Primary DB)   │ │    (Cache)      │ │   (Uploads)     │
└─────────────────┘ └─────────────────┘ └─────────────────┘
```

### Data Flow

1. **Authentication Flow**: JWT → Security Filter Chain → User Context → Service Layer
2. **Content Creation**: Request → Validation → Sanitization (OWASP) → Database → Cache Invalidation
3. **Real-time Notifications**: Event → Kafka/WebSocket → User Subscription → Push Delivery
4. **View Counting**: Request → Valkey (buffer) → Scheduled Sync → MySQL (persistent)

### Project Structure

```
src/main/java/com/Nguyen/blogplatform/
├── BlogPlatformApplication.java     # Application entry point
├── Component/                      # Utility components
├── Enum/                          # Domain enums (Role, Status, etc.)
├── config/                         # Configuration classes
│   ├── SecurityConfig.java         # Spring Security setup
│   ├── ValkeyConfig.java           # Redis/Valkey client
│   ├── WebSocketConfig.java        # STOMP messaging
│   └── OpenAPIConfig.java          # Swagger documentation
├── controller/                     # REST API controllers
│   ├── Authentication/            # Auth endpoints
│   ├── Post/                      # Content management
│   ├── admin/                     # Admin operations
│   └── Notification/              # WebSocket controllers
├── model/                          # JPA entities
│   ├── Post.java                   # Blog post entity
│   ├── User.java                   # User entity
│   ├── Comment.java                # Comment entity
│   └── ...
├── repository/                     # Spring Data JPA
├── service/                        # Business logic
│   ├── auth/                       # Authentication services
│   ├── notification/               # Multi-channel notifications
│   ├── post/                       # Content services
│   └── scheduled/                  # Background tasks
├── security/                       # Security components
│   ├── JwtUtils.java               # Token management
│   └── AuthTokenFilter.java        # JWT filter
└── util/                           # Utility classes
```

---

## Tech Stack

### Backend Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 25 | Primary language |
| Spring Boot | 4.0.0-SNAPSHOT | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | Data access layer |
| Spring WebSocket | 6.x | Real-time messaging |
| Spring Cache | 4.0.0-M3 | Caching abstraction |
| Spring Kafka | 3.x | Message streaming |
| Spring Actuator | 3.x | Health monitoring |
| Thymeleaf | 3.x | Template engine |
| Lombok | 1.18+ | Code generation |
| ModelMapper | 3.2.6 | DTO mapping |
| Flyway | 12.4.0 | Database migrations |
| Swagger/OpenAPI | 2.8.14 | API documentation |

### Database & Storage

| Component | Technology | Usage |
|-----------|------------|-------|
| Primary Database | MySQL 8.4 | Relational data |
| Cache Layer | Valkey 8.0 (Redis fork) | Session, view counts, caching |
| File Storage | Local filesystem (extensible to S3) | Avatars, uploads |

### DevOps & Infrastructure

| Component | Technology |
|-----------|------------|
| Containerization | Docker |
| Orchestration | Docker Compose |
| Monitoring | Prometheus + Grafana |
| Java Runtime | Eclipse Temurin 25 JRE |
| Build Tool | Gradle 8.x |

### Third-Party Integrations

| Service | Purpose |
|---------|---------|
| SMTP (Mailtrap/SES) | Email delivery |
| Telegram Bot | Admin alerts |

---

## Key Features

### 1. Multi-Channel Authentication System

The platform supports two authentication methods with seamless integration:

- **JWT Token Authentication**: Stateless authentication with refresh tokens
- **API Key Authentication**: For external service integrations

### 2. Content Management System

Advanced blog post management with:

- **Draft/Published/Scheduled States**: Flexible publishing workflow
- **Rich Metadata**: SEO-friendly slugs, excerpts, thumbnails
- **Categorization & Tagging**: Hierarchical content organization
- **Series Management**: Group related posts into collections
- **Content Sanitization**: OWASP HTML Sanitizer for XSS prevention

### 3. Engagement Features

- **Comment System**: Nested comments with moderation
- **Rating System**: 1-5 star ratings per post
- **Like System**: One-click content appreciation
- **Bookmarks**: Save posts for later reading
- **Post Reporting**: Content moderation workflow

### 4. Notification Infrastructure

Multi-channel notification system supporting:

- **WebSocket Real-time**: STOMP protocol for live updates
- **Email Notifications**: Thymeleaf templates with SMTP
- **In-app Notifications**: Persistent notification center
- **Digest Mode**: Batched notification summaries
- **Quiet Hours**: User-configurable do-not-disturb

### 5. Analytics & Monitoring

- **View Counting**: Buffered counter with Valkey, async MySQL sync
- **Traffic Analysis**: Request tracking with geographic data
- **Prometheus Metrics**: JVM, HTTP, and custom business metrics
- **Grafana Dashboards**: Visual monitoring and alerting
- **Structured Logging**: Correlation IDs and log aggregation

### 6. API & Integration

- **RESTful API**: Resource-oriented endpoints
- **OpenAPI Documentation**: Interactive Swagger UI
- **API Key Management**: Granular access control
- **Global Search**: Full-text search across posts, categories, tags
- **External Webhooks**: Event-driven integrations

---

## Implementation Highlights

### Performance Optimizations

**View Count Buffering with Valkey**
The platform uses a write-behind caching strategy for view counts:
- Views are recorded in Valkey with TTL
- Background scheduler syncs to MySQL periodically
- Reduces database write load by ~90%

**Async Processing**
- Email sending via `@Async`
- Notification processing via Kafka topics
- Scheduled tasks with `@EnableScheduling`

### Caching Strategy

```java
@/home/ng-dev/ng-dev/java/blog-platform-spring-boot/src/main/java/com/Nguyen/blogplatform/config/CacheConfig.java:1-10
@Configuration
@EnableCaching
public class CacheConfig {
    // Spring Cache abstraction with Valkey backend
    // Used for: User sessions, post metadata, search results
}
```

---

## Challenges & Solutions

### Challenge 1: Token Revocation in Stateless JWT

**Problem**: JWT tokens are stateless by design; revoking them (e.g., user logout, security breach) is challenging.

**Solution**: 
- Implemented JWT Blacklist with SHA-256 token hashing
- Added JTI (JWT ID) claim for unique token identification
- Database-backed blacklist with automatic expiration cleanup

**Location**: `@/home/ng-dev/ng-dev/java/blog-platform-spring-boot/src/main/java/com/Nguyen/blogplatform/security/JwtUtils.java:203-246`

### Challenge 2: XSS Prevention in Rich Content

**Problem**: Blog posts may contain HTML; need to sanitize while preserving allowed tags.

**Solution**:
- OWASP Java HTML Sanitizer integration
- Policy-based whitelisting of HTML elements
- Automatic sanitization on post creation/update

**Location**: `@/home/ng-dev/ng-dev/java/blog-platform-spring-boot/build.gradle:78`

### Challenge 3: Real-time Notification Delivery

**Problem**: Delivering notifications to specific users across multiple sessions and channels.

**Solution**:
- WebSocket with STOMP protocol for real-time delivery
- User-specific queues (`/user/queue/notifications`)
- Fallback to email for offline users
- Preference-based channel selection

**Location**: `@/home/ng-dev/ng-dev/java/blog-platform-spring-boot/src/main/java/com/Nguyen/blogplatform/service/notification/NotificationService.java:38-126`

### Challenge 4: Database Migration in Production

**Problem**: Zero-downtime schema changes with data preservation.

**Solution**:
- Flyway migration framework
- Version-controlled SQL migrations
- Rollback strategies for each change

---

## Getting Started

### Prerequisites

- Java 25+
- Docker & Docker Compose
- Gradle 8.x (or use wrapper)

### Quick Start with Docker

```bash
# Clone repository
git clone <repository-url>
cd blog-platform-spring-boot

# Copy environment configuration
cp .env.example .env
# Edit .env with your configurations

# Start infrastructure and application
docker-compose up -d

# Access points:
# - Application: http://localhost:8080
# - Swagger UI: http://localhost:8080/swagger-ui
# - Grafana: http://localhost:3000
# - Prometheus: http://localhost:9090
```

### Local Development

```bash
# Start infrastructure only
docker-compose up -d mysql valkey

# Run application
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Environment Configuration

Key variables in `.env`:

```properties
# Database
MYSQL_ROOT_PASSWORD=secure_password
DB_URL=jdbc:mysql://localhost:3306/spring_blog

# JWT
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=86400000

# Email
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=app_password
```

---

## API Documentation

### OpenAPI/Swagger

Interactive documentation available at:
- Development: `http://localhost:8080/swagger-ui`
- API Docs JSON: `http://localhost:8080/api-docs`

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | User registration |
| POST | `/api/v1/auth/login` | JWT login (sets cookie) |
| POST | `/api/v1/auth/refresh-token` | Refresh access token |
| POST | `/api/v1/auth/logout` | Logout & revoke token |
| GET | `/api/v1/auth/me` | Current user info |

### Post Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/post` | No | List published posts |
| GET | `/api/v1/post/{slug}` | No | Get post by slug |
| POST | `/api/v1/post` | Yes | Create new post |
| PUT | `/api/v1/post/{id}` | Yes | Update post |
| DELETE | `/api/v1/post/{id}` | Yes | Delete post |
| POST | `/api/v1/post/{id}/like` | Yes | Toggle like |
| POST | `/api/v1/post/{id}/rate` | Yes | Rate post (1-5) |
| GET | `/api/v1/post/{id}/related` | No | Get related posts |

### Admin Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/admin/users` | List all users |
| POST | `/api/v1/admin/users/{id}/ban` | Ban/unban user |
| GET | `/api/v1/admin/reports` | View content reports |
| GET | `/api/v1/admin/statistics` | Platform analytics |
| GET | `/api/admin/analytics/summary` | Dashboard summary (views, users, posts, likes) |
| GET | `/api/admin/analytics/growth/monthly` | Monthly growth analytics |
| GET | `/api/admin/analytics/users/monthly` | New users per month |
| GET | `/api/admin/analytics/posts/monthly` | Post growth per month |
| GET | `/api/admin/analytics/posts/top-viewed` | Top viewed posts |
| GET | `/api/admin/analytics/posts/top-liked` | Top liked posts |
| GET | `/api/admin/analytics/posts/top-popular` | Top popular posts |
| GET | `/api/admin/analytics/authors/top` | Top authors by engagement |
| POST | `/api/admin/analytics/export` | Export analytics report (PDF/Excel) |

---

## Best Practices & Lessons Learned

1. **Security First**: Implement XSS sanitization early; retrofitting is difficult
2. **Async by Default**: User-facing operations should not wait for external calls
3. **Database Indexing**: Query performance degrades significantly without proper indexing on slugs and foreign keys
4. **Caching Strategy**: Use write-behind for high-frequency writes (views), write-through for consistency-critical data
5. **API Versioning**: Prefix all endpoints with `/api/v1/` from day one
6. **Configuration Externalization**: Never commit secrets; use environment variables

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License.

---

## Contact

**Developer**: Nguyen  
**Email**: dinhnguyen1812002@gmail.com  
**Project Repository**: [GitHub](https://github.com/yourusername/blog-platform-spring-boot)

---

<p align="center">
  Built with ❤️ using Spring Boot
</p>
