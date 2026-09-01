# Order Service

The service manages the order lifecycle within the E-Commerce Microservices architecture. It provides authenticated order creation, paginated order history, order status tracking, and payment status management. The service coordinates with the Cart and Payment Services through service discovery and load-balanced communication while maintaining the order and payment state in its own database.

> **Note:** This service is part of a larger E-Commerce Microservices project.

---

## Highlights

* **Authenticated order management** — Order operations require an authenticated user, with user identity obtained through the User & Auth Service
* **User and delivery snapshot** — Customer information such as user ID, email, phone number, and delivery address is captured as part of the order at creation time
* **Checkout orchestration** — Receives order creation requests from the Cart Service and coordinates payment initiation through the Payment Service
* **Payment integration** — Communicates with the Payment Service to initiate payment and receives the generated payment link for the checkout flow
* **Payment-driven order lifecycle** — Payment Service reports the final payment result back to the Order Service, which updates payment details and transitions the order status accordingly
* **Paginated order history** — Users can retrieve their previous orders through a paginated response rather than loading their complete order history at once
* **Order status tracking** — Provides a dedicated endpoint to retrieve the current status of an individual order
* **Service-to-service communication** — Communication with User/Auth, Cart, and Payment Services uses Eureka-based service discovery and load-balanced RestTemplate
* **Global exception handling** — Handles order-specific and downstream service communication failures through a centralized exception handler
* **Clean layered architecture** — Controller, service, repository, security, and domain responsibilities are separated following clean code and Low-Level Design principles

---

## Architecture Overview

```text
                              Cart Service
                                   │
                                   │ POST /order/new
                                   ▼
                            Order Service
                                   │
                                   ├───────────────┐
                                   │               │
                                   ▼               ▼
                            Order Database    Payment Service
                                                   │
                                                   │ Payment Link
                                                   ▼
                                            Order Service
                                                   │
                                                   ▼
                                              Cart Service
```

Authentication and user information flow:

```text
                         Order Service
                              │
                              ▼
                   Spring Security Filter Chain
                              │
                              ▼
                   LoadBalanced RestTemplate
                              │
                              ▼
                     User/Auth Service
                              │
                           /validate
                              │
                              ▼
                    User Information Headers
                    ├── X-User-Id
                    ├── X-User-Email
                    ├── X-User-Phone
                    └── ...
                              │
                              ▼
                       Order Service
                              │
                              ▼
                      Create User Snapshot
```

Payment status flow:

```text
Payment Service
      │
      │ Payment Result
      ▼
Order Service
      │
      ├── Update Payment Details
      │
      └── Update Order Status
```

---

## Authentication & Authorization

The Order Service requires authentication for customer-facing operations.

The authenticated user's information is obtained from the User & Auth Service through its `/validate` endpoint.

```text
Client
  │
  │ Access Token
  ▼
Order Service
  │
  ▼
Spring Security Filter Chain
  │
  ▼
LoadBalanced RestTemplate
  │
  ▼
User/Auth Service
  │
  ▼
/validate
  │
  ├── Validate Access Token
  ├── Check token validity
  └── Extract user information
  │
  ▼
User Information
  │
  ▼
Order Service
```

The Order Service uses the authenticated user information when creating and retrieving orders.

---

## User & Delivery Information

When an order is created, the Order Service obtains the relevant customer information and delivery address and stores them with the order.

```text
Order
 ├── Order ID
 ├── User
 │    ├── User ID
 │    ├── Email
 │    └── Phone
 │
 ├── Products
 ├── Delivery Details
 ├── Order Date
 ├── Payment Details
 └── Order Status
```

This allows the order to retain the customer and delivery information associated with the purchase instead of depending exclusively on the user's current profile.

For example, if a user changes their profile or address after placing an order, the existing order can continue to retain the information captured when that order was created.

---

## Order Creation

```text
POST /order/new
```

Order creation is initiated by the Cart Service during checkout.

