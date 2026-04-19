# Docker Deployment Guide

This guide covers containerized deployment of the Spring Boot Blog Platform using Docker and Docker Compose with Flyway database migrations.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                    Docker Compose Stack                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────┐      ┌──────────────┐      ┌──────────────┐  │
│  │  Prometheus │      │    Grafana   │      │   Valkey     │  │
│  │  (metrics)  │      │  (dashboard) │      │  (Redis)     │  │
│  └──────┬──────┘      └──────────────┘      └──────┬───────┘  │
│         │                                             │        │
│         └──────────────────────┬──────────────────────┘        │
│                                │                               │
│                    ┌───────────▼──────────┐                  │
│                    │  blog-app (Spring)   │                  │
│                    │  Port: 8080          │                  │
│                    └───────────┬──────────┘                  │
│                                │                              │
│                    ┌───────────▼──────────┐                  │
│                    │     MySQL 8.0        │                  │
│                    │  Port: 3307 (host)   │                  │
│                    └───────────────────────┘                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Prerequisites

- **Docker**: 20.10+ ([Install Docker](https://docs.docker.com/get-docker/))
- **Docker Compose**: 2.0+ (usually included with Docker Desktop)
- **Memory**: Minimum 4GB RAM recommended (2GB for MySQL + 1GB for app + 1GB for others)
- **Disk**: 10GB free space for database volume

## Quick Start

### 1. Clone and Setup

```bash
cd /home/ng-dev/ng-dev/java/blog-platform-spring-boot
```

### 2. Configure Environment

Copy the example environment file and edit as needed:

```bash
cp .env.example .env
nano .env  # or use any text editor
```

**Critical settings**:
- `MYSQL_ROOT_PASSWORD` - Change from default!
- `JWT_SECRET` - Generate a strong secret (min 256-bit)
- OAuth credentials (optional for development)

Generate JWT secret:
```bash
# Linux/macOS
openssl rand -base64 32

# Or Java
java -jar your-app.jar --generate-jwt-secret
```

### 3. Start All Services

```bash
# Start all services in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# Follow specific service logs
docker-compose logs -f blog-app
docker-compose logs -f mysql
```

### 4. Verify Services

Check container status:
```bash
docker-compose ps
```

Expected output:
```
     Name                     Command               State                    Ports
----------------------------------------------------------------------------------------
mysql_db         docker-entrypoint.sh mysqld      Up      0.0.0.0:3307->3306/tcp
valkey           docker-entrypoint.sh valkey ...  Up      0.0.0.0:6379->6379/tcp
blog-app         java -XX:+UseContainerSuppo ...  Up      0.0.0.0:8080->8080/tcp
prometheus       /bin/prometheus --config.f ...  Up      0.0.0.0:9090->9090/tcp
grafana          /run.sh                          Up      0.0.0.0:3000->3000/tcp
```

Check application health:
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "MySQL", "validationQuery": "isValid()" } },
    "diskSpace": { "status": "UP" },
    "flyway": { "status": "UP" }
  }
}
```

### 5. Verify Flyway Migrations

Check migration status:
```bash
# View Flyway info via Spring Boot Actuator
curl http://localhost:8080/actuator/flyway

# Or check MySQL directly
docker-compose exec mysql mysql -uroot -p123456789 -e "SELECT * FROM flyway_schema_history;"
```

## Database Access

### Connect via MySQL Client

```bash
# Using docker-compose exec
docker-compose exec mysql mysql -uroot -p

# Or from host (port 3307)
mysql -h 127.0.0.1 -P 3307 -u root -p

# Password is from .env: MYSQL_ROOT_PASSWORD
```

### Database Management Tools

**phpMyAdmin** (optional, add to docker-compose):
```yaml
phpmyadmin:
  image: phpmyadmin/phpmyadmin
  container_name: phpmyadmin
  ports:
    - "8081:80"
  environment:
    PMA_HOST: mysql
    PMA_PORT: 3306
    PMA_USER: root
    PMA_PASSWORD: ${MYSQL_ROOT_PASSWORD}
  depends_on:
    - mysql
  networks:
    - spring_network
