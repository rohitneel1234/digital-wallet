# Digital Wallet & Transaction System

## Overview

Digital Wallet & Transaction System is a production-ready backend application built with **Spring Boot 3.x** and **Java 17**. It enables users to securely manage digital wallets, perform peer-to-peer fund transfers, and monitor transaction activities while following modern backend development best practices.

The application is designed using a layered architecture with a focus on **security, scalability, maintainability, and observability**.

---

# Features

## Wallet Management

* User registration and onboarding
* Automatic wallet creation for every registered user
* Retrieve wallet details
* Update wallet owner information
* Close wallet accounts
* Input validation for email, required fields, and positive transfer amounts

---

## Transaction Engine

* Secure fund transfers between wallets
* Automatic balance updates using Spring Data JPA
* Transaction history retrieval
* Filter transactions by:

  * Amount
  * Date
* Custom JPQL queries for advanced filtering

---

## Security

* JWT-based authentication
* Stateless authorization
* Role-based access control

### Roles

**ROLE_USER**

* Manage own wallet
* Perform fund transfers
* View personal transactions

**ROLE_ADMIN**

* Delete wallets
* Access system-wide data

---

## AOP (Aspect-Oriented Programming)

Implemented cross-cutting concerns using Spring AOP:

* Method execution logging
* Request parameter logging
* Performance monitoring
* Execution time tracking for critical operations

This keeps business logic clean while improving observability.

---

## Performance & Optimization

### Asynchronous Processing

* Background notification using `@Async`
* Sends transaction success notifications without blocking API responses

### Caching

* Cache frequently accessed wallet balances
* Reduce database load and improve performance

### Scheduled Tasks

Using `@Scheduled`, the application periodically:

* Logs system health
* Logs transaction statistics

---

## Monitoring

Spring Boot Actuator endpoints:

* `/actuator/health`
* `/actuator/metrics`

Custom Health Indicator:

* Checks transaction engine status

---

## Data Model

Entities:

* User
* Wallet
* Transaction

Relationships are implemented using JPA to maintain data integrity.

---

## Project Structure

```text
digital-wallet
│── src
│   ├── main
│   │   ├── java
│   │   │   └── com.example.digitalwallet
│   │   │       ├── config/             # Security & application configuration
│   │   │       ├── controller/         # REST Controllers
│   │   │       ├── service/            # Business logic
│   │   │       ├── repository/         # JPA repositories
│   │   │       ├── entity/             # Database entities
│   │   │       ├── dto/                # Request & Response DTOs
│   │   │       ├── security/           # JWT authentication & authorization
│   │   │       ├── exception/          # Global exception handling
│   │   │       ├── aop/                # Logging & performance aspects
│   │   │       ├── scheduler/          # Scheduled jobs
│   │   │       ├── health/             # Custom health indicators
│   │   │       └── DigitalWalletApplication.java
│   │   │
│   │   └── resources
│   │       ├── application.yml
│   │       └── static/
│   │
│   └── test
│       ├── controller/                 # Integration tests
│       └── service/                    # Unit tests
│
├── pom.xml
├── README.md
└── .gitignore
```

## Architecture

The project follows a layered architecture:

Controller
↓
Service
↓
Repository
↓
Database

Additional design principles:

* Constructor Injection
* DTO pattern for API requests/responses
* Separation of Entity and API models
* Global Exception Handling
* Validation framework

---

## Tech Stack

* Java 17
* Spring Boot 3.x
* Spring Security
* JWT Authentication
* Spring Data JPA
* Spring AOP
* Spring Cache
* Spring Scheduling
* Spring Async
* Spring Boot Actuator
* H2 Database
* Maven

---

## REST APIs

* Register User
* Login
* Get Wallet Details
* Update Wallet Owner
* Close Wallet
* Transfer Funds
* Get Transaction History
* Filter Transactions by Amount
* Filter Transactions by Date
* Health Check

---

## Database

Development Database:

* H2 In-Memory Database

H2 Console:

```
http://localhost:8080/h2-console
```

Connection Details:

```
JDBC URL: jdbc:h2:mem:walletdb
Username: sa
Password: (leave blank)
```

To seed a wallet balance for testing:

```sql
UPDATE wallets
SET balance = 10000
WHERE id = 1;
```

---

## Running the Project

Clone the repository:

```
git clone <repository-url>
```

Build the project:

```
mvn clean install
```

Run the application:

```
mvn spring-boot:run
```

Access:

* Application: `http://localhost:8080`
* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* H2 Console: `http://localhost:8080/h2-console`

---

## Project Highlights

* Production-ready architecture
* JWT Authentication & Authorization
* Role-based access control
* Secure fund transfer engine
* JPA entity relationships
* JPQL custom queries
* DTO-based API design
* Global exception handling
* Input validation
* AOP logging & performance monitoring
* Async background processing
* Caching support
* Scheduled maintenance tasks
* Spring Boot Actuator monitoring

---

## Future Enhancements

* Redis distributed caching
* Docker & Kubernetes deployment
* PostgreSQL/MySQL production profile
* Kafka/RabbitMQ event-driven notifications
* Email/SMS notifications
* Comprehensive unit and integration tests
* API rate limiting and audit logging

---

## Objective

Build a backend system that is:

* Scalable
* Secure
* Maintainable
* Observable
* Production-ready
