# PulseRoom 🚀

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

> **PulseRoom** is a production-grade, distributed real-time collaborative workspace designed for high-concurrency environments. It powers live multi-user document editing, participant presence tracking, and high-frequency vector whiteboard streaming. Engineered on Spring Boot WebSockets with distributed horizontal scaling via Redis Pub/Sub and Apache Kafka, PulseRoom ensures deterministic event ordering, strict idempotency, and durable snapshot recovery with PostgreSQL.

---

## 📑 Table of Contents

- [Overview](#pulseroom-)
- [Key Features](#-key-features)
- [System Architecture](#-system-architecture)
- [WebSocket Protocol & Event Schema](#-websocket-protocol--event-schema)
- [Tech Stack](#-tech-stack)
- [Repository Structure](#-repository-structure)
- [Getting Started](#-getting-started)
  - [Prerequisites](#1-prerequisites)
  - [Environment Configuration](#2-environment-setup)
  - [Infrastructure Setup](#3-spin-up-infrastructure-redis--kafka)
  - [Running Backend](#4-run-the-backend)
  - [Running Android Client](#5-run-the-android-client)
- [Testing with WebSocket Clients](#-testing-with-postman--websocket-client)
- [Roadmap & Milestones](#-roadmap--upcoming-milestones)
- [License](#-license)

---

## ✨ Key Features

- **⚡ Real-Time Full-Duplex Synchronization**
  - Persistent, low-latency WebSocket communication layer (`ws://localhost:8080/ws`).
  - Bidirectional event broadcasting supporting concurrent room members.

- **📝 Concurrent Document Collaboration**
  - Live synchronized document state across all connected participants.
  - Granular presence awareness and live typing indicator streams (`TYPING_START`, `TYPING_STOP`).

- **🎨 High-Frequency Whiteboard Streaming**
  - Sub-millisecond streaming of vector whiteboard coordinates (`DRAW_START`, `DRAW_POINTS`, `DRAW_END`).
  - Smooth stroke rendering and state distribution with minimal overhead.

- **🛡️ Distributed Reliability & Concurrency Primitives**
  - **Monotonic Server Sequencing (`serverSeq`)**: Strict, room-scoped sequence counter to detect network dropouts, out-of-order packets, and guarantee deterministic state transitions.
  - **Event Idempotency (`eventId`)**: UUID-based event tracking to prevent duplicate mutation replays during client reconnections or network retries.
  - **Durable Snapshot Recovery**: Automatic room state reconstruction directly from PostgreSQL on client reconnection or node failure.

- **🌐 Horizontal Scalability & Distributed Event Bus**
  - Multi-instance backend scaling with cross-node fan-out handled by Redis Pub/Sub.
  - Asynchronous event streaming and audit logging backed by Apache Kafka.

---

## 🏗️ System Architecture

PulseRoom is engineered to scale seamlessly from an individual development node to a distributed multi-instance production cluster:

```
                  ┌──────────────────────────────────────────────┐
                  │    Clients (Android Jetpack Compose & Web)   │
                  └──────────────────────┬───────────────────────┘
                                         │  WebSocket (WSS) / REST
                                         ▼
                     ┌─────────────────────────┐
                     │   Spring Boot Backend   │
                     │  (WebSocket + Handlers) │
                     └──────┬────────────┬─────┘
                            │            │
            ┌───────────────▼┐          ┌▼────────────────┐
            │ Redis Pub/Sub  │          │   PostgreSQL    │
            │ (Cross-node    │          │ (Durable State, │
            │  Event Bridge) │          │  Snapshots)     │
            └────────────────┘          └─────────────────┘
                            │
                            ▼
            ┌──────────────────────────────────┐
            │      Apache Kafka (Event Log)     │
            └──────────────────────────────────┘
```

### Architectural Breakdown
- **Client Tier**: Native Android application designed with Jetpack Compose, Material 3, and reactive unidirectional data flow (StateFlow & Kotlin Coroutines).
- **Application Tier**: Spring Boot 3 handling WebSocket protocol framing, session lifecycles, payload validation, and room sequencing.
- **Coordination Tier**: Redis Pub/Sub bridging WebSocket connections across separate application nodes for transparent cross-server event broadcast.
- **Event Streaming Tier**: Apache Kafka recording immutable event streams for analytics, auditing, and durable event logs.
- **Persistence Tier**: PostgreSQL maintaining durable room entities, user sessions, and authoritative snapshot checkpoints.

---

## 📡 WebSocket Protocol & Event Schema

### Event Envelope Contract
All WebSocket payloads adhere to a unified JSON event envelope:

```json
{
  "eventId": "evt_101",
  "type": "TEXT_UPDATE",
  "roomId": "room_alpha_99",
  "clientId": "client_usr_007",
  "clientSeq": 1,
  "serverSeq": 1058,
  "timestamp": 1726615600000,
  "payload": {
    "action": "insert",
    "text": "Hello, PulseRoom!"
  }
}
```

### Envelope Fields Breakdown
- `eventId`: **String** — Globally unique identifier generated by the client for idempotency verification.
- `type`: **String** — Discriminator indicating the specific domain action or event.
- `roomId`: **String** — Unique target workspace identifier.
- `clientId`: **String** — Unique identifier representing the initiating participant.
- `clientSeq`: **Number** — Incremental counter maintained by the client to track local mutations.
- `serverSeq`: **Number** — Monotonically increasing sequence assigned by the backend upon processing.
- `timestamp`: **Number** — Unix epoch timestamp (milliseconds) representing event creation time.
- `payload`: **Object** — Domain-specific data required to apply the update.

### Supported Event Types
| Event Type | Direction | Description |
| :--- | :--- | :--- |
| `ROOM_STATE` | Server -> Client | Authoritative initial room snapshot and version on join |
| `USER_JOINED` / `USER_LEFT` | Server -> Client | Real-time presence notifications |
| `TYPING_START` / `TYPING_STOP` | Client -> Server -> Client | Live typing indicators and user activity signals |
| `TEXT_UPDATE` | Client -> Server -> Client | Collaborative notes delta updates and patch synchronization |
| `DRAW_START` / `DRAW_POINTS` / `DRAW_END` | Client -> Server -> Client | High-frequency vector whiteboard strokes |
| `ERROR` | Server -> Client | Protocol, authentication, and validation error responses |

---

## 🛠️ Tech Stack

- **Backend Framework**: Java 21, Spring Boot 3 (Spring Web, Spring WebSocket, Spring Data JPA)
- **Mobile Client**: Kotlin 2.x, Jetpack Compose, Material Design 3, OkHttp WebSocket, Coroutines & StateFlow
- **Primary Database**: PostgreSQL (Relational schema, document snapshots, room persistence)
- **Cache & Pub/Sub Broker**: Redis 7 (Cross-node message propagation & active state caching)
- **Event Streaming Platform**: Apache Kafka (KRaft mode, decoupled event log)
- **DevOps & Containers**: Docker Compose, multi-stage build workflows

---

## 📂 Repository Structure

```text
pulseroom/
├── backend/            # Spring Boot WebSocket, Redis & Kafka backend service
│   ├── src/            # Java application source code and resources
│   ├── docker-compose.yml
│   ├── mvnw / mvnw.cmd
│   └── pom.xml
└── ui/                 # PulseRoom Android native client
    ├── app/            # Compose UI, ViewModels, Repository & WebSocket client
    ├── gradlew / gradlew.bat
    └── build.gradle.kts
```

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java**: OpenJDK 21 or higher installed
- **Database**: PostgreSQL 15+ running locally (or via Docker)
- **Containers**: Docker and Docker Compose installed (for Redis & Kafka)
- **Android SDK**: Android Studio Ladybug / Meerkat (API 34+) for client builds

### 2. Environment Setup
- Navigate to the backend directory and configure the environment:
  ```bash
  cd backend
  cp .env.example .env
  ```

- Ensure your PostgreSQL database named `room` is created:
  ```sql
  CREATE DATABASE room;
  ```

- Configure database connection credentials via `backend/src/main/resources/application.properties` or environment variables:
  ```bash
  export DB_URL="jdbc:postgresql://localhost:5432/room"
  export DB_USERNAME="postgres"
  export DB_PASSWORD="your_database_password"
  ```

### 3. Spin Up Infrastructure (Redis & Kafka)
- Launch the backing services in detached mode:
  ```bash
  cd backend
  docker compose up -d
  ```

### 4. Run the Backend
- Start the Spring Boot application using the bundled Maven wrapper:
  ```bash
  cd backend
  ./mvnw spring-boot:run
  ```
- The backend server will initialize on `http://localhost:8080` (WebSocket endpoint at `ws://localhost:8080/ws`).

### 5. Run the Android Client
- Build and install the debug APK on a connected device or running emulator:
  ```bash
  cd ui
  ./gradlew installDebug
  ```
- *Tip for Android Emulators*: The Android emulator accesses the host machine's localhost via `ws://10.0.2.2:8080/ws`.

---

## 🧪 Testing with Postman / WebSocket Client

- **Step 1**: Open **Postman** -> Click **New** -> Select **WebSocket**.
- **Step 2**: Connect to the WebSocket endpoint:
  ```text
  ws://localhost:8080/ws
  ```
- **Step 3**: Click **Connect** to establish the full-duplex session.
- **Step 4**: Set message payload format to **JSON** and send a test event:
  ```json
  {
    "eventId": "test-01",
    "type": "TEXT_UPDATE",
    "roomId": "room-101",
    "clientId": "user-a",
    "clientSeq": 1,
    "timestamp": 1726615600000,
    "payload": {
      "text": "Hello world!"
    }
  }
  ```
- **Step 5**: Observe the Spring Boot application logs for incoming deserialization, sequencing, and broadcast confirmation.

---

## 🗺️ Roadmap & Upcoming Milestones

- [x] **Full-Duplex WebSocket Backbone**: Low-latency communication via `ws://localhost:8080/ws`.
- [x] **Cross-Instance Event Sync**: Redis Pub/Sub event bridge for horizontal node scaling.
- [x] **Deterministic Sequencing & Idempotency**: Strict `serverSeq` assignment and `eventId` deduplication.
- [x] **Durable State Snapshots**: PostgreSQL snapshot checkpoints and automatic room state recovery.
- [x] **Android Client Integration**: Native Jetpack Compose UI with live collaborative canvas & notes.
- [ ] **Conflict Resolution Engine**: Operational Transformation (OT) / CRDT support for multi-user rich-text edits.
- [ ] **End-to-End Room Encryption**: Client-side encryption for private workspaces.
- [ ] **Canvas Export & Session Replay**: SVG/PNG snapshot exports and historical session playback.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
