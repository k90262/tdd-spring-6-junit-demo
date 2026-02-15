# Demo Project

> Reference: Pluralsight course: [TDD using Spring 6 and JUnit](https://app.pluralsight.com/ilx/video-courses/tdd-spring-6-junit/course-overview)

Junie AI Help: Based on the analysis of the `ticket-api` project, here is a component diagram illustrating the architecture and interactions between the different layers of the application.

### Clean Architecture Diagram (Full Flow)

```mermaid
flowchart TD
    %% External Actor
    Client([External Client])

    subgraph FrameworksLayer [Frameworks & Drivers Layer]
        direction TB
        Spring[Spring Boot Framework]
        WebREST["REST API (HTTP/JSON)"]
        DB[(H2 Database)]
        JPA[Spring Data JPA]
    end

    subgraph InterfaceAdapters [Interface Adapters Layer]
        direction TB
        Controller[TicketController]
        ExceptionHandler[ControllerExceptionHandler]
        DTOs[TicketDto / Records]
        CustomRepoImpl[TicketFilterRepositoryImpl]
    end

    subgraph UseCases [Use Cases Layer]
        direction TB
        ServiceI[TicketService Interface]
        ServiceImpl[TicketServiceImpl]
        
        RepoPorts["Repository Ports:
        - TicketRepository
        - AgentRepository"]
    end

    subgraph Entities [Entities Layer]
        direction TB
        TicketEntity[Ticket Entity]
        AgentEntity[Agent Entity]
    end

    %% Flow and Dependencies
    
    %% 1. The Trigger
    Client -->|REST Call / JSON| WebREST
    WebREST --> Controller

    %% 2. Dependency Rule (Inward)
    Controller --> ServiceI
    ServiceImpl --Implements--> ServiceI
    ServiceImpl --> RepoPorts
    
    %% 3. Inversion of Control for DB
    CustomRepoImpl --Implements--> RepoPorts
    JPA --Implements--> RepoPorts
    
    %% 4. Core Entities
    ServiceImpl --> TicketEntity
    RepoPorts --> TicketEntity

    %% 5. Infrastructure detail
    CustomRepoImpl --> DB
    JPA --> DB
    
    %% Styling for clarity
    style Client fill:#f9f,stroke:#333
    style Entities fill:#fff4dd,stroke:#d4a017
    style UseCases fill:#e1f5fe,stroke:#01579b
```

---

### Understanding the REST Call in Clean Architecture

In this model, the **REST Call** is handled as follows:

1.  **Entry Point**: The **External Client** (like a Browser or Postman) sends an HTTP request.
2.  **The Adapter**: The **Framework Layer** (Spring Web) receives the raw bytes and hands them to the **`TicketController`** (Interface Adapter). The Controller's job is to "adapt" the web world into the application world.
3.  **Inward Bound**: The Controller converts the incoming JSON into a **DTO** and calls the **`TicketService`** (Use Case).
4.  **The Boundary**: Notice that the Controller doesn't call the implementation (`TicketServiceImpl`) directly; it calls the **Interface** (`TicketService`). This is the boundary between the "How" (Web) and the "What" (Business Logic).
5.  **Data Transformation**: When the service is done, it returns data. The Controller then uses the **`TicketDto`** to format that data back into JSON for the client.

### Summary of Component Responsibilities
*   **External Client**: Initiates the request (The Driver).
*   **Frameworks (WebREST)**: Handles the technical details of HTTP/TCP.
*   **Adapters (Controller)**: Marshals data between the Web and Use Cases.
*   **Use Cases (ServiceImpl)**: Executes application-specific logic.
*   **Entities**: Holds the fundamental business rules.

This diagram now shows the complete lifecycle of a request while maintaining strict Clean Architecture boundaries.
