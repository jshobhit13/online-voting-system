# 🗳️ Online Voting System

### Secure • Scalable • Role-Based Digital Voting Platform

A production-ready **Online Voting System** built using Spring Boot, JWT Authentication, and MySQL designed to ensure secure, transparent, and tamper-proof elections.

---

# 🚀 Overview

This system enables users to participate in digital elections with strict security controls while providing administrators with full management capabilities.

It follows **industry-grade backend architecture**, ensuring:

- Secure authentication
- Controlled access
- Data integrity
- Scalability for real-world usage

---

# ✨ Key Features

## 👤 User Module

- Secure Registration & Login
- JWT-based Authentication
- View Active Elections
- Cast Vote (strict one-vote policy)
- Real-time voting status
- Result visibility based on election rules

## 🛠️ Admin Module

- Admin Dashboard
- Create & Manage Elections
- Add / Update / Remove Candidates
- Monitor Voting Activity
- Publish Results
- User Management

---

# 🔐 Security Architecture

- 🔒 BCrypt Password Hashing
- 🔑 JWT Token-based Authentication
- 🛡️ Spring Security Integration
- 👥 Role-Based Access Control (ADMIN / USER)
- 🚫 Duplicate Vote Prevention
- 🧹 Input Validation & Sanitization
- 🔍 Secure API Access via Authorization Headers

---

# 🧑‍💻 Tech Stack

## Backend

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT (JSON Web Token)

## Database

- MySQL

## Tools & Environment

- Git & GitHub
- Postman (API Testing)
- IntelliJ IDEA / VS Code
- Maven

---

# 🏗️ System Architecture

```id="5j8m7q"
Client (Frontend / Postman)
        ↓
   REST API Layer
        ↓
Controller Layer
        ↓
Service Layer (Business Logic)
        ↓
Repository Layer (JPA)
        ↓
     MySQL Database
```

---

# 📂 Project Structure

```id="yq6v3k"
online-voting-system/
│
├── controller/        # REST Controllers
├── entity/            # JPA Entities
├── repository/        # Database Access Layer
├── service/           # Business Logic
├── security/          # JWT & Security Config
├── config/            # App Configurations
│
├── resources/
│   └── application.properties
│
└── VotingSystemApplication.java
```

---

# ⚙️ Setup & Execution

## 1️⃣ Clone Repository

```id="3y0mlj"
git clone https://github.com/jshobhit13/online-voting-system.git
cd online-voting-system
```

---

## 2️⃣ Database Configuration

Create a MySQL database:

```id="scjpj1"
CREATE DATABASE voting_db;
```

Update configuration in:
`src/main/resources/application.properties`

```id="q3r1ah"
spring.datasource.url=jdbc:mysql://localhost:3306/voting_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

---

## 3️⃣ Run Application

```id="w3wwri"
mvn spring-boot:run
```

Application will start at:

```id="pnk0gx"
http://localhost:8080
```

---

# 🔐 Authentication Flow

1. User registers with credentials
2. Password is encrypted using BCrypt
3. User logs in → JWT token is generated
4. Token must be included in every protected request:

```id="3lcbxb"
Authorization: Bearer <JWT_TOKEN>
```

5. Backend validates token before granting access

---

# 🌐 API Endpoints

## 🔑 Authentication

| Method | Endpoint       | Description       |
| ------ | -------------- | ----------------- |
| POST   | /auth/register | Register user     |
| POST   | /auth/login    | Authenticate user |

---

## 🗳️ Voting

| Method | Endpoint       | Description     |
| ------ | -------------- | --------------- |
| GET    | /api/elections | Fetch elections |
| POST   | /api/vote      | Cast vote       |
| GET    | /api/results   | View results    |

---

# 🧪 Sample Request

## Register

```id="l7qbnm"
{
  "email": "user@example.com",
  "password": "secure123"
}
```

---

# 🧠 Core Concepts Implemented

- Stateless Authentication (JWT)
- Role-Based Authorization
- Secure Password Handling
- RESTful API Design
- Layered Architecture (Controller → Service → Repository)
- Data Integrity Constraints

---

# 📊 Database Design

## Users

- id (Primary Key)
- email (Unique)
- password (Encrypted)
- role (ADMIN / USER)

## Elections

- id
- title
- status

## Candidates

- id
- name
- election_id

## Votes

- id
- user_id
- candidate_id

---

# 🚀 Scalability & Design Highlights

- Modular architecture for easy scaling
- Stateless backend (JWT-based)
- Easily integrable with React / Angular frontend
- Optimized for high concurrency voting scenarios

---

# 🔮 Future Enhancements

- Aadhaar / OTP Verification
- Blockchain-based Voting Ledger
- Live Election Dashboard
- AI-based Fraud Detection
- Docker & Cloud Deployment (AWS / Azure)

---

# 🤝 Contribution

Contributions are welcome. Please fork the repository and submit a pull request.

---

# 📜 License

MIT License

---

# 👨‍💻 Author

**Shobhit Jain**
