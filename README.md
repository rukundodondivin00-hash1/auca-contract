# AUCA Contract System — Backend

Student Financial Contract Management System integrated with AUCA IMS APIs.

## Tech Stack
- Java 17
- Spring Boot 3.2.5
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Maven

## Setup

### 1. Configure application.properties
```
spring.datasource.url=jdbc:postgresql://localhost:5432/final_year
spring.datasource.username=postgres
spring.datasource.password=postgres1234
auca.api.key=YOUR_API_KEY_HERE
```

### 2. Run
```bash
mvn spring-boot:run
```

### 3. Swagger UI
```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

### Auth
- POST /api/auth/login — Login with AUCA credentials, returns JWT token

### Dashboard
- GET /api/dashboard — Returns everything: student info, fees, balance, contract, installments

### Contracts
- POST /api/contracts — Create contract with installments
- GET /api/contracts/my-contracts — Get student's contracts

### Admin
- GET /api/admin/contracts — Get all contracts (finance officer)

## How it works

1. Student logs in → AUCA auth → our JWT issued
2. Frontend calls GET /api/dashboard
3. Backend fetches: active term + registration + balance from AUCA
4. Backend calculates: paid amount, remaining, percentage, eligibility
5. Backend fetches: contract + installments from our PostgreSQL
6. Returns everything in one response

## Penalty Scheduler
Runs every day at midnight. Checks all PENDING installments with past deadlines and applies 5% penalty automatically.

## Database Tables (PostgreSQL)
- contracts
- contract_installments  
- penalty_history
