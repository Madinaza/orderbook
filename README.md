# Orderbook Platform

A microservices-based trading platform that supports user authentication, order lifecycle management, trade execution, market-data projection, and a React-based trading interface.

This project demonstrates a realistic distributed-system design with separated service ownership, JWT-based security, event-driven market-data updates through Kafka, real-time client updates through WebSocket/STOMP, and containerized local deployment with Docker Compose.

---

## Table of Contents

- [Overview](#overview)
- [Business Scope](#business-scope)
- [Architecture](#architecture)
- [Services](#services)
- [Core Workflows](#core-workflows)
- [Technology Stack](#technology-stack)
- [Repository Structure](#repository-structure)
- [Running the Project](#running-the-project)
- [Configuration](#configuration)
- [API Overview](#api-overview)
- [Frontend Overview](#frontend-overview)
- [Data Model Summary](#data-model-summary)
- [Testing Strategy](#testing-strategy)
- [Engineering Decisions](#engineering-decisions)
- [Current Limitations](#current-limitations)
- [Future Improvements](#future-improvements)

---

## Overview

Orderbook Platform simulates a simplified electronic trading environment.

Authenticated users can:

- register and log in
- place buy and sell orders
- view and manage their own orders
- replace or cancel eligible orders
- inspect order audit history
- view their own trade history

The platform also provides:

- public market-data views by instrument
- order book snapshots
- recent trade history
- real-time updates through WebSocket/STOMP
- administrative visibility for aggregate trading activity

The system is intentionally designed as a multi-service application to demonstrate service boundaries, event-driven communication, and realistic backend/frontend integration.

---

## Business Scope

The platform models a simplified order-driven trading workflow.

### Supported trading actions

- place **LIMIT** orders
- place **MARKET** orders
- cancel eligible live orders
- replace eligible live LIMIT orders
- retrieve personal orders with filtering and pagination
- retrieve personal trades with filtering and pagination

### Market visibility

- query order book by instrument
- query recent trades by instrument
- receive live market updates through WebSocket
- fallback to polling when live socket connectivity is not available

### Administrative visibility

- total orders and trades
- open, filled, and cancelled order counts
- active instrument overview
- buy/sell ratio
- filled-rate and cancel-rate metrics
- top active instruments by open quantity
- top traded instruments by execution count

---

## Architecture

The platform follows a microservices architecture with a mix of synchronous and asynchronous communication.

### Communication patterns

- **HTTP via API Gateway** for user-facing request/response workflows
- **Kafka events** for propagation from trading-service to market-data-service
- **WebSocket/STOMP** for live market updates to the frontend

### Data ownership

Each backend service owns its own data and schema:

- `auth-service` owns user accounts
- `trading-service` owns orders, fills, and audit logs
- `market-data-service` owns market-data projection tables

This separation keeps write-side behavior and read-side projections clearly distinct.

---

## Services

## 1. auth-service

Responsible for:

- registration
- login
- password hashing
- JWT issuance

Key characteristics:

- uses Spring Security
- hashes passwords with BCrypt
- issues JWTs containing trader identity and roles

---

## 2. trading-service

Responsible for:

- order placement
- order cancellation
- order replacement
- order retrieval
- order audit history
- trade retrieval for authenticated users
- matching engine execution
- administrative summary endpoints
- publishing integration events

This is the write-side source of truth for trading activity.

---

## 3. market-data-service

Responsible for:

- consuming trading events
- maintaining order-book projection tables
- maintaining trade-history projection tables
- exposing public market-data endpoints
- broadcasting market updates through WebSocket/STOMP

This is the read-side projection layer.

---

## 4. api-gateway

Responsible for:

- exposing a single entry point for frontend traffic
- routing requests to downstream services
- centralizing gateway-level request flow
- supporting frontend/backend decoupling

The frontend is configured to call the gateway at:

```text
http://localhost:8080
```

---

## 5. discovery-service

Responsible for:

- Eureka service registry support

This service exists to reflect a more realistic microservices environment, even though some routing is also statically configured for local Docker usage.

---

## 6. frontend

Responsible for:

- authentication workflows
- route protection
- order entry
- order management UI
- market dashboard
- trade history UI
- admin dashboard
- live WebSocket market subscriptions
- polling fallback behavior

---

## Core Workflows

## Authentication flow

1. user registers or logs in through the frontend
2. request is routed to `auth-service`
3. credentials are validated
4. JWT is issued
5. frontend stores the token
6. protected requests include `Authorization: Bearer <token>`

---

## Order placement flow

1. trader submits an order from the frontend
2. request passes through `api-gateway`
3. `trading-service` validates and persists the order
4. matching engine attempts execution against resting liquidity
5. fills are persisted if matches occur
6. audit events are recorded
7. order and trade events are published to Kafka
8. `market-data-service` consumes those events
9. projection tables are updated
10. WebSocket subscribers receive updated market views

---

## Order management flow

A trader can:

- view their orders
- filter by instrument, status, side, and order type
- paginate through results
- cancel eligible orders
- replace eligible LIMIT orders
- open an order-details modal to inspect audit events

---

## Market-data flow

1. `market-data-service` consumes:
    - `order.changed`
    - `trade.executed`
2. it updates:
    - `order_book_row`
    - `trade_history`
3. the frontend retrieves snapshots through REST endpoints
4. live subscribers receive updates through WebSocket topics

---

## Technology Stack

## Backend

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- Spring Cloud Gateway
- Spring Cloud Netflix Eureka
- Spring Kafka
- Flyway
- PostgreSQL
- H2 for test profile
- JJWT

## Frontend

- React
- Vite
- React Router
- Recharts
- STOMP client

## Infrastructure

- Docker
- Docker Compose
- Kafka
- Zookeeper

## Testing

- JUnit 5
- Mockito
- Spring Boot Test
- MockMvc
- Vitest
- Testing Library
- Cucumber
- JaCoCo

---

## Repository Structure

```text
orderbook-platform/
├── frontend/
│   ├── src/
│   ├── Dockerfile
│   └── package.json
├── services/
│   ├── shared-contracts/
│   ├── auth-service/
│   ├── trading-service/
│   ├── market-data-service/
│   ├── api-gateway/
│   └── discovery-service/
├── docker-compose.yml
└── pom.xml
```

### Module purpose

- `shared-contracts`: shared DTOs, exceptions, and Kafka event contracts
- `auth-service`: identity and token issuance
- `trading-service`: write-side order lifecycle and execution
- `market-data-service`: read-side projections and live broadcasts
- `api-gateway`: entry point and routing
- `discovery-service`: service discovery support
- `frontend`: React UI

---

## Running the Project

## Prerequisites

- Java 17
- Maven 3.9+
- Node.js 20+
- Docker Desktop
- Git

---

## Option A: Run the full stack with Docker Compose

This is the recommended local setup.

### Build and start all services

```bash
docker compose up -d --build
```

### Stop the stack

```bash
docker compose down
```

### Check running containers

```bash
docker compose ps
```

### View service logs

```bash
docker compose logs -f
```

---

## Option B: Run backend locally from Maven / IDE

From the repository root:

```bash
mvn clean test
mvn clean package
```

Then run each Spring Boot service separately from your IDE or with Maven commands for each module.

---

## Run frontend locally

```bash
cd frontend
npm install
npm run dev
```

Vite may automatically choose a different port if `5173` is already in use.  
For example, it may run on `5174`, `5175`, or `5176`.

---

## Default local endpoints

### Frontend
Dynamic Vite dev port, commonly:

```text
http://localhost:5173
```

or the next available port if already in use.

### Backend services

- API Gateway: `http://localhost:8080`
- Auth Service: `http://localhost:8081`
- Trading Service: `http://localhost:8082`
- Market Data Service: `http://localhost:8083`
- Discovery Service: `http://localhost:8761`
- Kafka UI: `http://localhost:8088`

---

## Configuration

## Databases

Each service uses its own database:

- `authdb`
- `tradingdb`
- `marketdatadb`

In Docker Compose these are backed by separate PostgreSQL containers.

---

## JWT

The platform uses a shared JWT secret between services that issue and validate tokens.

Relevant configuration exists under:

```yaml
app:
  security:
    jwt:
      secret: ...
```

---

## Kafka topics

The trading-service publishes the following integration events:

- `order.changed`
- `trade.executed`

The market-data-service consumes these topics to update its projections.

---

## Seeding behavior

Some seeders are implemented behind profile-based configuration.  
If you want seeded demo accounts or sample market data, ensure the relevant Spring profile and settings are enabled explicitly.

Do not assume demo users exist unless you have enabled the correct profile/configuration.

---

## API Overview

The main frontend-facing base URL is:

```text
http://localhost:8080
```

---

## Auth endpoints

### Register
```http
POST /api/auth/register
```

### Login
```http
POST /api/auth/login
```

---

## Order endpoints

### Place order
```http
POST /api/orders
```

### Get my orders
```http
GET /api/orders
```

Supported query parameters include:

- `instrument`
- `status`
- `side`
- `orderType`
- `page`
- `size`
- `sortBy`
- `sortDirection`

### Get order by id
```http
GET /api/orders/{id}
```

### Get audit events for an order
```http
GET /api/orders/{id}/events
```

### Cancel order
```http
POST /api/orders/{id}/cancel
```

### Replace order
```http
PUT /api/orders/{id}
```

---

## Trade endpoints

### Get authenticated trader's trades
```http
GET /api/trades/mine
```

### Get market trades by instrument
```http
GET /api/trades/{instrument}
```

---

## Market-data endpoints

### Get order book by instrument
```http
GET /api/orderbook/{instrument}
```

---

## Admin endpoints

### Get admin dashboard summary
```http
GET /api/admin/summary
```

### Simulate best venue routing
```http
POST /api/admin/routing/best-venue
```

---

## System endpoint

### Get platform status
```http
GET /api/system/status
```

---

## Frontend Overview

The frontend provides the following main routes:

- `/` home / overview
- `/market` market dashboard
- `/login` login page
- `/register` registration page
- `/orders` trader order management
- `/my-trades` personal trade history
- `/place-order` order entry page
- `/admin` admin dashboard

### Frontend behavior highlights

- route guards for protected pages
- admin-only navigation and route access
- token expiration checks
- logout handling
- polling-based refresh for several views
- WebSocket market subscriptions with polling fallback
- CSV export for personal trade history
- modal-based replace and audit-detail interactions

---

## Data Model Summary

## trading-service

### Main tables

- `limit_order`
- `trade_fill`
- `order_audit_log`

### Key behaviors

- `limit_order` stores the current state of an order
- `trade_fill` stores executions
- `order_audit_log` stores lifecycle messages for traceability

---

## market-data-service

### Main tables

- `order_book_row`
- `trade_history`

### Key behaviors

- `order_book_row` stores current book projection rows keyed by order ID
- `trade_history` stores append-only projected trade history
- `event_id` is used as an idempotency guard for trades

---

## auth-service

### Main table

- `trader_account`

### Key behaviors

- stores username, password hash, role, and creation timestamp

---

## Testing Strategy

The project includes multiple layers of testing.

## Backend tests

### Unit tests
Focused on:

- matching engine behavior
- smart order router behavior
- fee calculations
- service-level business rules

### Web/controller tests
Focused on:

- endpoint security
- access rules
- request validation
- response contract expectations

### Repository integration tests
Focused on:

- JPA mappings
- repository query behavior
- H2-backed integration checks

### BDD tests
Cucumber scenarios are used for behavior-oriented coverage of realistic workflows.

---

## Frontend tests

Frontend tests cover:

- component rendering
- page behavior
- route-related behavior
- market-dashboard and status interactions

---

## Running tests

### Backend
From the repository root:

```bash
mvn clean test
```

### Coverage report
If JaCoCo is configured in the module build:

```bash
mvn clean test jacoco:report
```

Typical report path:

```text
services/trading-service/target/site/jacoco/index.html
```

### Frontend
```bash
cd frontend
npm test
```

or according to your package scripts.

---

## Engineering Decisions

## Why microservices

The system is intentionally decomposed to model a more realistic backend design:

- auth concerns are isolated
- trading logic owns the write-side source of truth
- market-data concerns are separated into projection/read models
- frontend sees a single entry point through the gateway

This improves conceptual clarity and mirrors real-world service ownership patterns.

---

## Why Kafka

Kafka is used to decouple the trading write model from market-data projections.

This provides:

- separation between execution logic and read models
- asynchronous propagation of domain changes
- a cleaner event-driven design for public market views

---

## Why a separate market-data service

Trading writes and market visibility serve different responsibilities.

Keeping projections in a separate service supports:

- specialized read models
- simpler public queries
- better separation of domain ownership
- a more scalable architecture direction

---

## Why WebSocket plus polling fallback

Real-time WebSocket delivery improves the market experience, but polling fallback helps preserve usability when the socket is reconnecting or unavailable.

This is especially useful in local development environments where live connectivity may restart during service changes.

---

## Why shared-contracts

The shared-contracts module reduces duplication by centralizing:

- common API structures
- shared exceptions
- messaging event contracts

This improves consistency across services.

---

## Current Limitations

This project is strong as an educational, demonstration, and portfolio system, but it is not intended to represent a production brokerage platform.

Current limitations include:

- no refresh-token flow
- no full production-grade secret management
- no distributed tracing
- no Prometheus/Grafana observability stack
- no external exchange connectivity
- no advanced pre-trade risk engine
- no rate limiting at gateway level
- no production container orchestration
- local/dev-focused infrastructure assumptions

There are also some intentional simplifications in the trading model compared with real exchange systems.

---

## Future Improvements

The following would be strong next steps for production hardening or portfolio enhancement:

### Security
- refresh tokens
- stronger session management
- secret vault integration
- audit enrichment

### Reliability
- retry and dead-letter handling for Kafka consumers
- better health and readiness checks
- circuit-breaking for cross-service calls

### Observability
- structured JSON logging
- Prometheus metrics
- Grafana dashboards
- distributed tracing

### Deployment
- CI/CD pipeline
- container registry publishing
- Kubernetes or ECS deployment
- cloud-managed PostgreSQL and Kafka

### Product features
- better admin controls
- richer market simulation
- order validation improvements
- instrument master/reference data
- risk controls and trading limits

---

## Professional Positioning

This project is suitable as:

- a portfolio project demonstrating backend engineering depth
- a distributed systems coursework submission
- a showcase of microservices, security, event-driven design, and React integration
- a strong foundation for additional testing, documentation, and cloud deployment work

---

## Author

Madina Aghazada

---

## Final Note

This repository focuses on service separation, correctness of core workflows, realistic technical choices, and clean full-stack integration. It is intentionally scoped to demonstrate engineering quality rather than imitate every production concern of a real financial exchange.
