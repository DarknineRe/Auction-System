# 3. Class Diagram (พร้อมตำแหน่ง Design Pattern)

path ทุกไฟล์อยู่ใต้ `code/src/main/java/com/example/project/`
เพื่อให้อ่านได้ แยกเป็น 4 รูป: (A) ภาพรวมทุก Layer ของ flow ประมูล/ชำระเงิน, (B) State Pattern, (C) Entity และความสัมพันธ์, (D) GoF Creational Patterns (Singleton / Factory Method / Builder)

## 3.0 สัญลักษณ์ Pattern ที่ใช้ในแผนภาพ

| Stereotype | Pattern | ตำแหน่งในโค้ด |
|---|---|---|
| `<<Controller>>` | MVC (Controller) / Layered – Presentation | `controller/api/*`, `controller/PageController` |
| `<<Service>>` | Service Layer Pattern | `service/*Service` + `service/implementation/*Impl` |
| `<<Repository>>` | Repository Pattern (Spring Data JPA) | `repository/*` |
| `<<Entity>>` | Domain Model (Model ของ MVC) | `model/*` |
| `<<DTO>>` | DTO Pattern | `dto/request/*`, `dto/response/*` |
| `<<Mapper>>` | Mapper | `mapper/*` |
| `<<State>>` / `<<ConcreteState>>` | **State** (GoF – Behavioral) | `service/state/*` |
| `<<Registry>>` | ตัวเลือก State ตาม enum (Lookup ผ่าน `EnumMap`) | `*StateResolver` |
| `<<Builder>>` | **Builder** (GoF – Creational, เขียนเอง) | `model/PaymentBuilder` (ใช้โดย `PaymentServiceImpl.createForClosedBidding`) |
| `<<Factory Method>>` | **Factory Method** (GoF – Creational, static factory) | `exception/ErrorResponse.of(...)` (ใช้โดย `GlobalExceptionHandler`) |
| `<<Singleton>>` | **Singleton** (GoF – Creational, ผ่าน Spring IoC) | ทุก `@Service` / `@Component` / `@RestController` เช่น `BiddingServiceImpl` |

`*Service`, `*Repository`, `BiddingState`, `PaymentState` ในรูปเป็น **interface** (Mermaid ใส่ stereotype ได้ทีละอัน จึงแสดงเฉพาะชื่อ pattern); ส่วน `*ServiceImpl` และ `*StateResolver` เป็น class จริง

Dependency Injection (constructor injection) ใช้กับทุก class ในแผนภาพ — ลูกศร `..>` แสดงสิ่งที่ถูก inject
Singleton: ทุก `@Service` / `@Component` / `@RestController` เป็น Spring singleton bean (scope เริ่มต้น)

## 3.A ภาพรวม Layered Architecture (flow ประมูล → ชำระเงิน)

