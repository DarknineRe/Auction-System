# Auction-System

ระบบสำหรับประมูลสินค้า พัฒนาด้วย Spring Boot, PostgreSQL ตาม Layered Architecture

## สมาชิกและ Branch

| สมาชิก | รหัสนักศึกษา | Section | Branch | Feature Owner |
|---|---:|---:|---|---|
| ปุณยวีร์ แทนคำ | 673380282-8 | 01 | `poonywee_6733802828-01` | backend#1 |
| ปริญญ์นกร อยู่แท้กูล | 673380277-1 | 02 | `parinnakorn_6733802771_02` | |
| พงศพัศ เลบ้านแท่น | 673380283-6 | 01 | `poonywee_6733802828_01` | readme.md, presentation |
| ชนิณทร์ ใจช่วง | 673380264-0 | 01 | `chanin_6733802640_01` | reviewer, testing, deployment |
| จิณณวัตร โพธิ์ศรีทอง | 673380263-2 | 01 | `jinnawat_6733801632_01` | frontend |

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