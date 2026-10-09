# Auction-System

ระบบประมูลสินค้าออนไลน์สำหรับลงประกาศและเข้าร่วมประมูลผลงาน
ผู้ใช้สามารถสมัครสมาชิก สร้างการประมูล เสนอราคา และจัดการการชำระเงินได้
ระบบพัฒนาด้วย Spring Boot และ PostgreSQL โดยแบ่งโครงสร้างตาม Layered Architecture
มีหน้าเว็บด้วย Thymeleaf พร้อม REST API ที่เปิดดูเอกสารผ่าน Swagger UI ได้

## สมาชิกกลุ่ม

| ลำดับ | ชื่อ-นามสกุล | รหัสนักศึกษา | Section | Branch | หน้าที่รับผิดชอบ |
|---:|---|---:|---:|---|---|
| 1 | ปุณยวีร์ แทนคำ | 673380282-8 | 01 | `poonywee_6733802828-01` | backend#1 |
| 2 | ปริญญ์นกร อยู่แท้กูล | 673380277-1 | 02 | `parinnakorn_6733802771_02` |  |
| 3 | พงศพัศ เลบ้านแท่น | 673380283-6 | 01 | `poonywee_6733802828_01` | README และ presentation |
| 4 | ชนิณทร์ ใจช่วง | 673380264-0 | 01 | `chanin_6733802640_01` | Reviewer, testing และ deployment |
| 5 | จิณณวัตร โพธิ์ศรีทอง | 673380263-2 | 01 | `jinnawat_6733801632_01` | Frontend |

## Tech Stack

- **Backend:** Spring Boot 4.1.1, Java 26, Spring MVC, Spring Security, Bean Validation
- **Build Tool:** Maven (Maven Wrapper)
- **Database:** Neon PostgreSQL
- **ORM:** Spring Data JPA (Hibernate)
- **API Documentation:** OpenAPI / Swagger UI (Springdoc)
- **Frontend:** Thymeleaf
- **Deployment:** Render Web Service (Hybrid)
- **Containerization:** Docker

## System Architecture

- **Presentation Layer:** Controller / RestController / Views
- **Service Layer:** Business Logic & Transactions
- **Repository Layer:** Data Access Layer (Spring Data JPA)
- **Domain / Entity:** Entities, Value Objects, Enums & DTOs

## Database Design (ER Diagram)

ดู ER Diagram ได้ที่ [doc/diagrams/06-er-diagram.md](doc/diagrams/06-er-diagram.md)

## Installation & Setup
1. **Clone repository:**
   ```bash
   git clone https://github.com/DarknineRe/Auction-System.git
   cd Auction-System/code
   ```

2. **เตรียมฐานข้อมูล PostgreSQL:**
   สร้างฐานข้อมูลชื่อ `auction_system` บนเครื่อง local หรือใช้บริการคลาวด์ เช่น Neon:
   ```sql
   CREATE DATABASE auction_system;
   ```