```

**Adminer** (lighter alternative):
```yaml
adminer:
  image: adminer
  container_name: adminer
  ports:
    - "8081:8080"
  depends_on:
    - mysql
  networks:
    - spring_network
```

## Migration Workflow

### How Flyway Works in Docker

1. **On First Startup**: Spring Boot's Flyway auto-configuration runs automatically on application startup
   - Scans classpath for migrations in `db/migration/`
   - Executes pending migrations in order (V1 → V8)
   - Records applied migrations in `flyway_schema_history` table

2. **Schema Validation**: With `spring.jpa.hibernate.ddl-auto=validate`, Hibernate validates entity mappings against actual schema
   - **Important**: Never use `update` or `create-drop` in production!
   - Flyway owns schema changes, Hibernate only validates

3. **MySQL Initialization**: The migration files are also mounted to `/docker-entrypoint-initdb.d/` in the MySQL container
   - These run **only once** when MySQL data directory is empty
   - Provides fallback if app-side Flyway fails
   - Safe to keep, won't re-run on volume reuse

### Adding New Migrations

1. Create new migration file in `src/main/resources/db/migration/`:
   ```bash
   # Example: add created_by column to post table
   touch src/main/resources/db/migration/V9__add_created_by_to_post.sql
   ```

2. Write migration SQL (see examples in existing V1-V8 files)

3. Rebuild and restart:
   ```bash
   ./gradlew clean bootJar
   docker-compose build blog-app
   docker-compose up -d
   ```

Flyway will automatically detect and apply the new migration on startup.

### Migration Best Practices

- **Never modify** existing migration files that have been applied
- **Always test** migrations in a local/dev environment first
- **Backup** production database before applying new migrations
- Use **down migrations** (`U9__...`) only if you need rollback capability
- Keep migrations **idempotent** where possible (`CREATE TABLE IF NOT EXISTS`)

## Useful Commands

### Application Management

```bash
# Start/Stop/Restart
docker-compose up -d          # Start all
docker-compose down           # Stop all
docker-compose restart        # Restart all
docker-compose restart blog-app  # Restart app only

# Rebuild after code changes
docker-compose build --no-cache
docker-compose up -d --build

# View logs with timestamps
docker-compose logs -f --timestamp

# Execute command in container
docker-compose exec blog-app java -jar /app/app.jar --help
```

### Database Operations

```bash
# MySQL console
docker-compose exec mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} spring_blog

# Import SQL dump
docker-compose exec -T mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} spring_blog < backup.sql

# Export database
docker-compose exec mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} spring_blog > backup.sql

# Clean MySQL volume (WARNING: deletes all data)
docker-compose down -v
docker-compose up -d
```

### Monitoring

```bash
# Application metrics (Prometheus)
curl http://localhost:8080/actuator/prometheus

# Health check
curl http://localhost:8080/actuator/health

# Flyway migration status
curl http://localhost:8080/actuator/flyway

# Grafana (default login: admin/admin)
open http://localhost:3000

# Prometheus
open http://localhost:9090
```

## Troubleshooting

### App Fails to Start

```bash
# Check logs
docker-compose logs blog-app | tail -50

# Common issues:
# 1. Database not ready yet → wait 30-60 seconds and check again
# 2. Wrong DB credentials → verify .env file matches MySQL container settings
# 3. Flyway migration error → check migration SQL syntax
```

### Flyway Migration Errors

```bash
# Check current migration status
docker-compose exec mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SELECT * FROM flyway_schema_history ORDER BY installed_rank;"

# If migration fails, fix SQL then:
docker-compose exec mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "DELETE FROM flyway_schema_history WHERE version='V8';"  # Remove failed migration
docker-compose restart blog-app  # Retry

