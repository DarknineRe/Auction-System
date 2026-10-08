# 7. Component Diagram & Deployment Diagram

## 7.1 Component Diagram

แสดงองค์ประกอบภายใน Spring Boot application ตาม Layered Architecture และสิ่งที่เชื่อมต่อภายนอก (ลูกศรทึบ = เรียกใช้ / ขึ้นกับ)

```mermaid
flowchart LR
    subgraph CLIENT["Client"]
        Browser["Web Browser<br/>Thymeleaf pages<br/>(home, login, register,<br/>profile, bidding-detail)"]
        ApiClient["API Client<br/>Swagger UI / Bruno / Postman"]
    end

    subgraph APP["Auction System (Spring Boot 4 / Java)"]
        direction TB

        subgraph CROSS["Cross-cutting"]
            Sec["Security<br/>SecurityConfig, BCrypt,<br/>UserDetailsService,<br/>AuthEntryPoint / AccessDeniedHandler"]
            Exc["GlobalExceptionHandler<br/>ErrorResponse"]
            Oas["OpenApiConfig<br/>springdoc Swagger UI"]
            Init["AdminInitializer"]
        end

        subgraph PRES["Presentation Layer"]
            Web["PageController<br/>MVC + Thymeleaf views"]
            Rest["REST Controllers<br/>controller/api/*<br/>User, SellerProfile, Artwork,<br/>Bidding, BidAction, Comment,<br/>Payment, Admin*"]
        end

        subgraph DTOL["DTO Layer"]
            Dto["dto/request, dto/response"]
            Map["Mappers<br/>mapper/*"]
        end

        subgraph SVC["Service Layer"]
            Svc["Service interfaces +<br/>implementation/*Impl<br/>(@Transactional)"]
            State["State components<br/>BiddingState, PaymentState,<br/>*StateResolver"]
        end

        subgraph SCH["Scheduler"]
            BSch["BiddingExpiryScheduler"]
            PSch["PaymentExpiryScheduler"]
        end

        subgraph REPO["Repository Layer"]
            Repo["Spring Data JPA<br/>repository/*"]
        end

        subgraph DOM["Domain Layer"]
            Ent["Entities and Enums<br/>model/*"]
        end
    end

    DB[("PostgreSQL<br/>Neon (production)<br/>postgres:16 (docker-compose)")]

    Browser -->|"HTTP (HTML)"| Sec
    ApiClient -->|"HTTP REST JSON"| Sec
    Sec --> Web
    Sec --> Rest
    Sec --> Oas
    Web --> Svc
    Rest --> Dto
    Rest --> Map
    Rest --> Svc
    Map --> Dto
    Svc --> State
    Svc --> Repo
    BSch --> Svc
    PSch --> Svc
    Init --> Svc
    Repo --> Ent
    Repo -->|"JDBC / Hibernate"| DB
    Exc -. "แปลง exception จาก" .-> Rest
```

**การตรวจกฎ Layered:** ไม่มีเส้น `Presentation → Repository` ตรง ๆ — Controller ทุกตัวต้องผ่าน Service (ตามข้อกำหนดข้อ 3)

| Component | หน้าที่ | ตำแหน่งหลัก |
|---|---|---|
| Security | ยืนยันตัวตน (form login + HTTP Basic + remember-me), กำหนดสิทธิ์ตาม role, ตอบ 401/403 เป็น JSON สำหรับ `/api/**` | `config/SecurityConfig` ฯลฯ |
| REST Controllers | รับ/ตอบ JSON ที่ `/api/v1/**`, ตรวจ `@Valid` | `controller/api/*` |
| PageController | คืนหน้า Thymeleaf | `controller/PageController` |
| Service Layer | กฎธุรกิจ + transaction | `service/*`, `service/implementation/*` |
| State components | กฎการเปลี่ยนสถานะ Bidding/Payment | `service/state/*` |
| Schedulers | ปิดประมูลและทำให้ payment หมดอายุทุก 60 วินาที | `scheduler/*` |
| Repository | เข้าถึงข้อมูลด้วย Spring Data JPA (มี pessimistic lock `findByIdForUpdate`) | `repository/*` |
| OpenAPI | Swagger UI ที่ `/swagger-ui.html` | `config/OpenApiConfig` |

## 7.2 Deployment Diagram

### (ก) Production — ตามที่ระบุใน README (Render Web Service + Neon PostgreSQL)

```mermaid
flowchart LR
    subgraph USERDEV["User Device"]
        Br["Web Browser"]
    end

    subgraph RENDER["Render — Web Service (Docker)"]
        subgraph CONT["Container: eclipse-temurin:26-jre"]
            Jar["app.jar<br/>Spring Boot (port 8080)"]
        end
    end

    subgraph NEON["Neon — Cloud PostgreSQL"]
        Pg[("auction_system<br/>PostgreSQL")]
    end

    GH["GitHub Repository<br/>(main branch)"]

    Br -->|"HTTPS :443"| Jar
    Jar -->|"JDBC over TLS :5432<br/>DB_URL / DB_USERNAME / DB_PASSWORD"| Pg
    GH -->|"build from code/Dockerfile"| RENDER
```

Environment variables ที่แอปต้องใช้ (จาก `application.properties`): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `REMEMBER_ME_KEY`, `PORT`, และ (ไม่บังคับ) `ADMIN_EMAIL`, `ADMIN_PASSWORD`

### (ข) Local / ทดสอบ — `docker-compose.yml`

```mermaid
flowchart LR
    Dev["Developer Browser<br/>localhost:8080"]

    subgraph HOST["Docker Host (docker compose)"]
        subgraph APPC["Container: app (build ./code/Dockerfile)"]
            J["Spring Boot app.jar<br/>:8080"]
        end
        subgraph DBC["Container: database (postgres:16-alpine)"]
            P[("PostgreSQL<br/>db: auction")]
        end
        Vol[/"Volume: postgres_data"/]
    end

    Dev -->|"HTTP :8080 (APP_PORT)"| J
    J -->|"JDBC :5432<br/>jdbc:postgresql://database:5432/auction"| P
    P --- Vol
```

| Node | รายละเอียด | ที่มา |
|---|---|---|
| `app` | build จาก `code/Dockerfile` — multi-stage: `eclipse-temurin:26-jdk` build ด้วย Maven Wrapper → `eclipse-temurin:26-jre` รัน `app.jar` | `code/Dockerfile` |
| `database` | `postgres:16-alpine`, healthcheck `pg_isready`, `app` รอจน healthy (`depends_on: service_healthy`) | `docker-compose.yml` |
| Volume | `postgres_data` เก็บข้อมูลถาวร | `docker-compose.yml` |

> ก่อนส่งงาน: ใส่ Deployment URL จริงใน README และตรวจว่าชื่อบริการ (Render / Neon) ตรงกับที่ deploy จริง — แผนภาพ (ก) เขียนตามหัวข้อ Tech Stack ใน README ปัจจุบัน
