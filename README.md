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

## วิธีรันทดสอบ

โปรเจกต์ใช้ Maven Wrapper, JUnit 5, Mockito, Spring Boot Test และ MockMvc ปัจจุบันมี automated test 20 รายการ ครอบคลุมการเริ่มระบบ, การสมัครและยืนยันตัวตนบางกรณี, การกำหนดสิทธิ์และ CSRF, การเลื่อนผู้ใช้เป็น admin, การจัดการ auction และ payment, API ของ artwork และ seller profile, bidding และ comment, กฎการประมูล, การ login อัตโนมัติหลังสมัครผ่านหน้าเว็บ และหน้า Thymeleaf บางส่วน

ผลทดสอบนี้ยังไม่ครอบคลุมทุก endpoint ของระบบ แอปใช้ PostgreSQL และ Flyway จึงต้องใช้ฐานข้อมูลทดสอบแยกต่างหาก **ห้ามใช้ฐานข้อมูล Neon ที่ใช้งานจริงในการทดสอบ**

### สิ่งที่ต้องเตรียมและฐานข้อมูลทดสอบ

- Java 26
- PostgreSQL ที่ `localhost:5432`
- ฐานข้อมูลทดสอบชื่อ `auction_test`

หากยังไม่มี role และฐานข้อมูลทดสอบ ให้สร้างใน PostgreSQL ด้วย `psql` หรือ pgAdmin:

```sql
CREATE ROLE auction WITH LOGIN PASSWORD 'auction';
CREATE DATABASE auction_test OWNER auction;
```

ฐานข้อมูลนี้ต้องว่างหรือเป็นฐานข้อมูลที่ลบและสร้างใหม่ได้ เพราะ Flyway จะรัน database migration ของโปรเจกต์เมื่อเริ่ม Spring application context

### รันทดสอบทั้งหมดบน Windows (PowerShell)

เปิด PowerShell ที่โฟลเดอร์หลักของ repository แล้วรัน:

```powershell
Set-Location .\code
$env:DB_URL="jdbc:postgresql://localhost:5432/auction_test"
$env:DB_USERNAME="auction"
$env:DB_PASSWORD="auction"
$env:REMEMBER_ME_KEY="ci-only-remember-me-key"
.\mvnw.cmd --batch-mode verify
```

คำสั่ง `verify` จะ build แอปและรัน automated test ทั้งหมด หากต้องการรันเฉพาะ test ให้ใช้ `.\mvnw.cmd test`

ระบบเปิด CSRF protection สำหรับ API ที่เปลี่ยนแปลงข้อมูลด้วย หากเรียก API ผ่าน client ให้เรียก `GET /register` ก่อนและเก็บ session cookie จากนั้นอ่านค่า `_csrf` ที่ซ่อนอยู่ใน form แล้วส่งค่านั้นใน header `X-CSRF-TOKEN` พร้อม cookie เดิมเมื่อเรียก POST, PUT, PATCH หรือ DELETE ส่วน browser form จะส่ง token ให้อัตโนมัติ

รายการ automated test ปัจจุบัน:

| Test class | ทดสอบเรื่อง |
|---|---|
| `ProjectApplicationTests` | เริ่ม Spring application context ด้วยฐานข้อมูลทดสอบได้ |
| `BiddingServiceImplTest` | กฎการเพิ่มราคา, bid แรก, เจ้าของ auction และ auction ที่ไม่รับ bid โดยใช้ Mockito |
| `UserApiIntegrationTest` | สมัครบัญชี, validation, email ซ้ำ, authentication, แก้ profile/รหัสผ่าน, ปฏิเสธผู้ใช้ทั่วไปเมื่อเข้า admin และปฏิเสธ request ที่ไม่มี CSRF token |
| `ArtworkApiIntegrationTest` | ตั้งค่า seller profile, สร้าง/อ่าน/แก้ไข/ลบ artwork, validation และตรวจสิทธิ์เจ้าของ |
| `SellerprofileApiIntegrationTest` | อ่าน/แก้ seller profile, เรียกโดยไม่ login และตรวจว่า public profile ไม่เปิดเผยเลขบัญชี |
| `BiddingApiIntegrationTest` | สร้าง/อ่าน auction, กฎ bid, bid สูงสุด, ตรวจสิทธิ์ และสร้าง/โต้ตอบ/แก้ไข/ลบ comment |
| `AdminApiIntegrationTest` | ดูรายชื่อผู้ใช้, เลื่อน `USER` เป็น `ADMIN` โดย `SUPER_ADMIN`, ปฏิเสธการเลื่อนโดย `ADMIN`, ปิดบัญชี, ยกเลิก/ปิด auction และตรวจสิทธิ์ payment |
| `PageControllerIntegrationTest` | แสดงหน้า home/login/register ด้วย Thymeleaf และ login อัตโนมัติหลังสมัครผ่านหน้าเว็บ |

Maven บันทึกผลทดสอบโดยละเอียดไว้ที่ `code/target/surefire-reports/`

ดูวิธีตรวจ PASS/FAIL และรายการ manual test ที่ยังเหลือได้ที่ [คู่มือทดสอบ](test/TESTING-GUIDE.md) และ [รายงานผลทดสอบ](test/test-report.md)

### ดูผลทดสอบบน GitHub Actions

Workflow ที่ `.github/workflows/ci.yml` จะ build และทดสอบโปรเจกต์อัตโนมัติเมื่อ push code ไปที่ `main` หรือ `develop` หรือเปิด pull request โดยมี base เป็น `main` หรือ `develop` workflow จะสร้าง PostgreSQL ชั่วคราวสำหรับทดสอบ ไม่ได้ใช้ Neon

ก่อนรัน workflow ให้ตั้ง GitHub repository secrets ที่ **Settings → Secrets and variables → Actions**:

| Secret | ค่าที่ต้องใส่ |
|---|---|
| `DOCKERHUB_USERNAME` | Docker Hub username |
| `DOCKERHUB_TOKEN` | Docker Hub access token (แนะนำให้ใช้ token แทนรหัสผ่านบัญชี) |
| `RENDER_DEPLOY_HOOK` | Render deploy hook URL สำหรับ deploy หลัง test ผ่าน |

`DOCKERHUB_USERNAME` และ `DOCKERHUB_TOKEN` ช่วยให้ GitHub Actions login ก่อน pull image `postgres:16` และลดโอกาสติด unauthenticated pull rate limit

ดูผลได้โดยเปิด repository บน GitHub → แท็บ **Actions** → เลือก **Build, test, and deploy** → เปิด job `build-and-test` การ push ไป `develop` จะเริ่ม job deploy ไป Render ต่อเมื่อ test ผ่านเท่านั้น ส่วน `main` จะ build และทดสอบ แต่ไม่ deploy ไป Render อัตโนมัติ

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