```mermaid
classDiagram
    direction TB

    %% ---------- Presentation ----------
    class BiddingController {
        <<Controller>>
        +createBidding()
        +getBiddings()
        +placeBid()
        +cancelBidding()
        +rateSeller()
    }
    class PaymentController {
        <<Controller>>
        +getPurchases()
        +getSales()
        +submitSlip()
        +confirmPayment()
        +rejectPayment()
        +ship()
    }
    class AdminBiddingController {
        <<Controller>>
        +cancelBidding()
        +closeBidding()
    }

    %% ---------- DTO / Mapper ----------
    class PlaceBidRequest {
        <<DTO>>
        amount
    }
    class BiddingResponse {
        <<DTO>>
    }
    class PaymentResponse {
        <<DTO>>
    }
    class BiddingMapper {
        <<Mapper>>
        +toResponse(Bidding)
        +toResponse(BidAction)
    }
    class PaymentMapper {
        <<Mapper>>
        +toResponse(Payment)
    }

    %% ---------- Service interfaces ----------
    class BiddingService {
        <<Service>>
        +createBidding()
        +placeBid()
        +cancelBidding()
    }
    class PaymentService {
        <<Service>>
        +createForClosedBidding()
        +submitSlip()
        +confirmPayment()
        +rejectPayment()
        +ship()
        +expireOverduePayments()
    }
    class BiddingClosingService {
        <<Service>>
        +close()
        +closeExpiredBiddings()
    }
    class AdminBiddingService {
        <<Service>>
        +cancelBidding()
        +closeBidding()
    }

    %% ---------- Service implementations ----------
    class BiddingServiceImpl {
        -MIN_BID_INCREMENT
        +placeBid()
    }
    class PaymentServiceImpl {
        +submitSlip()
        +ship()
    }
    class BiddingClosingServiceImpl
    class AdminBiddingServiceImpl

    %% ---------- Builder pattern (Creational) ----------
    class PaymentBuilder {
        <<Builder>>
        +bidding(Bidding) PaymentBuilder
        +buyer(User) PaymentBuilder
        +sellerprofile(Sellerprofile) PaymentBuilder
        +amount(BigDecimal) PaymentBuilder
        +dueInDays(int) PaymentBuilder
        +build() Payment
    }

    %% ---------- State pattern ----------
    class BiddingStateResolver {
        <<Registry>>
        +resolve(Status) BiddingState
    }
    class PaymentStateResolver {
        <<Registry>>
        +resolve(Status) PaymentState
    }
    class BiddingState {
        <<State>>
    }
    class PaymentState {
        <<State>>
    }

    %% ---------- Scheduler ----------
    class BiddingExpiryScheduler {
        <<Scheduler>>
    }
    class PaymentExpiryScheduler {
        <<Scheduler>>
    }

    %% ---------- Repository ----------
    class BiddingRepository {
        <<Repository>>
        +findByIdForUpdate()
    }
    class BidActionRepository {
        <<Repository>>
    }
    class PaymentRepository {
        <<Repository>>
    }
    class SellerprofileRepository {
        <<Repository>>
    }

    %% ---------- Domain ----------
    class Bidding {
        <<Entity>>
    }
    class BidAction {
        <<Entity>>
    }
    class Payment {
        <<Entity>>
    }
    class Sellerprofile {
        <<Entity>>
    }

    %% Controller -> Service / Mapper / DTO
    BiddingController ..> BiddingService
    BiddingController ..> BiddingMapper
    BiddingController ..> PlaceBidRequest
    PaymentController ..> PaymentService
    PaymentController ..> PaymentMapper
    AdminBiddingController ..> AdminBiddingService
    BiddingMapper ..> BiddingResponse
    PaymentMapper ..> PaymentResponse

    %% Service impl realises interface (DIP)
    BiddingServiceImpl ..|> BiddingService
    PaymentServiceImpl ..|> PaymentService
    BiddingClosingServiceImpl ..|> BiddingClosingService
    AdminBiddingServiceImpl ..|> AdminBiddingService

    %% Service -> others
    BiddingServiceImpl ..> BiddingStateResolver
    BiddingServiceImpl ..> BiddingRepository
    BiddingServiceImpl ..> BidActionRepository
    PaymentServiceImpl ..> PaymentStateResolver
    PaymentServiceImpl ..> PaymentRepository
    PaymentServiceImpl ..> SellerprofileRepository
    PaymentServiceImpl ..> PaymentBuilder : creates Payment via
    PaymentBuilder ..> Payment : builds
    BiddingClosingServiceImpl ..> PaymentService
    BiddingClosingServiceImpl ..> BiddingRepository
    BiddingClosingServiceImpl ..> BidActionRepository
    AdminBiddingServiceImpl ..> BiddingClosingService
    AdminBiddingServiceImpl ..> BiddingStateResolver

    %% Schedulers
    BiddingExpiryScheduler ..> BiddingClosingService
    PaymentExpiryScheduler ..> PaymentService

    %% State
    BiddingStateResolver o-- BiddingState
    PaymentStateResolver o-- PaymentState

    %% Repository -> Entity
    BiddingRepository ..> Bidding
    BidActionRepository ..> BidAction
    PaymentRepository ..> Payment
    SellerprofileRepository ..> Sellerprofile
```

