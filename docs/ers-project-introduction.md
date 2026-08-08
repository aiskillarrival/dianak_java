# Expense Reimbursement System (ERS)

## Introduction

This document specifies the Expense Reimbursement System (ERS), a full-stack web application for managing employee expense reimbursements. We will construct this system over four iterations.

Employees incur expenses on behalf of the company and submit reimbursement requests. Managers review pending requests and approve or deny them. The system records each request through its complete lifecycle.

## Core Concepts

- **Users & Roles** — The system defines two roles: *Employee* and *Manager*. Employees submit and view their own requests. Managers view and resolve requests for all employees.
- **Reimbursements** — A request consists of an amount, a description, a type (travel, food, lodging, other), and a status.
- **Status Lifecycle** — A reimbursement is created in the `PENDING` state and is subsequently resolved to `APPROVED` or `DENIED` by a manager. Resolved requests are immutable.

## Example Workflow

1. **Submission** — An employee logs in and submits a reimbursement request: $120, "client dinner", type `FOOD`. The request is created with status `PENDING` and the employee recorded as its author.
2. **Review** — A manager logs in and views the pending requests, including the new submission.
3. **Resolution** — The manager approves the request. Its status becomes `APPROVED`, the manager is recorded as the resolver, and the request becomes immutable.
4. **Confirmation** — The employee views their reimbursements and observes the request is now `APPROVED`, along with who resolved it.

## Domain Objects

Domain objects are the entities central to the problem domain — the "things" the system stores and manipulates. They form the basis of the data model and appear throughout every layer of the application.

The ERS defines three domain objects:

### User

Represents an employee or manager. Each user belongs to one department.

| Field | Description |
|-------|-------------|
| `id` | Unique identifier |
| `username` | Unique login name |
| `password` | Credential for authentication |
| `firstName` | Given name |
| `lastName` | Surname |
| `role` | `EMPLOYEE` or `MANAGER` |
| `department` | The department the user belongs to |

### Department

Represents an organizational unit. A department has zero or many users.

| Field | Description |
|-------|-------------|
| `id` | Unique identifier |
| `name` | Unique department name |

### Reimbursement

Represents a single reimbursement request.

| Field | Description |
|-------|-------------|
| `id` | Unique identifier |
| `amount` | Monetary amount requested |
| `description` | Explanation of the expense |
| `type` | `TRAVEL`, `FOOD`, `LODGING`, or `OTHER` |
| `status` | `PENDING`, `APPROVED`, or `DENIED` |
| `author` | The user who submitted the request |
| `resolver` | The manager who resolved the request (empty while pending) |

## Project Structure

We will build the ERS in four iterations. Each iteration builds upon the previous one: new technologies are introduced, layers are rewritten, and features are added to an existing codebase.

| Iteration | Focus |
|-----------|-------|
| 1 | Java backend, relational database, REST API (Javalin), minimal HTML/CSS/JS frontend |
| 2 | Migration of the API to Spring Boot |
| 3 | Frontend rewritten in Angular/TypeScript; complete, polished product |
| 4 | Extension of the existing codebase with new features; final presentations |

All components run locally. Detailed requirements for each iteration are specified in separate documents.