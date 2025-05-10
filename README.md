# Brokerage Order API

A REST API service for a brokerage firm that allows employees to manage stock orders for their customers.

## Overview

This application provides backend services for managing stock orders in a brokerage system, including:

- Creating buy/sell orders
- Listing orders with filters
- Canceling pending orders
- Listing customer assets
- Matching orders (admin feature)

## Technology Stack

- Java 17
- Spring Boot 3.x
- Spring Security with JWT Authentication
- Spring Data JPA
- H2 In-Memory Database
- Maven
- ELK Stack (Elasticsearch, Logstash, Kibana) for logging and monitoring

## Prerequisites

- Java 17 or higher
- Docker and Docker Compose (for running ELK stack)
- No Maven installation needed (Maven wrapper is included)

## Building the Application

The project includes Maven wrapper scripts, so you don't need Maven installed:

```bash
# On Linux/Mac/Windows with PowerShell
./mvnw clean package

# On Windows with Command Prompt
mvnw.cmd clean package
```

## Running the Application

```bash
# On Linux/Mac/Windows with PowerShell
./mvnw spring-boot:run

# On Windows with Command Prompt
mvnw.cmd spring-boot:run
```

The application will be available at `http://localhost:8080`.

An H2 console is available at `http://localhost:8080/h2-console` with the following connection details:
- JDBC URL: `jdbc:h2:mem:brokerage`
- Username: `root`
- Password: ` ` (empty)

## Monitoring and Logging

The application is integrated with the ELK Stack for comprehensive logging and monitoring:

### Setting up the ELK Stack

The project includes a pre-configured Docker Compose setup for the ELK stack in the `logstash` directory:

1. Navigate to the project root directory
2. Run the ELK stack using the provided Docker Compose configuration:
```bash
cd logstash
docker-compose up -d
```

This will start:
- Elasticsearch on port 9200
- Logstash on port 5000 (configured to receive JSON logs)
- Kibana on port 5601

The configuration files are already included in the project:
- `logstash/docker-compose.yaml`: Docker Compose configuration for the ELK stack
- `logstash/logstash.conf`: Logstash pipeline configuration

### Kibana Dashboard

Kibana provides visualization of application logs and metrics, accessible at:
- URL: `http://localhost:5601`

### Setting up Kibana Index Pattern

To view application logs in Kibana, you need to create an index pattern:

1. Open Kibana at `http://localhost:5601`
2. Navigate to Management → Stack Management → Kibana → Index Patterns
3. Click "Create index pattern"
4. Enter `logstash-*` as the index pattern
5. Select `@timestamp` as the Time field
6. Click "Create index pattern"

After creating the index pattern, you can view logs in the Discover section of Kibana.

### Log Management

- Application logs are stored in the `logs/` directory
- Logstash processes these logs and forwards them to Elasticsearch
- Kibana provides a web interface for searching and visualizing these logs

## API Documentation

Swagger UI is available at `http://localhost:8080/swagger-ui/index.html`

### Core Endpoints

#### Authentication
- `POST /auth/login` - Authenticate a user and receive a JWT token

#### Orders
- `POST /orders` - Create a new order
- `GET /orders` - List orders with filters
- `DELETE /orders/{id}` - Cancel a pending order
- `POST /orders/{id}/match` - Match a pending order (admin only)

#### Assets
- `GET /assets` - List all assets for a customer

## Sample Users

The application is seeded with the following users:

### Admin User
- Username: `admin`
- Password: `adminPassword`
- Role: `admin`

### Regular User
- Username: `user1`
- Password: `user1Password`
- Role: `user`

## Authentication

All API endpoints (except login) require JWT authentication. 

1. Login to obtain a JWT token:
```
POST /auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "adminPassword"
}
```

2. Use the token in subsequent requests:
```
GET /orders?customerId=1&start=2023-01-01&end=2023-12-31
Authorization: Bearer {your-jwt-token}
```

## Authorization

- Regular users can only access and manipulate their own data
- Admin users can access and manipulate all customer data
- Order matching is restricted to admin users only

## Testing

### Running Tests

```bash
# On Linux/Mac/Windows with PowerShell
./mvnw test

# On Windows with Command Prompt
mvnw.cmd test
```

### Test Coverage

The application includes comprehensive tests for:
- Controller layer
- Service layer
- Security components

## Sample Requests

### Create Order
```
POST /orders?customerId=2&assetName=AAPL&side=BUY&size=5&price=150
Authorization: Bearer {your-jwt-token}
```

### List Orders
```
GET /orders?customerId=2&start=2024-01-01&end=2024-12-31
Authorization: Bearer {your-jwt-token}
```

### List Assets
```
GET /assets?customerId=2
Authorization: Bearer {your-jwt-token}
```

### Cancel Order
```
DELETE /orders/1
Authorization: Bearer {your-jwt-token}
```

### Match Order (Admin only)
```
POST /orders/1/match
Authorization: Bearer {your-jwt-token}
``` 