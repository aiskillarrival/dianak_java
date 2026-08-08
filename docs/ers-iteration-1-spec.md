# ERS — Iteration 1 Specification

## Overview

This document specifies Iteration 1 of the ERS. It defines the iteration's objectives, the functional and non-functional requirements, the user stories the application must satisfy, and the deliverables to be submitted.

Iteration 1 establishes the foundation of the system: domain objects modeled in Java, persistence in a relational database via JDBC, a REST API built with Javalin, and a minimal HTML/CSS/JavaScript frontend. All components run locally.

Later iterations will build upon this foundation: features will be added, existing features will be polished, and the minimal frontend will be replaced with a more usable one.

## Objectives

- Model the domain objects in Java
- Persist data in a relational database via JDBC
- Expose the application's functionality through a REST API (Javalin)
- Provide a minimal frontend sufficient to exercise the API

## Requirements

### Functional Requirements

1. Users can register an account (default role `EMPLOYEE`)
2. Every user has exactly one role: `EMPLOYEE` or `MANAGER`
3. Users can log in and log out
4. Employees can submit a reimbursement request (amount, description, type)
5. Employees can view their own reimbursements, filterable by status
6. Employees can edit a reimbursement while it is `PENDING`
7. Managers can view all reimbursements, filterable by status and department
8. Managers can approve or deny `PENDING` reimbursements; the resolver is recorded
9. Managers may also submit and manage their own reimbursements, as an employee does
10. Resolved reimbursements cannot be modified

### Non-Functional Requirements

1. Passwords are not stored in plaintext
2. Data persists in a relational database and survives application restart
3. The API follows REST conventions (proper methods, status codes, JSON)
4. Layered architecture: controller / service / repository (DAO)
5. Input validation with meaningful error responses
6. Unit tests for the service layer

### User Stories

- As a user, I can register an account
- As a user, I can log in and log out
- As an employee, I can submit a reimbursement request
- As an employee, I can view the status of my requests
- As an employee, I can edit a pending request
- As a manager, I can view pending requests
- As a manager, I can approve or deny requests
- As a manager, I can view a history of resolved requests
- As a manager, I can do everything an employee can do (submit and manage my own reimbursements)

## Deliverables

- Codebase in a GitHub repository
- Live demonstration of the application
- Entity-relationship diagram (ERD) of the database schema