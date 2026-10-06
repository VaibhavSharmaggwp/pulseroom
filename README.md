# PulseRoom 🚀

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)

> **Production-grade real-time collaborative workspace supporting live concurrent notes, presence tracking, and high-frequency whiteboard streaming. Built with Spring Boot WebSockets, featuring monotonic server sequencing, event idempotency, PostgreSQL snapshot recovery, and distributed horizontal scale-out powered by Redis Pub/Sub and Kafka.**

---

## 🏗️ System Architecture

PulseRoom is designed to scale from a single-node Spring Boot service to a multi-instance distributed cluster:

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

---

## ✨ Features

- **Real-Time Collaboration**: Full-duplex persistent WebSocket connections (`ws://localhost:8080/ws`).
- **Concurrent Notes**: Live participant presence, typing indicators, and real-time state synchronization.
- **Collaborative Whiteboard**: High-frequency streaming of vector stroke coordinates (`DRAW_START`, `DRAW_POINTS`, `DRAW_END`).
- **Distributed Concurrency Primitives**:
  - **Server-Side Sequencing (`serverSeq`)**: Monotonically increasing sequence numbers per room to detect dropped messages and ensure deterministic event ordering.
  - **Event Idempotency (`eventId`)**: Globally unique IDs preventing duplicate mutation replay across reconnects.
  - **Snapshot Recovery**: Automatic client reconnection restoring the latest durable state from PostgreSQL.
- **Horizontal Scalability**: Decoupled JVM instances bridged via Redis Pub/Sub for cross-server message fan-out.

---

## 📡 WebSocket Protocol & Event Schema

### Event Envelope Contract
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

### Supported Event Types
| Event Type | Direction | Description |
| :--- | :--- | :--- |
| `ROOM_STATE` | Server -> Client | Initial room snapshot and version on join |
| `USER_JOINED` / `USER_LEFT` | Server -> Client | Real-time presence notifications |
| `TYPING_START` / `TYPING_STOP`| Client -> Server -> Client | Live typing indicators |
| `TEXT_UPDATE` | Client -> Server -> Client | Collaborative notes delta updates |
| `DRAW_START` / `DRAW_POINTS` / `DRAW_END` | Client -> Server -> Client | High-frequency vector whiteboard strokes |
| `ERROR` | Server -> Client | Protocol and validation error responses |

---

## 📂 Repository Structure

```text
pulseroom/
├── backend/            # Spring Boot WebSocket, Redis & Kafka backend
│   ├── src/            # Java application source and resources
│   ├── docker-compose.yml
│   ├── mvnw / mvnw.cmd
│   └── pom.xml
└── ui/                 # PulseRoom Android client (Jetpack Compose)
    ├── app/            # Android UI application source (Compose UI, ViewModels, WebSocket)
    ├── gradlew / gradlew.bat
    └── build.gradle.kts
```

---

## 🛠️ Tech Stack

- **Backend**: Java 21, Spring Boot (Web, WebSocket, Data JPA)
- **Client (Android)**: Kotlin, Jetpack Compose, Material 3, OkHttp WebSocket, StateFlow & Coroutines
- **Database**: PostgreSQL (Room metadata, user identity, document snapshots)
- **Caching & Pub/Sub**: Redis 7
- **Event Streaming**: Apache Kafka (KRaft mode)
- **Containerization**: Docker Compose

---

## 🚀 Getting Started

### 1. Prerequisites
- Java 21+ installed
- PostgreSQL installed and running locally
- Docker (for optional Redis & Kafka services)

### 2. Environment Setup
Navigate to the backend directory and copy the example configuration:
```bash
cd backend
cp .env.example .env
```
Ensure your database named `room` exists:
```sql
CREATE DATABASE room;
```

Configure your credentials in `backend/src/main/resources/application.properties` or set them via environment variables:
```bash
export DB_URL="jdbc:postgresql://localhost:5432/room"
export DB_USERNAME="postgres"
export DB_PASSWORD="your_database_password"
```

### 3. Spin Up Infrastructure (Redis & Kafka)
```bash
cd backend
docker compose up -d
```

### 4. Run the Backend
```bash
cd backend
./mvnw spring-boot:run
```
The server will boot on `http://localhost:8080`.

### 5. Run the Android Client
Navigate to the UI directory and build/install the debug application:
```bash
cd ui
./gradlew installDebug
```
Alternatively, open the `ui` module in Android Studio and run it on an emulator or connected device. For Android emulators, point the client to `ws://10.0.2.2:8080/ws`.

---

## 🧪 Testing with Postman

1. Open **Postman** -> Click **New** -> **WebSocket**.
2. Enter the URL:
   ```text
   ws://localhost:8080/ws
   ```
3. Click **Connect**.
4. Set the message format to **JSON** and send:
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
5. Check your Spring Boot terminal console for confirmation of the parsed event.

---

## 🗺️ Roadmap & Upcoming Milestones

- [x] Full-duplex WebSocket communication backbone (`ws://localhost:8080/ws`)
- [x] Redis Pub/Sub cross-instance event synchronization
- [x] Monotonic server sequencing & event idempotency
- [x] PostgreSQL durable state snapshots and room recovery
- [x] Android client application with Jetpack Compose & live WebSocket integration
- [ ] Operational Transformation (OT) / CRDT support for rich-text conflicts
- [ ] End-to-end room encryption for private workspaces
- [ ] Whiteboard canvas export (PNG/SVG) and replay timeline

---

## 📄 License
This project is open source and available under the [MIT License](LICENSE).