**จุดที่ใช้ Pattern ในรูป A**

| Pattern | ตำแหน่ง | เหตุผล |
|---|---|---|
| Layered Architecture | Controller → Service → Repository → Entity (ลูกศรทุกเส้นไปทางเดียว ไม่ข้าม Layer) | แยกความรับผิดชอบ, ทดสอบทีละชั้นได้ |
| Service Layer | `*Service` / `*ServiceImpl` | รวม business rule และ `@Transactional` ไว้ที่เดียว (เช่น กฎ bid ขั้นต่ำใน `BiddingServiceImpl.placeBid`) |
| DTO + Mapper | `PlaceBidRequest`, `BiddingResponse`, `BiddingMapper` | ไม่ส่ง Entity ออก API |
| Dependency Injection / DIP | `BiddingController ..> BiddingService` (interface) | Controller รู้จักแค่ interface; ใช้ constructor injection |
| State (GoF Behavioral) | `BiddingState`, `PaymentState` + `*StateResolver` | ดูรูป B |
| Builder (GoF Creational) | `PaymentBuilder` ← `PaymentServiceImpl.createForClosedBidding` | รวมการประกอบ `Payment` (bidding, buyer, seller, amount) และคำนวณ `createdAt`/`dueDate` ไว้ที่เดียว ตรวจความครบถ้วนใน `build()` — ดูรูป D |

## 3.B State Pattern (Bidding และ Payment)

```mermaid
classDiagram
    direction LR

    class BiddingState {
        <<State>>
        +getStatus() Bidding.Status
        +acceptsBids() boolean
        +canMoveTo(Bidding.Status) boolean
    }
    class ActiveBiddingState {
        <<ConcreteState>>
        acceptsBids true
        canMoveTo CLOSED or CANCELLED
    }
    class ClosedBiddingState {
        <<ConcreteState>>
        acceptsBids false
        canMoveTo none
    }
    class CancelledBiddingState {
        <<ConcreteState>>
        acceptsBids false
        canMoveTo none
    }
    class BiddingStateResolver {
        <<Registry>>
        -EnumMap states
        +BiddingStateResolver(List~BiddingState~)
        +resolve(Bidding.Status) BiddingState
    }

    ActiveBiddingState ..|> BiddingState
    ClosedBiddingState ..|> BiddingState
    CancelledBiddingState ..|> BiddingState
    BiddingStateResolver o-- BiddingState : collects all beans

    class PaymentState {
        <<State>>
        +getStatus() Payment.Status
        +canMoveTo(Payment.Status) boolean
    }
    class AwaitingPaymentState {
        <<ConcreteState>>
    }
    class PaymentSubmittedState {
        <<ConcreteState>>
    }
    class PaidPaymentState {
        <<ConcreteState>>
    }
    class CompletedPaymentState {
        <<ConcreteState>>
    }
    class ExpiredPaymentState {
        <<ConcreteState>>
    }
    class CancelledPaymentState {
        <<ConcreteState>>
    }
    class PaymentStateResolver {
        <<Registry>>
        -EnumMap states
        +resolve(Payment.Status) PaymentState
    }

    AwaitingPaymentState ..|> PaymentState
    PaymentSubmittedState ..|> PaymentState
    PaidPaymentState ..|> PaymentState
    CompletedPaymentState ..|> PaymentState
    ExpiredPaymentState ..|> PaymentState
    CancelledPaymentState ..|> PaymentState
    PaymentStateResolver o-- PaymentState : collects all beans

    class BiddingServiceImpl
    class AdminBiddingServiceImpl
    class AdminBidActionServiceImpl
    class PaymentServiceImpl
    class AdminPaymentServiceImpl

    BiddingServiceImpl ..> BiddingStateResolver
    AdminBiddingServiceImpl ..> BiddingStateResolver
    AdminBidActionServiceImpl ..> BiddingStateResolver
    PaymentServiceImpl ..> PaymentStateResolver
    AdminPaymentServiceImpl ..> PaymentStateResolver
```