3. **กำหนดค่า Environment Variables:**
   ตั้งค่าการเชื่อมต่อฐานข้อมูลและคีย์ความปลอดภัยก่อนรันระบบ:

   **Linux / macOS (Bash / Zsh):**
   ```bash
   export DB_URL="jdbc:postgresql://localhost:5432/auction_system"
   export DB_USERNAME="postgres"
   export DB_PASSWORD="your_password"
   export REMEMBER_ME_KEY="supersecretremembermekey12345"

   # (Optional) บัญชีผู้ดูแลระบบเริ่มต้น
   export ADMIN_EMAIL="admin@example.com"
   export ADMIN_PASSWORD="adminpassword123"
   ```

   **Windows (PowerShell):**
   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/auction_system"
   $env:DB_USERNAME="postgres"
   $env:DB_PASSWORD="your_password"
   $env:REMEMBER_ME_KEY="supersecretremembermekey12345"
   $env:ADMIN_EMAIL="admin@example.com"
   $env:ADMIN_PASSWORD="adminpassword123"
   ```

## How to Run

###  Docker

วิธีนี้เป็นวิธีที่ง่ายที่สุด ไม่ต้องติดตั้ง Java หรือ PostgreSQL บนเครื่อง

1. **รันระบบด้วย Docker Compose:**
   เปิด Terminal ในโฟลเดอร์ root ของโปรเจกต์ (ที่มีไฟล์ `docker-compose.yml`) แล้วรันคำสั่ง:
   ```bash
   docker compose up -d --build
   ```
   *(หมายเหตุ: คำสั่งนี้จะทำการ build image ของ Spring Boot และสร้าง container ของ PostgreSQL ให้ทำงานร่วมกันโดยอัตโนมัติ)*

2. **หยุดการทำงานของ Docker:**
   ```bash
   docker compose down
   ```

### Local

1. **Quick Start (one  line):**

   เข้าไปที่โฟลเดอร์ `code` แล้ว copy คำสั่งด้านล่างไปวางใน Terminal ได้เลย (ระบบจะเซ็ตตัวแปรที่จำเป็นและสร้าง Admin อัตโนมัติ):

   **Linux / macOS:**
   ```bash
   DB_URL=jdbc:postgresql://localhost:5432/auction_system DB_USERNAME=postgres DB_PASSWORD= REMEMBER_ME_KEY=devkey123 ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD=admin12345 ./mvnw spring-boot:run
   ```

   **Windows (PowerShell):**
   ```powershell
   $env:DB_URL="jdbc:postgresql://localhost:5432/auction_system"; $env:DB_USERNAME="postgres"; $env:DB_PASSWORD=""; $env:REMEMBER_ME_KEY="devkey123"; $env:ADMIN_EMAIL="admin@example.com"; $env:ADMIN_PASSWORD="admin12345"; .\mvnw.cmd spring-boot:run
   ```

### เข้าใช้งานผ่าน Browser (Thymeleaf Web UI)

   เมื่อระบบเริ่มทำงานเรียบร้อยแล้ว สามารถเปิดเบราว์เซอร์เพื่อเข้าใช้งานหน้าเว็บต่าง ๆ ได้ดังนี้:

| Service | URL |
|---|---|
| Thymeleaf Web UI | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |

### บัญชีทดสอบ

| Gmail | Password | Role |
|---|---|---|
| verity@gmail.com | 67ihatemylife | SUPER ADMIN |


## API Documentation

> 📖 **[ดูรายละเอียด REST API Specification ฉบับเต็มได้ที่นี่ (doc/api-spec.md)](doc/api-spec.md)**

| Service | URL |
|---|---|
| Swagger UI (local) | http://localhost:8080/swagger-ui.html |
| Swagger UI (production) | https://auction-system-68vk.onrender.com/swagger-ui/index.html |
| REST API | อยู่ใน Swagger UI ทุก endpoint ขึ้นต้นด้วย /api/v1 |

## How to Run Tests

The project uses Maven Wrapper, JUnit 5, Mockito, Spring Boot Test, and MockMvc. The current suite has 17 automated tests covering application startup, selected user/authentication and authorization flows, CSRF enforcement, artwork and seller-profile APIs, bidding and comments, bidding rules, web registration auto-login, and public Thymeleaf pages. This is not exhaustive coverage of every endpoint. The application uses PostgreSQL and Flyway, so local tests need a separate disposable test database. Do not use the production Neon database for tests.

### Prerequisites and test database

- Java 26
- PostgreSQL running on `localhost:5432`
- A local test database named `auction_test`

If the test role and database do not already exist, create them in PostgreSQL (for example, using `psql` or pgAdmin):

```sql
CREATE ROLE auction WITH LOGIN PASSWORD 'auction';
CREATE DATABASE auction_test OWNER auction;
```

The test database must be empty or disposable: Flyway applies the project's database migrations when the Spring application context starts.

### Run all tests on Windows (PowerShell)

From the repository root, run:

```powershell
Set-Location .\code
$env:DB_URL="jdbc:postgresql://localhost:5432/auction_test"
$env:DB_USERNAME="auction"
$env:DB_PASSWORD="auction"
$env:REMEMBER_ME_KEY="ci-only-remember-me-key"
.\mvnw.cmd --batch-mode verify
```

`verify` builds the application and runs all automated tests. To run just the tests, use `.\mvnw.cmd test`.

CSRF protection is enabled for state-changing API requests. For an API client, first make a GET request to `/register` while retaining the session cookie, read the hidden `_csrf` form value, and send it in the `X-CSRF-TOKEN` header with that cookie on POST, PUT, PATCH, and DELETE requests. Browser forms include the token automatically.

The current automated tests include:

| Test class | What it checks |
|---|---|
| `ProjectApplicationTests` | The Spring application context starts successfully with the test database. |
| `BiddingServiceImplTest` | Bidding increment, first bid, auction owner, and inactive-auction rules using Mockito. |
| `UserApiIntegrationTest` | Registration, validation, duplicate email, authentication, profile update/password change, admin access denial, and CSRF rejection without a token. |
| `ArtworkApiIntegrationTest` | Seller profile setup, artwork creation/list/read/update/delete, validation, and ownership authorization. |
| `SellerprofileApiIntegrationTest` | Seller profile read/update, unauthenticated access, and public profile bank-account privacy. |
| `BiddingApiIntegrationTest` | Bidding creation/list/read, bid rules/highest bid, authorization, and comment create/react/update/delete. |
| `PageControllerIntegrationTest` | Thymeleaf rendering for home/login/registration and web registration automatically signing the new user in. |

Maven writes detailed test results to `code/target/surefire-reports/`.

### Run tests with GitHub Actions

The workflow at `.github/workflows/ci.yml` automatically builds and tests the project when code is pushed to `develop` or a pull request targets `develop`. It starts a temporary PostgreSQL database; it does not use Neon. In GitHub, open the repository's **Actions** tab, select **Build, test, and deploy**, and check the `build-and-test` job. A push to `develop` triggers the Render deployment job only after the tests pass.

## Deployment URL

 Service | URL |
|---|---|
| Thymeleaf Web UI | https://auction-system-68vk.onrender.com/ |
| Swagger UI | https://auction-system-68vk.onrender.com/swagger-ui/index.html |
| PostgreSQL | Neon PostgreSQL |

## Project Structure

```text
Auction-System/
├── code/       # Spring Boot application, tests, and configuration
├── doc/        # API specification, design documents, diagrams, and slides
├── img/        # Project images and media
├── test/       # Test report and test documentation
├── docker-compose.yml
└── README.md
```
