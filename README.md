# Auction-System

ระบบสำหรับประมูลสินค้า พัฒนาด้วย Spring Boot, PostgreSQL ตาม Layered Architecture

## สมาชิกและ Branch

| สมาชิก | รหัสนักศึกษา | Section | Branch | Feature Owner |
|---|---:|---:|---|---|
| ปุณยวีร์ แทนคำ | 673380282-8 | 01 | `poonywee_6733802828-01` | **Backend Core & Payment System**<br>- Security & User Authentication<br>- Comment System & Seller Profile<br>- Admin Management (User, Bidding, Moderation, Bid Action)<br>- Bidding Concurrency (Row Lock) & State Pattern<br>- Payment System (State Pattern, Scheduler, Admin) |
| ปริญญ์นกร อยู่แท้กูล | 673380277-1 | 02 | `parinnakorn_6733802771_02` | **Artwork, Bid Tracking & API Doc**<br>- Artwork Management (Service, Controller, DTOs, Mapper)<br>- BidAction Service & Controller<br>- Global Exception Handler & ErrorResponse<br>- OpenAPI / Swagger UI Configuration<br>- Database Constraints & JPA Relations Fixes<br>- SOLID Principles Analysis |
| พงศพัศ เลบ้านแท่น | 673380283-6 | 01 | `pongsapat_6733802836_01` | **Architecture, Financial Precision & System Design**<br>- Bidding Service Architecture & Core Logic<br>- Monetary Precision (Double to BigDecimal Migration)<br>- Super Admin Role & Security / Validation Logic<br>- Database Migration (Flyway) & Timezone Setup (ICT)<br>- System Architecture, Diagrams (.puml/SVG), Design Patterns & Slides |
| ชนิณทร์ ใจช่วง | 673380264-0 | 01 | `chanin_6733802640_01` | **Lead, DevOps, Testing & Thymeleaf Integration**<br>- Project Structure & Layered Architecture Setup<br>- Code Reviewer & PR Merge Management<br>- Bidding Controller & REST API DTOs<br>- Thymeleaf Web Integration & Workspace Dashboard<br>- CI/CD (GitHub Actions), Dockerization & Render Deployment |
| จิณณวัตร โพธิ์ศรีทอง | 673380263-2 | 01 | `jinnawat_6733801632_01` | **Frontend & UI/UX Development**<br>- Design System, Color Theme & Responsive Layout<br>- Shared Components (Navbar, Buttons, Forms, Cards, Badges)<br>- Authentication UI (Login & Register Pages)<br>- Bidding Detail Page (Countdown, Bid Form, History, Comments)<br>- User Profile & Change Password UI |

### รายละเอียดภาระงานและ Feature Ownership (อ้างอิงจาก Git Commit Log)

1. **ปุณยวีร์ แทนคำ (`poonywee_6733802828-01` / Git: `punya-wee`)**
   - **Entity Models & Security:** สร้าง Model พื้นฐาน (User, SellerProfile, Artwork, Bidding, BidAction, Comment), ตั้งค่า Spring Security ให้เชื่อมโยงกับฐานข้อมูล User, จัดการ User Service/Controller, สร้าง `AdminInitializer` เพื่อ seed Super Admin อัตโนมัติจาก Environment Variables
   - **Profile & Comment Subsystem:** พัฒนา SellerProfile Service/Controller/DTO/Mapper, พัฒนา Comment Service/Controller/DTO/Mapper และระบบ Reaction (Like/Dislike)
   - **Admin Management System:** พัฒนาโมดูลผู้ดูแลระบบ ได้แก่ AdminUserService (เปิด/ปิดการใช้งานผู้ใช้), AdminBiddingService (ยกเลิกและปิดประมูล), AdminModerationService (ลบ artwork และ comment ที่ไม่เหมาะสม), AdminBidActionService (ตรวจสอบและย้อนประมูล / Rollback bid)
   - **Bidding Concurrency & Business Rules:** ออกแบบ State Pattern สำหรับสถานะการประมูล (ACTIVE, CLOSED, CANCELLED), ป้องกันปัญหา Concurrency ด้วย Database Row-level Locking (Pessimistic Lock), ระบบ Auto-close การประมูลที่หมดเวลาและบันทึกผู้ชนะ, ระบบแบ่งหน้าและเรียงลำดับการประมูล (Pagination & Sorting)
   - **Payment Subsystem:** พัฒนาระบบชำระเงินเต็มรูปแบบ (Payment Entity, Repository, Service, Controller, AdminPaymentService/Controller), ใช้ State Pattern ในการจัดการสถานะการจ่ายเงิน (Awaiting, Submitted, Paid, Completed, Expired, Cancelled) และสร้าง `PaymentExpiryScheduler` สำหรับจัดการรายการที่หมดเวลาชำระเงิน

