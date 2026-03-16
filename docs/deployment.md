# Deployment Documentation

## Local Deployment

The platform is designed to run locally using Docker Compose.

## Services
- PostgreSQL for auth-service
- PostgreSQL for trading-service
- PostgreSQL for market-data-service
- Zookeeper
- Kafka
- Kafka UI
- discovery-service
- auth-service
- trading-service
- market-data-service
- api-gateway

## Start stack
```bash
docker compose up -d --build
```

## Stop stack
```bash
docker compose down
```

## Useful Health Checks
- gateway: `http://localhost:8080/actuator/health`
- auth: `http://localhost:8081/actuator/health`
- trading: `http://localhost:8082/actuator/health`
- market-data: `http://localhost:8083/actuator/health`
- discovery: `http://localhost:8761`

## Production-Oriented Next Steps
A production-grade deployment would typically add:
- managed PostgreSQL
- managed Kafka
- secret manager
- centralized logging
- metrics and alerting
- container registry
- CI/CD pipeline
- reverse proxy or ingress controller
- TLS termination
- Kubernetes or ECS deployment
