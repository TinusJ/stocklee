# Docker Setup for Stocklee

This document describes how to run the Stocklee application using Docker and PostgreSQL.

## Prerequisites

- Docker and Docker Compose installed on your system
- Internet connection for downloading Docker images

## Quick Start

1. **Build and start the application with PostgreSQL:**
   ```bash
   docker-compose up --build
   ```

2. **Access the application:**
   - Application: http://localhost:8080
   - PostgreSQL: localhost:5432

3. **Stop the application:**
   ```bash
   docker-compose down
   ```

## Configuration

### Database Configuration
- **Database Name**: stocklee
- **Username**: postgres
- **Password**: password
- **Port**: 5432
- **Connection URL**: `jdbc:postgresql://postgres:5432/stocklee`

### Application Configuration
- **Port**: 8080
- **Java Version**: 21 (in Docker containers)
- **Hibernate DDL Mode**: update

## Docker Services

### PostgreSQL Service (`postgres`)
- **Image**: postgres:15
- **Container Name**: stocklee-postgres
- **Port Mapping**: 5432:5432

### Application Service (`stocklee-app`)
- **Build**: Multi-stage Dockerfile with Java 21
- **Container Name**: stocklee-app
- **Port Mapping**: 8080:8080
- **Dependencies**: Depends on PostgreSQL service

## Development Notes

- The application uses PostgreSQL in production/Docker environment
- Tests continue to use H2 database for fast execution
- Database schema is automatically managed by Hibernate (DDL mode: update)
- Environment variables in docker-compose.yml override application.yml settings

## Troubleshooting

1. **Port conflicts**: Ensure ports 5432 and 8080 are not in use by other applications
2. **Build issues**: Run `docker-compose build --no-cache` to rebuild from scratch
3. **Database connection**: The application waits for PostgreSQL to be ready via `depends_on`

## Logs

View application logs:
```bash
docker-compose logs stocklee-app
```

View PostgreSQL logs:
```bash
docker-compose logs postgres
```