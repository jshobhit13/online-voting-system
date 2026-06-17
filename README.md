# VoteSecure

A full-stack secure online voting platform. The backend is a Spring Boot REST API handling authentication, role-based access, and concurrent vote protection. The frontend is a Next.js 14 app using the App Router, with server-side rendering for election pages and API routes that proxy requests to the backend so JWT secrets never reach the browser.

> Tested at 100+ concurrent users with zero duplicate votes recorded.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Next.js 14 (App Router) |
| Frontend Language | TypeScript |
| Styling | Tailwind CSS |
| State Management | Zustand |
| Backend | Spring Boot 3.2.5 |
| Backend Language | Java 17 |
| Security | Spring Security + JJWT (HS256) |
| Database | MongoDB |
| Password Hashing | BCrypt |
| Build Tool (backend) | Maven |
| Testing | JUnit 5, Mockito |

---

## Features

- **Voter registration and login** with BCrypt password hashing
- **Stateless JWT authentication** — token stored in httpOnly cookie, never in localStorage
- **Role-based access control** — `ROLE_USER` and `ROLE_ADMIN` with protected Next.js routes
- **Server-side rendered election pages** — election list loads with data, no client-side spinner
- **Concurrent vote protection** via three-layer duplicate prevention
- **Election lifecycle management** — `UPCOMING → ACTIVE → CLOSED`
- **Admin dashboard** — create elections, manage candidates, update status, view results
- **Real-time vote confirmation** — optimistic UI update on successful vote submission

---

## Project Structure

```
votesecure/
├── backend/                        # Spring Boot application
│   ├── src/main/java/com/shobhit/voting_system/
│   │   ├── controller/             # AuthController, VoteController, ElectionController, AdminController
│   │   ├── service/                # AuthService, VoteService, ElectionService
│   │   ├── repository/             # UserRepository, VoteRepository, ElectionRepository
│   │   ├── model/                  # User, Election, Vote, Candidate
│   │   ├── dto/                    # Request/response DTOs
│   │   ├── security/               # JwtAuthFilter, JwtUtil, SecurityConfig
│   │   └── exception/              # GlobalExceptionHandler
│   └── pom.xml
│
└── frontend/                       # Next.js 14 application
    ├── app/
    │   ├── (auth)/
    │   │   ├── login/page.tsx
    │   │   └── register/page.tsx
    │   ├── (voter)/
    │   │   ├── elections/
    │   │   │   ├── page.tsx        # SSR — election list
    │   │   │   └── [id]/page.tsx   # SSR — election detail + vote form
    │   │   └── dashboard/page.tsx
    │   ├── admin/
    │   │   ├── elections/page.tsx
    │   │   └── elections/[id]/page.tsx
    │   └── api/                    # Next.js API routes (proxy to Spring Boot)
    │       ├── auth/[...path]/route.ts
    │       ├── elections/[...path]/route.ts
    │       └── vote/route.ts
    ├── components/
    │   ├── ElectionCard.tsx
    │   ├── VoteForm.tsx
    │   ├── CandidateList.tsx
    │   └── AdminElectionTable.tsx
    ├── lib/
    │   ├── api.ts                  # Fetch wrappers for API routes
    │   └── auth.ts                 # Token helpers, middleware utils
    ├── store/
    │   └── authStore.ts            # Zustand store for auth state
    ├── middleware.ts               # Route protection (redirects unauthenticated users)
    └── next.config.ts
```

---

## Architecture

```
Browser
  │
  ▼
Next.js Frontend (Port 3000)
  ├── Server Components (SSR)
  │     └── Fetch elections directly on server using httpOnly cookie
  ├── Client Components
  │     └── Vote form, admin dashboard interactions
  ├── API Routes (proxy layer)
  │     └── Forward requests to Spring Boot, attach JWT from cookie
  └── Middleware
        └── Redirect unauthenticated users, check role for /admin
  │
  │  (internal server-to-server call — JWT never exposed to browser)
  ▼
Spring Boot Backend (Port 8080)
  │
  ▼
JwtAuthFilter
  ├── Validates Bearer token
  ├── Sets SecurityContextHolder
  └── 401 if invalid
  │
  ▼
Controllers → Services → Repositories
  │
  ▼
MongoDB
  └── Unique compound index on (userId, electionId)
```

### Why API Routes as a proxy

The Next.js API routes (`/app/api/**`) sit between the browser and Spring Boot. When a user logs in, the backend returns a JWT. The API route stores it in an httpOnly cookie instead of returning it to the browser directly. This means JavaScript running in the browser can never read the token, which eliminates XSS-based token theft entirely. Every subsequent request from the browser hits the Next.js API route, which reads the cookie server-side and forwards the request to Spring Boot with the Authorization header attached.

---

## Concurrent Vote Protection

The core challenge in any voting system is preventing duplicate votes when multiple requests arrive simultaneously. VoteSecure uses three layers:

**Layer 1 — Application check**
`existsByUserIdAndElectionId()` rejects an already-voted user before any write operation.

