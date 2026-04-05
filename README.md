> This repository is intended for learning, experimentation, and reference purposes. It is not designed as a production-grade system.

# kotlin-reactive-learning-lab

A hands-on reference project demonstrating reactive REST APIs with Kotlin, Spring WebFlux, R2DBC, and WebClient — covering non-blocking programming patterns end-to-end.

---

## Overview

This project implements a simple Product catalog API using a fully reactive stack. Every layer — from the HTTP handler down to the database query — is non-blocking: no thread ever sits idle waiting for I/O.

**Why this matters in real-world systems:**  
Blocking I/O is one of the most common causes of poor throughput in Java/Kotlin services. A thread waiting for a database response cannot serve another request. At high concurrency, thread pools saturate and latency spikes. Reactive programming solves this by using a small number of event-loop threads that never block, allowing a service to handle thousands of concurrent requests with far fewer resources.

---

## Real-World Context

Reactive stacks are used in production where high concurrency and low latency matter:

- **E-commerce platforms** — product search APIs handling thousands of simultaneous queries
- **Financial services** — real-time pricing feeds that aggregate data from multiple sources
- **API gateways / BFF (Backend for Frontend)** — composing responses from multiple microservices into a single payload
- **Streaming pipelines** — processing continuous data feeds without large thread pools

This project demonstrates the **API Composition** pattern specifically: a single endpoint (`/products/{id}/stock`) merges data from two independent async sources (the database and a stock service) into one response.

---

## What This Repo Demonstrates

| Concept | Where |
|---------|-------|
| Non-blocking HTTP layer with Spring WebFlux | `ProductController` |
| Reactive database access via R2DBC | `ProductRepository`, `DatastoreConfig` |
| Reactive HTTP client (`WebClient`) | `StockService`, `WebClientConfiguration` |
| Composing two async sources with `Mono.zipWith` | `ProductController.findByIdWithStock` |
| API Composition / BFF pattern | `/products/{id}/stock` endpoint |
| Reactive error handling with `switchIfEmpty` | `ProductController.findById` |
| Testing WebFlux controllers with `@WebFluxTest` | `ProductControllerTest` |
| Testing R2DBC repositories with `@DataR2dbcTest` | `ProductRepositoryTest` |
| Verifying reactive streams with `StepVerifier` | `ProductRepositoryTest` |

---

## Architecture / Component Flow

```
HTTP Client
    │
    ▼
┌─────────────────────────────┐
│      ProductController       │  REST layer — maps HTTP requests to reactive operations
│  - GET /products             │
│  - GET /products/{id}        │
│  - GET /products/{id}/stock  │
└──────────┬──────────────────┘
           │                    │
           ▼                    ▼
┌──────────────────┐   ┌───────────────┐
│ ProductRepository │   │  StockService  │  Fetches stock qty via WebClient
│  (R2DBC + H2)    │   │  (WebClient)  │
└──────────────────┘   └───────┬───────┘
           │                    │
           │         ┌──────────▼──────────────────────┐
           │         │  GET /stock-service/product/{id}/│
           │         │  quantity  (mock endpoint,       │
           │         │  hosted in the same application) │
           │         └─────────────────────────────────┘
           ▼
     H2 In-Memory DB
```

**Request lifecycle for `GET /products/{id}/stock`:**

1. Request arrives at `ProductController.findByIdWithStock`.
2. Two `Mono` publishers are created — one for the DB query, one for the stock HTTP call.
3. `Mono.zipWith` subscribes to both **concurrently**.
4. When both emit, `ProductStockView.from(product, qty)` assembles the response.
5. Spring WebFlux serialises the result to JSON and writes it to the response — no thread was blocked at any point.

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Kotlin 1.6 | Primary language |
| Spring Boot 2.7 | Application framework |
| Spring WebFlux | Non-blocking HTTP server (Netty) |
| Project Reactor | Reactive streams (`Mono`, `Flux`) |
| Spring Data R2DBC | Reactive relational database access |
| H2 (in-memory) | Embedded database — no setup required |
| WebClient | Non-blocking HTTP client |
| JUnit 5 + StepVerifier | Testing |
| Gradle (Kotlin DSL) | Build tool |

---

## Project Structure

