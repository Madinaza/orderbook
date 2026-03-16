# API Overview

## Base URL

When running through the gateway locally:

```text
http://localhost:8080
```

## Authentication

Protected endpoints require:

```http
Authorization: Bearer <jwt-token>
```

## Error Format

Errors follow a consistent structure similar to:

```json
{
  "timestamp": "2026-03-15T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/orders"
}
```

## Auth Endpoints

### Register
`POST /api/auth/register`

### Login
`POST /api/auth/login`

## Order Endpoints

### Place order
`POST /api/orders`

### Get my orders
`GET /api/orders`

Supported filters may include:
- instrument
- status
- side
- orderType
- page
- size
- sortBy
- sortDirection

### Get order by id
`GET /api/orders/{id}`

### Get order audit events
`GET /api/orders/{id}/events`

### Cancel order
`POST /api/orders/{id}/cancel`

### Replace order
`PUT /api/orders/{id}`

## Trade Endpoints

### Get my trades
`GET /api/trades/mine`

### Get public trades by instrument
`GET /api/trades/{instrument}`

## Market Data Endpoints

### Get order book
`GET /api/orderbook/{instrument}`

## Admin Endpoints

### Admin summary
`GET /api/admin/summary`

## System Endpoint

### System status
`GET /api/system/status`