2. **ปริญญ์นกร อยู่แท้กูล (`parinnakorn_6733802771_02` / Git: `parinnakorn`)**
   - **Artwork Subsystem (CRUD):** พัฒนาโมดูลจัดการผลงานศิลปะครบวงจร ได้แก่ `ArtworkService`, `ArtworkServiceImpl`, `ArtworkController`, `ArtworkMapper` และ DTOs (`CreateArtworkRequest`, `UpdateArtworkRequest`, `ArtworkResponse`)
   - **BidAction History & Tracking:** พัฒนาโมดูลประวัติการเคาะประมูล ได้แก่ `BidActionService`, `BidActionServiceImpl`, `BidActionController`, `BidActionRepository`
   - **Global Error Handling:** วางโครงสร้างการจัดการ Error ของระบบ REST API ด้วย `GlobalExceptionHandler` และสร้างมาตรฐานการตอบกลับด้วย `ErrorResponse` (จัดการ HTTP status 400, 401, 403, 404 อย่างเป็นระบบ)
   - **API Documentation & OpenAPI:** ติดตั้ง Dependency `springdoc-openapi`, พัฒนา `OpenApiConfig` เพื่อสร้างเอกสาร Swagger UI พร้อมการยืนยันตัวตน (Basic Auth) และเปิด path ใน SecurityConfig
   - **Database & Entity Constraints:** ปรับปรุงความสัมพันธ์และ Constraints ของ Entity ให้ถูกต้องตามหลัก Relational Model (Unique/Nullable ของ SellerProfile, User Role เป็น EnumType.STRING, ManyToOne สำหรับ Comment, และแก้ไข Cascade)
   - **SOLID Principles Documentation:** วิเคราะห์และจัดทำเอกสารหลักการออกแบบระบบตาม SOLID Principles พร้อมอ้างอิงไฟล์และบรรทัดโค้ด (`doc/solid-analysis.md`)

3. **พงศพัศ เลบ้านแท่น (`pongsapat_6733802836_01` / Git: `zeemongteng`)**
   - **Core Bidding Architecture:** ริเริ่มและออกแบบอินเทอร์เฟซ `BiddingService`, พัฒนาโครงร่าง `BiddingServiceImpl` และฟังก์ชันหลัก เช่น `placeBid`
   - **Financial Precision Refactoring:** ตรวจสอบและ Refactor ระบบตัวเลขการเงินจากการใช้ Double มาเป็น `BigDecimal` ทั้งระบบ (Entities, DTOs, Services) เพื่อความแม่นยำทางธุรกรรม ป้องกันปัญหา floating-point error
   - **Super Admin & Security Authorization:** พัฒนาระบบสิทธิ์ `SUPER_ADMIN` เพื่อให้มีเพียงผู้ดูแลระดับสูงเท่านั้นที่สามารถระงับสิทธิ์แอดมินคนอื่นได้, ปรับปรุงการตรวจสอบสิทธิ์ผ่านอีเมลแทน ID จาก client, ป้องกันปัญหา access control และ input constraints (จำกัดความยาวฟิลด์ password, URL, ข้อความ เพื่อป้องกัน crash)
   - **Database Migration & Localization:** ตั้งค่าระบบ Database Migration ด้วย Flyway (`db/migration/`) เพื่อรองรับการย้ายฐานข้อมูลอัตโนมัติ, กำหนดการตั้งค่าเวลาของระบบให้เป็น Bangkok Timezone (UTC+7 / ICT)
   - **System Architecture & Documentation:** ออกแบบและเขียนไดอะแกรมระบบทั้งหมด (PlantUML `.puml`, SVG, PNG: ER Diagram, Class Diagram, Sequence Diagram, Activity Diagram, Component & Deployment, State Diagram), จัดทำเอกสาร REST API Specification (`doc/api-spec.md`), เอกสาร Design Patterns (`doc/design-patterns.md`), Presentation Slides และดูแล `README.md`