```
kotlin-reactive-learning-lab/
├── src/
│   ├── main/
│   │   ├── kotlin/org/demo/
│   │   │   ├── Application.kt                 # Entry point
│   │   │   ├── config/
│   │   │   │   ├── DatastoreConfig.kt          # Schema + seed-data initialisation
│   │   │   │   └── WebClientConfiguration.kt   # WebClient bean definition
│   │   │   ├── controller/
│   │   │   │   └── ProductController.kt        # REST endpoints + mock stock endpoint
│   │   │   ├── model/
│   │   │   │   ├── Product.kt                  # Domain model (R2DBC entity)
│   │   │   │   └── ProductStockView.kt         # Composite DTO (product + stock)
│   │   │   ├── repository/
│   │   │   │   └── ProductRepository.kt        # Reactive R2DBC repository
│   │   │   └── service/
│   │   │       └── StockService.kt             # WebClient wrapper for stock queries
│   │   └── resources/
│   │       ├── application.properties          # R2DBC connection + logging config
│   │       ├── schema.sql                      # DDL — creates the products table
│   │       └── data.sql                        # Seed data — 3 sample products
│   └── test/
│       └── kotlin/org/demo/
│           ├── controller/
│           │   └── ProductControllerTest.kt    # @WebFluxTest — controller unit tests
│           ├── model/
│           │   └── ProductStockViewTest.kt     # Plain JUnit 5 — DTO unit tests
│           └── repository/
│               └── ProductRepositoryTest.kt    # @DataR2dbcTest — repository tests
├── build.gradle.kts
├── settings.gradle
├── .gitignore
└── LICENSE
```

---

## How to Run Locally

**Prerequisites:** JDK 17+, no other setup needed (H2 database is embedded).

```bash
# Clone
git clone https://github.com/your-username/kotlin-reactive-learning-lab.git
cd kotlin-reactive-learning-lab

# Build and run
./gradlew bootRun
```

The application starts on `http://localhost:8080`.

> **Windows:** Use `gradlew.bat bootRun` instead of `./gradlew bootRun`.

### Build only (no run)

```bash
./gradlew build
```

---

## How to Run Tests

```bash
./gradlew test
```

Test results are written to `build/reports/tests/test/index.html`.

**Test coverage summary:**

| Test class | Type | What it tests |
|------------|------|---------------|
| `ProductControllerTest` | `@WebFluxTest` | HTTP endpoints, status codes, response bodies, error handling |
| `ProductRepositoryTest` | `@DataR2dbcTest` | R2DBC queries against a live H2 database |
| `ProductStockViewTest` | Unit | DTO factory method and data class equality |

---

## Example Usage

With the application running (`./gradlew bootRun`):

**List all products:**
```bash
curl http://localhost:8080/products
```
```json
[
  {"id":1,"name":"Wireless Headphones","price":79.99},
  {"id":2,"name":"Mechanical Keyboard","price":129.99},
  {"id":3,"name":"USB-C Hub","price":49.99}
]
```

**Get a single product:**
```bash
curl http://localhost:8080/products/1
```
```json
{"id":1,"name":"Wireless Headphones","price":79.99}
```

**Get a product with stock level (demonstrates reactive composition):**
```bash
curl http://localhost:8080/products/1/stock
```
```json
{"id":1,"name":"Wireless Headphones","price":79.99,"stockQuantity":10}
```

**Not found — demonstrates reactive error handling:**
```bash
curl -i http://localhost:8080/products/999
# HTTP/1.1 404 Not Found
```

---

## Learning Outcomes

After studying this project you should be able to:

1. Explain the difference between blocking and non-blocking I/O and why it matters.
2. Use `Mono` and `Flux` to represent 0-or-1 and 0-or-many async results.
3. Compose multiple async sources with `Mono.zipWith`.
4. Write reactive repository queries using Spring Data R2DBC.
5. Make non-blocking HTTP calls with `WebClient`.
6. Handle the "not found" case idiomatically with `switchIfEmpty`.
7. Test reactive controllers with `@WebFluxTest` and `WebTestClient`.
8. Verify reactive streams with `StepVerifier`.

---

## Limitations

This project is intentionally simplified for teaching purposes:

- **No authentication or authorisation** — all endpoints are publicly accessible.
- **In-memory H2 database** — data is lost on restart; not suitable for persistent storage.
- **Self-hosted mock stock service** — in a real system this would be a separate service. The mock always returns a fixed value of 10.
- **No pagination** — `GET /products` returns the full table; impractical for large datasets.
- **No input validation** — the `save` operation in the repository has no validation layer.
- **No production observability** — no metrics, tracing, or structured logging.
- **Fixed WebClient base URL** — hardcoded to `localhost:8080`; a real system would use service discovery or environment-specific configuration.

---

## Future Improvements

- Add `POST /products` to create new products, demonstrating reactive write paths.
- Introduce Spring Security for authentication.
- Replace the mock stock endpoint with a separate Spring Boot application to demonstrate true inter-service communication.
- Add Flyway for schema version management.
- Add Spring Boot Actuator for health checks and metrics.
- Containerise with Docker and add a `docker-compose.yml`.

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