**เหตุผลที่ใช้ State:** กฎ "สถานะไหนไปสถานะไหนได้" ของ Bidding (3 สถานะ) และ Payment (6 สถานะ) ไม่ต้องกระจายเป็น `if/switch` ใน Service — แต่ละสถานะรู้กฎของตัวเอง Service ถามผ่าน `resolver.resolve(status).canMoveTo(target)` และเพิ่มสถานะใหม่ได้ด้วยการเพิ่ม class (OCP)

หมายเหตุ: การนำ State มาใช้แบบนี้เป็นรูปแบบ "state เป็นพฤติกรรมตรวจกฎการย้าย" โดย Entity ยังเก็บสถานะเป็น enum (เพื่อ persist ด้วย JPA) และ Service เป็นผู้สั่งเปลี่ยนสถานะ

## 3.C Entity และความสัมพันธ์ (Domain Layer)

```mermaid
classDiagram
    direction LR

    class User {
        <<Entity>>
        -Long id
        -String name
        -String email
        -String password
        -String phone
        -String address
        -Role role
        -boolean enabled
    }
    class Sellerprofile {
        <<Entity>>
        -Long sellprofileId
        -String bankaccount
        -BigDecimal rating
        -int salecount
    }
    class Artwork {
        <<Entity>>
        -Long id
        -String title
        -String imageUrl
    }
    class Bidding {
        <<Entity>>
        -Long id
        -BigDecimal startingPrice
        -BigDecimal lastBid
        -Date startDate
        -Date endDate
        -Status status
        -Integer sellerRating
    }
    class BidAction {
        <<Entity>>
        -Long id
        -BigDecimal amount
        -Date timestamp
        -Status status
        -Date voidedAt
        -String voidReason
    }
    class Comment {
        <<Entity>>
        -Long id
        -String message
        -int thumbsup
        -int thumbsdown
    }
    class CommentReaction {
        <<Entity>>
        -Long id
        -Type type
    }
    class Payment {
        <<Entity>>
        -Long id
        -BigDecimal amount
        -Status status
        -Date dueDate
        -String slipUrl
        -String shippingAddress
        -String carrier
        -String trackingNumber
    }

    class Role {
        <<enumeration>>
        USER
        ADMIN
        SUPER_ADMIN
    }
    class BiddingStatus {
        <<enumeration>>
        ACTIVE
        CLOSED
        CANCELLED
    }
    class BidStatus {
        <<enumeration>>
        VALID
        VOIDED
    }
    class PaymentStatus {
        <<enumeration>>
        AWAITING_PAYMENT
        PAYMENT_SUBMITTED
        PAID
        COMPLETED
        EXPIRED
        CANCELLED
    }
    class ReactionType {
        <<enumeration>>
        LIKE
        DISLIKE
    }

    Sellerprofile "1" --> "1" User : user (OneToOne)
    Artwork "*" --> "1" Sellerprofile : sellerprofile (ManyToOne)
    Bidding "*" --> "*" Artwork : artworks (ManyToMany)
    Bidding "*" --> "1" User : owner
    Bidding "*" --> "0..1" User : winner
    Bidding "1" *-- "*" BidAction : bidActions (cascade ALL)
    Bidding "1" *-- "*" Comment : comments (cascade ALL, orphanRemoval)
    BidAction "*" --> "1" User : user
    BidAction "*" --> "0..1" User : voidedBy
    Comment "*" --> "1" User : user
    Comment "1" *-- "*" CommentReaction : reactions (cascade ALL, orphanRemoval)
    CommentReaction "*" --> "1" User : user
    Payment "1" --> "1" Bidding : bidding (OneToOne)
    Payment "*" --> "1" User : buyer
    Payment "*" --> "1" Sellerprofile : sellerprofile

    User ..> Role
    Bidding ..> BiddingStatus
    BidAction ..> BidStatus
    Payment ..> PaymentStatus
    CommentReaction ..> ReactionType
```