4. **ชนิณทร์ ใจช่วง (`chanin_6733802640_01` / Git: `DarknineRe`)**
   - **Project Architecture & Code Review:** วางรากฐานและโครงสร้างโปรเจกต์ Spring Boot ตาม Layered Architecture, ปรับปรุงโครงสร้างโฟลเดอร์ของ Repository, รับผิดชอบเป็น Code Reviewer ตรวจสอบและ Merge Pull Requests ของทีมทั้งหมดเข้าสู่ `develop` และ `main`
   - **Bidding Controller & DTOs:** พัฒนา `BiddingController` สำหรับ REST API และสร้าง DTOs ที่เกี่ยวข้อง (`CreateBiddingRequest`, `PlaceBidRequest`, `BidActionResponse`, `BiddingResponse`, `BiddingMapper`)
   - **Thymeleaf Web Integration & Workspace Dashboard:** พัฒนา `PageController`, `WebPageExceptionHandler`, `AuctionListingService`, และหน้า `workspace.html` เพื่อเป็นแดชบอร์ดจัดการระบบสำหรับผู้ซื้อ ผู้ขาย และแอดมิน พร้อมเชื่อมโยง Pagination ร่วมกับ Backend
   - **DevOps, Testing & Cloud Deployment:** ตั้งค่า GitHub Actions CI Workflow (`.github/workflows/ci.yml`), เขียนสคริปต์รันเทสต์ `aw`, สร้าง Dockerfile และ `docker-compose.yml`, ติดตั้งและ Deploy ระบบขึ้น Cloud บน Render Web Service ร่วมกับ Neon PostgreSQL

5. **จิณณวัตร โพธิ์ศรีทอง (`jinnawat_6733801632_01` / Git: `WaffleXL`)**
   - **Frontend Design System & Styling:** ออกแบบและวางมาตรฐาน CSS Design System ทั้งระบบ (ชุดสี `variables.css`, Base styles, Responsive typography)
   - **Shared UI Components:** พัฒนาคอมโพเนนต์ส่วนกลางสำหรับใช้งานซ้ำ ได้แก่ Navbar (`navbar.html`, `navbar.css`), Buttons, Form inputs & Textarea, Cards, Badges, Pagination UI, และ State indicators
   - **Authentication Pages:** พัฒนาหน้าเว็บลงชื่อเข้าใช้ (Login) และสมัครสมาชิก (Register) พร้อมระบบตรวจสอบฟอร์มและเชื่อมต่อกับ Spring Security Routes
   - **Auction / Bidding Detail Page:** พัฒนาหน้าแสดงรายละเอียดการประมูล (`bidding-detail.html`) พร้อมระบบนับเวลาถอยหลังแบบเรียลไทม์ (Live Countdown Timer), ฟอร์มเคาะประมูล, แถบประวัติการเคาะราคา (Bid History Timeline), และส่วนแสดงความคิดเห็น (Comment Section)
   - **User Profile UI:** พัฒนาหน้าดูและแก้ไขข้อมูลส่วนตัว (`profile.html`), ประวัติของผู้ใช้ และฟอร์มเปลี่ยนรหัสผ่าน (Change Password)

##  Tech Stack

- **Backend:** Spring Boot 4.1.1, Java 26, Spring MVC, Spring Security, Bean Validation
- **Build Tool:** Maven (Maven Wrapper)
- **Database:** Neon PostgreSQL
- **ORM:** Spring Data JPA (Hibernate)
- **API Documentation:** OpenAPI / Swagger UI (Springdoc)
- **Frontend:** Thymeleaf
- **Deployment:** Render Web Service (Hybrid)
- **Containerization:** Docker

##  System Architecture

- **Presentation Layer:** Controller / RestController / Views
- **Service Layer:** Business Logic & Transactions
- **Repository Layer:** Data Access Layer (Spring Data JPA)
- **Domain / Entity:** Entities, Value Objects, Enums & DTOs

##  Database Design (ER Diagram)


##  Installation & Setup (LOCAL)

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

##  How to Run

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


##  API Documentation

> 📖 **[ดูรายละเอียด REST API Specification ฉบับเต็มได้ที่นี่ (doc/api-spec.md)](doc/api-spec.md)**

| Service | URL |
|---|---|
| Swagger UI (local) | http://localhost:8080/swagger-ui.html |
| Swagger UI (production) | https://auction-system-68vk.onrender.com/swagger-ui/index.html |
| REST API | อยู่ใน Swagger UI ทุก endpoint ขึ้นต้นด้วย /api/v1 |

##  How to Run Tests

`@Darknine` all yours buddy

##  Deployment URL

 Service | URL |
|---|---|
| Thymeleaf Web UI | https://auction-system-68vk.onrender.com/ |
| Swagger UI | https://auction-system-68vk.onrender.com/swagger-ui/index.html |
| PostgreSQL | Neon PostgreSQL |

##  Project Structure