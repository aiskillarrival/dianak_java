# Expense Reimbursement System (ERS)

A full-stack Java web application for managing employee expense reimbursements. Employees submit reimbursement requests; managers review and approve or deny them.

**Iteration 1** — Java backend with Javalin REST API, H2 embedded database, and minimal HTML/CSS/JS frontend.

## Prerequisites

- **Java 17+** ([SDKMAN](https://sdkman.io/) recommended: `sdk install java 17.0.10-tem`)
- **Maven 3.8+** (`sdk install maven` or `brew install maven`)
- **Git**

## Quick Start

```bash
# 1. Clone the repository
git clone <repo-url>
cd dianak_java

# 2. Build the project (runs tests)
mvn clean package

# 3. Start the server
java -Djava.net.preferIPv4Stack=true -jar target/expense-reimbursement-system-1.0-SNAPSHOT.jar

# 4. Open in browser
# http://localhost:7000
```

> **Note:** The `-Djava.net.preferIPv4Stack=true` flag is required on some WSL/Linux systems to force IPv4 socket binding. Without it, Jetty may bind only to IPv6, causing connection refused errors.

## Project Structure

```
dianak_java/
├── pom.xml                          # Maven build configuration
├── src/
│   ├── main/
│   │   ├── java/com/ers/
│   │   │   ├── App.java             # Entry point, Javalin setup
│   │   │   ├── model/               # Domain objects & enums
│   │   │   ├── dao/                 # Data access (JDBC)
│   │   │   ├── service/             # Business logic
│   │   │   ├── controller/          # REST controllers
│   │   │   ├── dto/                 # Request/response DTOs
│   │   │   └── util/                # Database & password utilities
│   │   └── resources/
│   │       ├── schema.sql           # Database schema
│   │       └── public/              # Frontend (HTML/CSS/JS)
│   └── test/
│       └── java/com/ers/service/    # Unit tests
└── docs/                            # Project specifications
```

## Architecture

Layered architecture: **Controller → Service → DAO**

| Layer | Responsibility |
|-------|---------------|
| `controller` | REST endpoints, request parsing, HTTP responses |
| `service` | Business logic, validation |
| `dao` | JDBC persistence, SQL queries |

## API Endpoints

### Authentication
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/register` | Register new account (default: EMPLOYEE) |
| `POST` | `/api/login` | Login, returns Bearer token |
| `POST` | `/api/logout` | Invalidate session |
| `GET` | `/api/me` | Get current user |

### Departments
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/departments` | List all departments |

### Reimbursements
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/reimbursements` | Submit new reimbursement |
| `GET` | `/api/reimbursements` | List reimbursements (`?status=&departmentId=`) |
| `GET` | `/api/reimbursements/{id}` | Get reimbursement by ID |
| `PUT` | `/api/reimbursements/{id}` | Edit PENDING reimbursement (author only) |
| `PATCH` | `/api/reimbursements/{id}/resolve` | Approve/deny (manager only) |

All authenticated endpoints require `Authorization: Bearer <token>` header.

## Database

Uses **H2** embedded database with file-based persistence (`./data/ersdb.mv.db`). Data survives application restarts. Tables are auto-created on first startup.

Seeded departments: Engineering, Sales, Human Resources, Finance, Marketing.

## Running Tests

```bash
mvn test
```

17 unit tests cover the service layer (user registration, login, reimbursement submission, editing, and resolution).

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Runtime | Java 17+ |
| Web Framework | Javalin 6 |
| Database | H2 (embedded, file-based) |
| Database Access | JDBC |
| Password Hashing | BCrypt (favre) |
| JSON | Jackson + Gson |
| Testing | JUnit 5 + Mockito |
| Build | Maven |