# OR clean entire DB (development only):
docker-compose down -v
docker-compose up -d
```

### MySQL Connection Refused

- Verify port 3307 not in use: `lsof -i :3307`
- Check MySQL container logs: `docker-compose logs mysql`
- Ensure volume permissions: `docker-compose exec mysql ls -la /var/lib/mysql`

### Out of Memory

Increase Docker Desktop memory limit (Settings → Resources → Memory: 4GB+)

### Disk Space

Clear unused Docker resources:
```bash
docker system prune -a --volumes  # WARNING: removes all unused images/containers
```

## Production Deployment

For production, consider:

### 1. Environment Security
- Change all default passwords
- Use Docker secrets or external secret manager (AWS Secrets Manager, HashiCorp Vault)
- Enable SSL/TLS for database connections
- Set `cookieSecure: true` in application-prod.yml

### 2. Performance Tuning
```yaml
# docker-compose.prod.yml
services:
  mysql:
    environment:
      MYSQL_INNODB_BUFFER_POOL_SIZE: 2G
      MYSQL_MAX_CONNECTIONS: 200
    command: [
      'mysqld',
      '--innodb-buffer-pool-size=2G',
      '--max-connections=200',
      '--character-set-server=utf8mb4',
      '--collation-server=utf8mb4_unicode_ci'
    ]
```

### 3. Backup Strategy
```bash
# Automated daily backup
docker-compose exec mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} spring_blog > backup_$(date +%Y%m%d).sql

# Cron job example
0 2 * * * cd /path/to/project && docker-compose exec -T mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} spring_blog > backups/backup_$(date +\%Y\%m\%d).sql
```

### 4. Docker Networking
- Use custom bridge network (already configured)
- Consider overlay network for multi-host (Docker Swarm/K8s)
- Restrict port exposure (only 80/443 to external)

### 5. Resource Limits
```yaml
services:
  blog-app:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
    environment:
      JAVA_OPTS: "-Xmx1G -Xms512m"
```

### 6. High Availability
- MySQL replication (master-slave or InnoDB Cluster)
- Valkey sentinel for Redis HA
- Load balancer (nginx, traefik) in front of app instances
- Multiple app replicas with sticky sessions

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Deploy to Production
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build and push Docker image
        run: |
          docker build -t your-registry/blog-platform:${{ github.sha }} .
          docker push your-registry/blog-platform:${{ github.sha }}
      - name: Deploy with Docker Compose
        run: |
          ssh user@server "cd /path/to/project && \
            docker-compose pull && \
            docker-compose up -d --build"
```

## Cleanup

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (WARNING: deletes all data)
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Clean Docker system (all unused resources)
docker system prune -a --volumes
```

## Support

- **Spring Boot Logs**: `docker-compose logs -f blog-app`
- **Database Logs**: `docker-compose logs -f mysql`
- **Flyway Issues**: Check `flyway_schema_history` table for failed migrations
- **Spring Actuator**: `/actuator/health`, `/actuator/flyway`, `/actuator/metrics`

---

## Migration Files Reference

All Flyway migrations are located in: `src/main/resources/db/migration/`

| Version | Description | Dependencies |
|---------|-------------|--------------|
| V1 | Core tables (roles, category, tags, meme, traffic, templates, newsletters) | None |
| V2 | User & auth (user, social_media_links, refresh_tokens, api_keys, oauth, jwt_blacklist) | roles (V1) |
| V3 | Content (post, series, series_post) | user (V2) |
| V4 | Join tables (user_roles, post_like, post_category, post_tags) | user, roles, post, category, tags |
| V5 | Interactions (comment, rating, bookmark) | user, post |
| V6 | Notifications (notifications, prefs, history) | user |
| V7 | Newsletters (campaigns, email_logs) | user, newsletter_subscribers |
| V8 | Reporting (article_reports, oauth_audit, video) | user, post, oauth_accounts |

Detailed migration documentation: `src/main/resources/db/migration/README.md`
