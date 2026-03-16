# Architecture Documentation

## Purpose

Orderbook Platform is designed as a distributed trading simulation platform. The architecture separates identity, execution, projection, routing, and presentation concerns into dedicated services.

## Architecture Style

The platform follows a microservices architecture with a mix of:
- synchronous request/response communication
- asynchronous event-driven propagation
- real-time push delivery to clients

## Services

## auth-service
Responsible for:
- account registration
- login
- password hashing
- JWT issuance

It does not own trading workflows or market projections.

## trading-service
Responsible for:
- order placement
- order cancellation
- order replacement
- order retrieval
- trade retrieval for traders
- audit event recording
- matching engine execution
- publishing domain events

This service is the write-side source of truth for order and trade activity.

## market-data-service
Responsible for:
- consuming trading events
- projecting order-book state
- projecting trade history
- serving public market data queries
- broadcasting live updates through WebSocket/STOMP

This service acts as the read-side projection layer.

## api-gateway
Responsible for:
- single entry point for frontend requests
- request routing to downstream services
- simplifying frontend/backend interaction
- centralizing CORS and gateway concerns

## discovery-service
Responsible for:
- service registry support in development architecture
- dynamic service awareness where enabled

## frontend
Responsible for:
- authentication workflows
- market monitoring
- order management
- trade visibility
- admin analytics

## Data Ownership

Each service owns its own database:
- auth-service owns trader accounts
- trading-service owns orders, fills, and audit logs
- market-data-service owns market projection tables

No service writes directly into another service’s database.

## Event Flow

### Order lifecycle
1. trader places an order
2. trading-service validates and persists the order
3. matching engine attempts execution
4. fills are persisted
5. order change event is published
6. trade executed event is published
7. market-data-service consumes events
8. projection tables are updated
9. clients can query updated projections or receive WebSocket pushes

## Security Model

- JWT is issued by auth-service
- protected services validate JWT
- role claims determine access level
- admin routes require admin role
- frontend enforces client-side route guards in addition to backend enforcement

## Design Principles

### Service ownership
Each service owns a clear responsibility boundary.

### Event-driven read models
Trading remains the source of truth while market views are asynchronously derived.

### API consistency
Shared DTOs and API error structures reduce inconsistency across services.

### Operational simplicity
Docker Compose allows local reproduction of the full platform.

## Trade-offs

### Strengths
- clear separation of concerns
- realistic event-driven workflow
- strong demonstration of backend engineering concepts
- scalable design direction

### Constraints
- local-focused deployment defaults
- educational simplifications in matching and infrastructure
- limited observability compared to production-grade systems