```text
Cart Service
      │
      │ Create Order Request
      ▼
Order Service
      │
      ├── Validate authenticated user
      │
      ├── Create order
      │
      ├── Persist order details
      │
      ▼
Payment Service
      │
      ├── Create payment
      └── Generate payment link
      │
      ▼
Order Service
      │
      │ Payment Link
      ▼
Cart Service
      │
      ▼
Return checkout information to user
```

The Order Service therefore acts as the coordination point between the Cart and Payment Services during checkout.

---

## Payment Lifecycle

The Order Service initially creates the order while payment is still in progress.

```text
Order Created
      │
      ▼
Payment In Progress
      │
      ▼
Payment Service
      │
      ├── Payment Successful
      │
      └── Payment Failed
```

Once the Payment Service has processed the payment, it communicates the result back to the Order Service.

```text
Payment Service
      │
      │ PUT /order/payment-status
      ▼
Order Service
      │
      ├── Update payment details
      │
      └── Update order status
```

The payment result is handled regardless of whether the payment succeeds or fails.

The Order Service owns the resulting order-state transition rather than allowing the Payment Service to directly modify the Order entity.

---

## Order History

```text
GET /order/history
```

Authenticated users can retrieve their previous orders.

The service uses the authenticated user's identity to query their orders and returns the results using pagination.

```text
Authenticated User
        │
        ▼
SecurityContext / User Identity
        │
        ▼
Order Repository
        │
        ▼
Find Orders for User
        │
        ▼
Paginated Response
```

This prevents users from retrieving another user's order history by supplying a different user ID.

---

## Order Status

```text
GET /order/status/{orderId}
```

Returns the current status of a particular order.

The status is maintained by the Order Service and can change as the order progresses through its lifecycle.

```text
Order
  │
  ▼
Current Order Status
```

Payment-related events can cause the Order Service to transition the order from its initial payment state into the appropriate subsequent state.

---

## API Endpoints

The service uses `/order` as its context path.

| Method | Endpoint                  | Auth Required | Description                                                                 |
| ------ | ------------------------- | ------------- | --------------------------------------------------------------------------- |
| `POST` | `/order/new`              | Access Token  | Create a new order and initiate payment                                     |
| `GET`  | `/order/history`          | Access Token  | Retrieve paginated order history for the authenticated user                 |
| `GET`  | `/order/status/{orderId}` | Access Token  | Retrieve the current status of an order                                     |
| `PUT`  | `/order/payment-status`   | No            | Receive payment result from Payment Service and update payment/order status |

> **Note:** `/order/payment-status` is currently configured with `permitAll()` because it is used for internal service-to-service communication from the Payment Service.

---

## Service-to-Service Communication

The Order Service communicates with other microservices using Eureka service discovery and load-balanced RestTemplate.

### Cart → Order

The Cart Service initiates order creation during checkout.

```text
Cart Service
     │
     ▼
LoadBalanced RestTemplate
     │
     ▼
Eureka
     │
     ▼
Order Service
```

### Order → Payment

The Order Service initiates payment through the Payment Service.

```text
Order Service
     │
     ▼
LoadBalanced RestTemplate
     │
     ▼
Eureka
     │
     ▼
Payment Service
     │
     ▼
Payment Link
```

### Payment → Order

After processing the payment, the Payment Service reports the payment result back to the Order Service.

```text
Payment Service
     │
     ▼
LoadBalanced RestTemplate
     │
     ▼
Eureka
     │
     ▼
Order Service
     │
     ▼
/payment-status
```

---

## Request Flows

### Create Order

```text
POST /order/new
        │
        ▼
Spring Security Filter Chain
        │
        ▼
Validate Access Token
        │
        ▼
User/Auth Service /validate
        │
        ▼
User Information + Delivery Details
        │
        ▼
Create Order
        │
        ├── Persist Order
        │
        ▼
Call Payment Service
        │
        ▼
Receive Payment Link
        │
        ▼
Return Payment Link
```

### Payment Status Update

```text
Payment Service
        │
        │ Payment Result
        ▼
PUT /order/payment-status
        │
        ▼
Update Payment Details
        │
        ▼
Update Order Status
```

### Order History

