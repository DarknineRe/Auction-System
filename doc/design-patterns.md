# Design Patterns

## Enterprise / Architectural Patterns
| Pattern | ปัญหาที่แก้ / การใช้งาน | ไฟล์/คลาสที่ใช้ |
|---|---|---|
| Layered Architecture | จัดระเบียบโครงสร้างโปรเจกต์ให้แยกส่วนการทำงานชัดเจน (Presentation, Business, Data Access) | โครงสร้างแพ็กเกจทั้งโปรเจกต์ (`controller` ➔ `service` ➔ `repository` ➔ `model`) |
| MVC | แยกส่วนรับ Request (Controller), การประมวลผลข้อมูล (Model), และการแสดงผล (View/JSON) | ทุกคลาสใน `controller/api/` (Controller), Entity/DTO (Model), และ JSON Response (View) |
| Repository Pattern | ซ่อนความซับซ้อนของการเขียน SQL และเชื่อมต่อฐานข้อมูลโดยตรง ทำให้เปลี่ยน Data Source ได้ง่าย | ทุกอินเทอร์เฟซในโฟลเดอร์ `repository/` ที่สืบทอด `JpaRepository` |
| Service Layer Pattern | รวม Business Logic ไว้ในที่เดียว เพื่อให้แยกออกจาก Controller ทำให้โค้ดสามารถใช้ซ้ำได้และทดสอบง่ายขึ้น | ทุกคลาสใน `service/` (Interface) และ `service/implementation/` (Concrete) |
| DTO Pattern + Mapper | ป้องกันการส่ง Entity ของฐานข้อมูลออกไปที่ API โดยตรง และแยก API Contract ออกจาก Database Schema | คลาสต่างๆ ใน `dto/request/`, `dto/response/` และคลาสสำหรับการแปลงใน `mapper/` |
| Dependency Injection | ลดความเชื่อมโยง (Coupling) ระหว่างคลาส เพื่อให้สามารถสลับหรือจำลอง (Mock) คลาสได้ง่ายเวลาเขียนเทสต์ | การใช้งาน Constructor Injection ในคลาสระดับ Controller และ Service (เช่น `BiddingServiceImpl.java`) |

## GoF Patterns (กลุ่ม Creational — 3 แบบ)

| Pattern | ปัญหาที่แก้ | ไฟล์/คลาสที่ใช้ |
|---|---|---|
| **Singleton** | ควบคุมให้มีออบเจกต์ (Instance) ของคลาสเพียงตัวเดียวตลอดอายุการทำงานของระบบเพื่อประหยัดหน่วยความจำ (ทำงานผ่าน Spring IoC Container) | คลาสที่ถูกกำกับด้วย `@Service`, `@RestController` เช่น `service/implementation/BiddingServiceImpl.java` |
| **Factory Method** | ควบคุมและจัดมาตรฐานการสร้างออบเจกต์ที่มีความซับซ้อนให้อยู่ที่จุดเดียว ทำให้ไม่ต้องเรียกใช้ Constructor โดยตรงในหลายๆ คลาสเมื่อต้องโยน Error | `exception/ErrorResponse.java` (มีการใช้ static factory method `ErrorResponse.of(...)` ในการสร้างออบเจกต์) |
| **Builder** (เขียนเอง) | การสร้าง `Payment` ต้องใช้หลายค่า (bidding, buyer, seller, amount) และต้องคำนวณ `createdAt`/`dueDate` เอง การเรียก setter ทีละตัวใน Service ทำให้โค้ดยาวและลืมตั้งค่าได้ จึงรวมขั้นตอนไว้ใน Builder และตรวจความครบถ้วนใน `build()` | Builder: `model/PaymentBuilder.java`<br>ผู้ใช้งาน: `PaymentServiceImpl` (เมธอดสร้าง Payment หลัง bidding ปิด: `new PaymentBuilder().bidding(...).buyer(...).sellerprofile(...).amount(...).dueInDays(...).build()`) |

## Class Diagram ประกอบ (GoF Creational Patterns)

```mermaid
classDiagram
    %% Singleton Pattern
    class BiddingServiceImpl {
        <<Singleton (Spring @Service)>>
        -BiddingRepository biddingRepository
        -UserService userService
        +createBidding(CreateBiddingRequest request, String ownerEmail) Bidding
    }
    
    %% Factory Method Pattern
    class ErrorResponse {
        -Instant timestamp
        -int status
        -String error
        -String message
        -String path
        -List~String~ details
        +of(HttpStatus status, String message, String path, List~String~ details) ErrorResponse$
    }
    
    %% Builder Pattern (hand-written)
    class PaymentBuilder {
        <<Builder>>
        -Bidding bidding
        -User buyer
        -Sellerprofile sellerprofile
        -BigDecimal amount
        -int dueDays
        +bidding(Bidding bidding) PaymentBuilder
        +buyer(User buyer) PaymentBuilder
        +sellerprofile(Sellerprofile sellerprofile) PaymentBuilder
        +amount(BigDecimal amount) PaymentBuilder
        +dueInDays(int dueDays) PaymentBuilder
        +build() Payment
    }
    class Payment

    PaymentBuilder ..> Payment : builds
    PaymentServiceImpl ..> PaymentBuilder : uses
```


