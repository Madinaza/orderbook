# Testing Documentation

## Testing Philosophy

The project uses layered testing to validate correctness at multiple levels:
- domain behavior
- service workflows
- web/API contracts
- persistence behavior
- frontend rendering and interaction
- end-to-end behavior scenarios

## Backend Test Categories

## Domain Unit Tests
Focus:
- matching engine behavior
- order book behavior
- smart order routing logic
- fee calculations

Examples:
- exact price cross
- no cross
- FIFO at same price
- multi-level fills
- market order with insufficient liquidity

## Service Tests
Focus:
- order command workflows
- order query behavior
- admin summary calculations
- business-rule enforcement

## Controller/Web Tests
Focus:
- secured endpoint behavior
- role-based access
- validation errors
- expected HTTP status codes

## Repository Integration Tests
Focus:
- JPA mapping correctness
- query correctness
- audit log retrieval
- trade query behavior

## BDD / Cucumber
Focus:
- realistic trading scenarios
- behavior-oriented acceptance validation

## Frontend Tests
Focus:
- page rendering
- protected route behavior
- polling and fallback UI
- component interactions

## Coverage
JaCoCo is used to generate backend coverage reports.

### Run tests
```bash
mvn clean test
```

### Generate coverage report
```bash
mvn clean test jacoco:report
```

Coverage output is typically generated under:
```text
target/site/jacoco/index.html
```

## Recommended Evidence for Submission
- successful build log
- test execution summary
- JaCoCo report
- selected screenshots of passing tests
