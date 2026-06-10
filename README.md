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
- GET /api/dashboard — Returns unified student data: academic, financial, contract, registration info

### Contracts
- POST /api/contracts — Create contract with installments (requires authentication)
- GET /api/contracts/my-contracts — Get authenticated student's contracts

### Admin (requires `ROLE_ADMIN` in JWT)
- GET /api/admin/contracts — List all contracts (paginated)
- GET /api/admin/contracts/{id} — Get contract by ID
- GET /api/admin/contracts/student/{studentId} — Get contracts by student
- GET /api/admin/contracts/status/{status} — Filter contracts by status (PENDING, ACTIVE, COMPLETED, CANCELLED, OVERDUE)
- PATCH /api/admin/contracts/{id}/status — Update contract status
- DELETE /api/admin/contracts/{id} — Delete contract
- POST /api/admin/contracts/bulk/status — Bulk update contract statuses

- GET /api/admin/installments — List all installments (paginated)
- GET /api/admin/installments/contract/{contractId} — Get installments by contract
- PATCH /api/admin/installments/{id}/status — Update installment status
- PATCH /api/admin/installments/{id}/waive-penalty — Waive penalty for an installment

- GET /api/admin/penalties — List all penalty history (paginated)
- GET /api/admin/penalties/installment/{installmentId} — Get penalties by installment
- GET /api/admin/penalties/contract/{contractId} — Get penalties by contract

- GET /api/admin/students?keyword={search} — Search students by name or ID (paginated)
- GET /api/admin/students/{studentId}/summary — Get student financial summary across contracts

## Installment Rules by Semester

The system enforces semester-based installment deadlines based on the active term (format: `YYYY/S` where S is semester):

### Semester 1 (e.g., 2025/1)
- **Number of installments:** 2
- **Deadline 1:** October 31 (last day of October)
- **Deadline 2:** November 30 (last day of November)

### Semester 2 (e.g., 2025/2)
- **Number of installments:** 3
- **Deadline 1:** February 28/29 (last day of February)
- **Deadline 2:** March 31 (last day of March)
- **Deadline 3:** April 30 (last day of April)

**Validation rules:**
- Installment count must match semester requirements
- All deadline dates must be the last day of the expected month
- Total installment amounts must equal remaining balance
- Student must have paid at least 50% of total fees to be eligible

## How it works

1. Student logs in → AUCA auth → our JWT issued
2. Frontend calls GET /api/dashboard
3. Backend fetches: active term + registration + balance from AUCA
4. Backend calculates: paid amount, remaining, percentage, eligibility
5. Backend fetches: contract + installments from our PostgreSQL
6. Returns everything in one response

## Roles

- **STUDENT** — Default role from AUCA IMS. Can access student endpoints only.
- **ADMIN** — Must be granted by AUCA IMS. Can access `/api/admin/**` endpoints.

The JWT stores the role issued by AUCA. Backend does not override or change roles.

## Penalty Scheduler
Runs every day at midnight. Checks all PENDING installments with past deadlines and applies 5% penalty automatically.

## Database Tables (PostgreSQL)
- contracts
- contract_installments  
- penalty_history