**Layer 2 — Transactional boundary**
`@Transactional` wraps the existence check and the save together to give atomicity. Requires MongoDB replica set (see setup below).

**Layer 3 — Database constraint (the real guarantee)**
A unique compound index on `(userId, electionId)` at the MongoDB level. Even if two concurrent threads both pass layers 1 and 2 simultaneously, MongoDB guarantees exactly one write succeeds. The second throws `DuplicateKeyException`, which the service catches and returns as a clean `409 Conflict` response.

This is covered by the test `castVote_concurrentDuplicateRejectedByDb`.

---

## API Reference

### Auth (public)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a new voter |
| POST | `/auth/login` | Login and receive JWT |

### Elections (ROLE_USER)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/elections` | List all elections |
| GET | `/api/elections/{id}` | Get election details |
| GET | `/api/elections/{id}/results` | View results (CLOSED only) |

### Voting (ROLE_USER)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/vote` | Cast a vote |

### Admin (ROLE_ADMIN)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/admin/elections` | Create a new election |
| PATCH | `/admin/elections/{id}/status` | Update election status |
| POST | `/admin/elections/{id}/candidates` | Add a candidate |
| DELETE | `/admin/elections/{id}/candidates/{cid}` | Remove a candidate |

---

## Data Models

**User**
```json
{
  "id": "ObjectId",
  "email": "voter@example.com",
  "password": "<bcrypt-hash>",
  "role": "ROLE_USER",
  "createdAt": "2025-05-01T10:00:00Z"
}
```

**Election**
```json
{
  "id": "ObjectId",
  "title": "Student Council President 2025",
  "description": "Annual student body election",
  "status": "ACTIVE",
  "startTime": "2025-05-01T09:00:00Z",
  "endTime": "2025-05-07T18:00:00Z",
  "candidates": [
    { "id": "c1", "name": "Alice Sharma" },
    { "id": "c2", "name": "Rahul Mehta" }
  ]
}
```

**Vote**
```json
{
  "id": "ObjectId",
  "userId": "ObjectId",
  "electionId": "ObjectId",
  "candidateId": "c1",
  "castedAt": "2025-05-03T14:22:00Z"
}
```

Unique compound index: `{ userId: 1, electionId: 1 }`

---

## Setup

### Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 20+
- MongoDB (replica set required for `@Transactional` support)

### 1. Clone the repository

```bash
git clone https://github.com/jshobhit13/votesecure.git
cd votesecure
```

### 2. Start MongoDB as a replica set

```bash
mongod --replSet rs0 --bind_ip localhost
```

Then in `mongosh`:

```js
rs.initiate()
```

### 3. Configure and run the backend

Create `backend/src/main/resources/.env` or export these as environment variables:

```
MONGO_URI=mongodb://localhost:27017/votesecure
JWT_SECRET=your-minimum-256-bit-secret-here
JWT_EXPIRATION_MS=3600000
```

In `application.properties`:

```properties
spring.data.mongodb.uri=${MONGO_URI}
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS}
```

```bash
cd backend
mvn spring-boot:run
```

Backend runs at `http://localhost:8080`.

### 4. Configure and run the frontend

```bash
cd frontend
cp .env.example .env.local
```

`.env.local`:

```
BACKEND_URL=http://localhost:8080
COOKIE_SECRET=any-random-string-for-cookie-signing
```

```bash
npm install
npm run dev
```

Frontend runs at `http://localhost:3000`.

### 5. Run backend tests

```bash
cd backend
mvn test
```

---

## Key Frontend Pages

| Route | Type | Description |
|---|---|---|
| `/login` | Client | Login form, sets httpOnly cookie on success |
| `/register` | Client | Registration form |
| `/elections` | Server | SSR list of all elections with status badges |
| `/elections/[id]` | Server | Election detail with candidate list and vote form |
| `/dashboard` | Client | Voter's voting history |
| `/admin/elections` | Server | Admin table of all elections |
| `/admin/elections/[id]` | Client | Manage candidates, update status |

---

## Sample Requests (backend direct)

**Register**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email": "voter@example.com", "password": "SecurePass@123"}'
```

**Login**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "voter@example.com", "password": "SecurePass@123"}'
```

**Cast a vote**
```bash
curl -X POST http://localhost:8080/api/vote \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{"electionId": "<election-id>", "candidateId": "c1"}'
```

---

## Known Limitations

- No token revocation — JWT remains valid until expiry. A refresh token flow would address this.
- Election status transitions are manual via admin API. A `@Scheduled` job should automate `UPCOMING → ACTIVE → CLOSED` based on `startTime` and `endTime`.
- No rate limiting on auth endpoints. Bucket4j should be added before any public deployment.
- Frontend is not yet deployed. Backend is deployable to Railway or Render with environment variable configuration.

---

## Author

**Shobhit Jain**
B.Tech CSE, Galgotias University (2026)
[github.com/jshobhit13](https://github.com/jshobhit13)
