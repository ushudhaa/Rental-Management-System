# Rental Management System

A Spring Boot web application for managing rental properties and rent payments, with both a Thymeleaf UI and a REST API.

## Tech Stack
Java 17, Spring Boot 3.5, Spring MVC + Thymeleaf, Spring Data JPA, PostgreSQL, Bean Validation, springdoc-openapi (Swagger), Lombok, Maven

## Features
- **Properties** — CRUD, filter by status/city, pagination
- **Payments** — CRUD, mark-as-paid, filter by property/status, pagination
- Web UI at `/`, `/properties`, `/payments`
- REST API at `/api/v1/...`
- Global exception handling with consistent JSON error responses

## Project Structure

src/main/java/com/example/RentalManagementSystem
├── config/
├── controller/
├── dto/
├── entity/
├── enums/
├── exception/
├── repository/
├── service/
└── RentalManagementSystemApplication.java

src/main/resources
├── templates/
├── static/css/
└── application.properties


## Setup

1. Create the database:
```sql
CREATE DATABASE rental_management_db;
```

2. Configure `application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rental_management_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

3. Run:
```bash
mvn spring-boot:run
```

App runs at `http://localhost:8080`.

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/properties` | Create property |
| GET | `/api/v1/properties` | List properties (`?status=`, `?city=`, `?page=`) |
| GET | `/api/v1/properties/{id}` | Get property |
| PUT | `/api/v1/properties/{id}` | Update property |
| DELETE | `/api/v1/properties/{id}` | Delete property |
| POST | `/api/v1/payments` | Create payment |
| GET | `/api/v1/payments` | List payments (`?propertyId=`, `?status=`, `?page=`) |
| GET | `/api/v1/payments/{id}` | Get payment |
| PUT | `/api/v1/payments/{id}` | Update payment |
| PATCH | `/api/v1/payments/{id}/mark-paid` | Mark payment paid |
| DELETE | `/api/v1/payments/{id}` | Delete payment |

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Sample Requests

**Property:**
```json
{
  "title": "Cozy 2BHK Apartment",
  "address": "123 Main Street",
  "city": "Kathmandu",
  "type": "APARTMENT",
  "rentAmount": 25000,
  "bedrooms": 2,
  "bathrooms": 1
}
```

**Payment:**
```json
{
  "propertyId": 1,
  "tenantName": "Rajesh Shrestha",
  "amount": 25000,
  "dueDate": "2026-08-05",
  "paymentMethod": "BANK_TRANSFER"
}
```

## Author
Ushudha Sanwa Limbu — created during Java Backend Developer internship at Sitoula Tech Solution.