```text
GET /order/history
        │
        ▼
Spring Security Filter Chain
        │
        ▼
Validate Access Token
        │
        ▼
User/Auth Service /validate
        │
        ▼
Authenticated User ID
        │
        ▼
Find User's Orders
        │
        ▼
Paginated Response
```

---

## Exception Handling

The service uses centralized exception handling through a global exception handler.

Examples include:

* Order not found
* Invalid order-related requests
* Downstream service communication failures
* HTTP server errors from service-to-service calls

Instead of exposing raw exceptions directly to clients, exceptions are handled centrally and converted into appropriate API responses.

```text
Service / Repository / RestTemplate
              │
              ▼
        Exception Raised
              │
              ▼
    Global Exception Handler
              │
              ▼
     Structured Error Response
```

---

## Design Decisions

### Why store customer information with the order?

-> An order should retain the relevant customer and delivery information associated with the purchase. Storing this information with the order prevents historical orders from depending entirely on the user's current profile, which may change after the order is placed.

### Why authenticate orders using the User/Auth Service?

-> Authentication is centralized in the User & Auth Service. The Order Service delegates access-token validation rather than duplicating JWT validation and token management logic.

### Why use the authenticated user identity for order history?

-> Order history is private user-specific data. The service derives the user identity from the authenticated security context rather than accepting a user ID from the client, preventing users from requesting another user's orders by changing an identifier in the request.

### Why does Order communicate with Payment instead of Cart communicating directly with Payment?

-> Cart is responsible for managing the shopping cart, while Order owns the order lifecycle and Payment owns payment processing. Having Order initiate the payment keeps these responsibilities separated:

```text
Cart
 │
 ▼
Order
 │
 ▼
Payment
```

### Why does Payment report the result back to Order?

-> Payment Service owns payment processing, but Order Service owns the order lifecycle. Payment therefore reports the payment result, while Order decides how that result affects the order and updates its own state accordingly.

### Why is `/payment-status` currently public?

-> The endpoint is currently used for internal service-to-service communication from the Payment Service and is configured with `permitAll()`. This is a deliberate current-state tradeoff rather than a claim that the endpoint is externally secure.

A future implementation can introduce service-to-service authentication so that only trusted Payment Service requests are accepted.

### Why use synchronous service-to-service communication?

-> The current implementation uses load-balanced RestTemplate and Eureka to keep the checkout flow synchronous and straightforward. Kafka-based asynchronous communication can be introduced later for payment and order lifecycle events where eventual consistency is acceptable.

---

## Tech Stack

| Layer                 | Technology                 |
| --------------------- | -------------------------- |
| Framework             | Spring Boot                |
| Security              | Spring Security            |
| Database              | MySQL                      |
| ORM                   | Spring Data JPA            |
| Service Discovery     | Netflix Eureka             |
| Service Communication | Load-balanced RestTemplate |
| Build Tool            | Maven                      |
| Language              | Java                       |

---

## Environment Variables

Sensitive database and service configuration should be externalized through environment variables rather than hardcoded.

| Variable              | Description         |
| --------------------- | ------------------- |
| `DATASOURCE_URL`      | JDBC connection URL |
| `DATASOURCE_USERNAME` | Database username   |
| `DATASOURCE_PASSWORD` | Database password   |
| `EUREKA_SERVER_URL`   | Eureka Server URL   |

---
<!--
## Getting Started

```bash
# Clone the repository
git clone https://github.com/your-username/order-service.git

# Navigate to the project
cd order-service

# Configure environment variables

# Run the service
./mvnw spring-boot:run
```

The Order Service will register itself with the Eureka Service Discovery Server and communicate with the User & Auth, Cart, and Payment Services through service discovery.

---
-->
## Known Gaps & Roadmap

* Secure `/payment-status` with service-to-service authentication like a custom header created by payment just for communication between these services
* Kafka-based asynchronous communication for payment/order events
* Delivery address modification within defined business rules
* Customer-care based delivery address modification
* Order cancellation
* Order return/refund workflow
* Distributed tracing and observability
* Docker containerization