ในโค้ดจริง enum เหล่านี้เป็น nested enum (`User.Role`, `Bidding.Status`, `BidAction.Status`, `Payment.Status`, `CommentReaction.Type`) — รูปนี้แยกเป็นกล่องเพื่อให้อ่านง่าย

## 3.D GoF Creational Patterns (Singleton, Factory Method, Builder)

```mermaid
classDiagram
    direction LR

    %% ---------- Singleton (Spring IoC) ----------
    class SpringIoCContainer {
        <<Container>>
        singleton scope (default)
    }
    class BiddingServiceImpl {
        <<Singleton>>
        -BiddingRepository biddingRepository
        -BiddingStateResolver stateResolver
        +placeBid()
    }
    class PaymentServiceImpl {
        <<Singleton>>
        -PaymentRepository paymentRepository
        -int dueDays
        +createForClosedBidding(Bidding)
    }
    SpringIoCContainer ..> BiddingServiceImpl : one shared instance
    SpringIoCContainer ..> PaymentServiceImpl : one shared instance

    %% ---------- Factory Method (static factory) ----------
    class ErrorResponse {
        <<Factory Method>>
        -Instant timestamp
        -int status
        -String error
        -String message
        -String path
        -List~String~ details
        +of(HttpStatus, String, String, List~String~) ErrorResponse$
    }
    class GlobalExceptionHandler {
        <<RestControllerAdvice>>
        -build(HttpStatus, String, HttpServletRequest, List~String~)
    }
    GlobalExceptionHandler ..> ErrorResponse : ErrorResponse.of(...)

    %% ---------- Builder ----------
    class PaymentBuilder {
        <<Builder>>
        -Bidding bidding
        -User buyer
        -Sellerprofile sellerprofile
        -BigDecimal amount
        -int dueDays
        +bidding(Bidding) PaymentBuilder
        +buyer(User) PaymentBuilder
        +sellerprofile(Sellerprofile) PaymentBuilder
        +amount(BigDecimal) PaymentBuilder
        +dueInDays(int) PaymentBuilder
        +build() Payment
    }
    class Payment {
        <<Entity / Product>>
        -Bidding bidding
        -User buyer
        -Sellerprofile sellerprofile
        -BigDecimal amount
        -Date createdAt
        -Date dueDate
    }
    PaymentServiceImpl ..> PaymentBuilder : new PaymentBuilder()...build()
    PaymentBuilder ..> Payment : creates (sets createdAt, dueDate)
```

| Pattern | Role ในรูป | ปัญหาที่แก้ |
|---|---|---|
| Singleton | `BiddingServiceImpl`, `PaymentServiceImpl` (และทุก `@Service`/`@Component`) | มี instance เดียวต่อ Spring context ไม่ต้องเขียน `getInstance()` เอง และ inject ผ่าน constructor ได้ |
| Factory Method | `ErrorResponse.of(...)` | จัดรูปแบบ error body (timestamp, status, reason phrase) ที่จุดเดียว `GlobalExceptionHandler` ไม่ต้องเรียก constructor 6 พารามิเตอร์เอง |
| Builder | `PaymentBuilder` → `Payment` | `Payment` ต้องมีหลายค่าและคำนวณ `createdAt`/`dueDate`; Builder ทำให้อ่านง่ายและโยน `IllegalStateException` ถ้าลืมตั้งค่าที่จำเป็น (`bidding`, `buyer`, `sellerprofile`, `amount`) |

หมายเหตุ: `PaymentBuilder` เป็น Builder ที่เขียนเอง (ไม่ใช้ Lombok `@Builder`) และ `status` เริ่มต้นเป็น `AWAITING_PAYMENT` จากค่า default ของ `Payment`
