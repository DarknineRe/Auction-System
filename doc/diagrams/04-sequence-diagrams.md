# 4. Sequence Diagrams

path ทุกไฟล์อยู่ใต้ `code/src/main/java/com/example/project/` — ลำดับการเรียกตรงกับโค้ดจริง และเป็นไปตามกฎ Layered Architecture (Controller ไม่เรียก Repository ตรง ๆ)

| # | Scenario | โค้ดหลัก |
|---|---|---|
| 4.1 | ผู้ใช้ลงราคาประมูล (Place Bid) | `BiddingController.placeBid`, `BiddingServiceImpl.placeBid` |
| 4.2 | ระบบปิดประมูลอัตโนมัติและสร้างรายการชำระเงิน | `BiddingExpiryScheduler`, `BiddingClosingServiceImpl`, `PaymentServiceImpl.createForClosedBidding`, `PaymentBuilder` |
| 4.3 | ชำระเงินและจัดส่ง (ส่งสลิป → ยืนยัน → จัดส่ง) | `PaymentController`, `PaymentServiceImpl` |
| 4.4 | Admin ยกเลิก (Void) bid | `AdminBidActionController`, `AdminBidActionServiceImpl.voidBid` |

---

## 4.1 Place Bid

```mermaid
sequenceDiagram
    autonumber
    actor Bidder as User (Bidder)
    participant Sec as Spring Security
    participant BC as BiddingController
    participant US as UserService
    participant BS as BiddingService<br/>(BiddingServiceImpl)
    participant SR as BiddingStateResolver
    participant BR as BiddingRepository
    participant AR as BidActionRepository
    participant BM as BiddingMapper
    participant DB as PostgreSQL

    Bidder->>Sec: POST /api/v1/biddings/{id}/bids {amount}
    Sec->>Sec: authenticate (HTTP Basic / session)
    Sec->>BC: placeBid(id, authentication, PlaceBidRequest)
    Note over BC: @Valid ตรวจ amount ก่อนเข้า method
    BC->>US: getCurrentUser(email)
    US-->>BC: User
    BC->>BS: placeBid(id, email, amount)
    activate BS
    Note over BS: @Transactional
    BS->>BR: findByIdForUpdate(id)
    BR->>DB: SELECT ... FOR UPDATE (pessimistic lock)
    DB-->>BR: Bidding
    BR-->>BS: Bidding
    BS->>BS: load bidder by email (UserRepository)

    alt ผู้ประมูลเป็นเจ้าของ
        BS-->>BC: throw 409 "Cannot bid on your own auction"
    else
        BS->>SR: resolve(bidding.status)
        SR-->>BS: BiddingState
        BS->>BS: state.acceptsBids() ?
        BS->>BS: ตรวจ startDate / endDate
        BS->>AR: findTopByBidding_IdAndStatusOrderByAmountDesc(id, VALID)
        AR->>DB: SELECT top valid bid
        DB-->>AR: highest bid or empty
        AR-->>BS: Optional BidAction
        BS->>BS: ไม่ใช่ highest bidder อยู่แล้ว
        BS->>BS: amount >= (highest + 1.00) หรือ startingPrice
        BS->>AR: save(new BidAction)
        AR->>DB: INSERT bidactions
        BS->>BR: save(bidding with lastBid = amount)
        BR->>DB: UPDATE biddings
        BS-->>BC: BidAction
    end
    deactivate BS

    BC->>BM: toResponse(bidAction)
    BM-->>BC: BidActionResponse
    BC-->>Bidder: 201 Created (BidActionResponse)

    Note over BC,Bidder: ถ้า service throw ResponseStatusException → GlobalExceptionHandler แปลงเป็น ErrorResponse (404 / 409 / 400)
```

**Pattern ที่เห็นใน scenario นี้:** Layered + Service Layer (กฎ bid อยู่ใน Service), DTO/Mapper (`PlaceBidRequest` → `BidActionResponse`), State (`BiddingStateResolver.resolve(...).acceptsBids()`), Repository (Spring Data JPA), Dependency Injection

---

## 4.2 ระบบปิดประมูลอัตโนมัติและสร้างรายการชำระเงิน

```mermaid
sequenceDiagram
    autonumber
    participant Sch as BiddingExpiryScheduler<br/>(@Scheduled 60s)
    participant CS as BiddingClosingService<br/>(BiddingClosingServiceImpl)
    participant BR as BiddingRepository
    participant AR as BidActionRepository
    participant PS as PaymentService<br/>(PaymentServiceImpl)
    participant SPR as SellerprofileRepository
    participant PR as PaymentRepository
    participant DB as PostgreSQL

    loop ทุก 60 วินาที (app.bidding.expiry-check-ms)
        Sch->>CS: closeExpiredBiddings()
        activate CS
        Note over CS: @Transactional
        CS->>BR: findByStatusAndEndDateBefore(ACTIVE, now)
        BR->>DB: SELECT expired ACTIVE biddings
        DB-->>BR: List Bidding
        BR-->>CS: expired list

        loop แต่ละ Bidding ที่หมดเวลา
            CS->>AR: findTopByBidding_IdAndStatusOrderByAmountDesc(id, VALID)
            AR-->>CS: Optional top bid
            opt มี bid VALID
                CS->>CS: bidding.winner = topBid.user<br/>bidding.lastBid = topBid.amount
            end
            CS->>CS: bidding.status = CLOSED
            CS->>BR: save(bidding)
            BR->>DB: UPDATE biddings
            CS->>PS: createForClosedBidding(bidding)
            activate PS
            alt ไม่มีผู้ชนะ หรือมี Payment อยู่แล้ว
                PS-->>CS: return (ไม่สร้าง)
            else มีผู้ชนะ
                PS->>SPR: findByUser_Id(owner.id)
                SPR-->>PS: Sellerprofile
                alt ไม่มี Seller Profile
                    PS->>PS: log.warn (ไม่สร้าง Payment)
                else
                    PS->>PS: new PaymentBuilder().bidding().buyer(winner)<br/>.sellerprofile().amount(lastBid).dueInDays(dueDays).build()<br/>(Builder: สร้าง Payment AWAITING_PAYMENT,<br/>createdAt = now, dueDate = now + dueDays)
                    PS->>PR: save(payment)
                    PR->>DB: INSERT payments
                end
            end
            deactivate PS
        end
        CS-->>Sch: จำนวนที่ปิด
        deactivate CS
    end
```

**Pattern:** Scheduler เรียก Service ผ่าน interface (DIP), Service Layer ประสานหลาย Repository ใน transaction เดียว, State (Bidding `ACTIVE` → `CLOSED`)
**หมายเหตุ:** `BiddingClosingService.close` ถูกใช้ซ้ำโดย `AdminBiddingServiceImpl.closeBidding` (Admin ปิดประมูลก่อนเวลา) จึงมี flow เดียวกัน

---

## 4.3 ชำระเงินและจัดส่ง (Buyer ส่งสลิป → Seller ยืนยัน → Seller จัดส่ง)

```mermaid
sequenceDiagram
    autonumber
    actor Buyer as User (Buyer / Winner)
    actor Seller
    participant PC as PaymentController
    participant PS as PaymentService<br/>(PaymentServiceImpl)
    participant SR as PaymentStateResolver
    participant PR as PaymentRepository
    participant SPR as SellerprofileRepository
    participant PM as PaymentMapper
    participant DB as PostgreSQL

    rect rgb(235, 245, 255)
    Note over Buyer,DB: ขั้นที่ 1: Buyer ส่งสลิปชำระเงิน (AWAITING_PAYMENT → PAYMENT_SUBMITTED)
    Buyer->>PC: POST /api/v1/payments/{id}/slip {slipUrl}
    PC->>PS: submitSlip(id, buyerId, slipUrl)
    PS->>PR: findByIdForUpdate(id)
    PR->>DB: SELECT ... FOR UPDATE
    DB-->>PS: Payment
    PS->>PS: isBuyer? (else 403)
    PS->>SR: resolve(AWAITING_PAYMENT).canMoveTo(PAYMENT_SUBMITTED)
    SR-->>PS: true (else 409)
    PS->>PS: ตรวจ dueDate (else 409) และที่อยู่ของ Buyer (else 400)
    PS->>PR: save(slipUrl, paidAt, snapshot shippingAddress,<br/>status = PAYMENT_SUBMITTED)
    PR->>DB: UPDATE payments
    PC->>PM: toResponse(payment)
    PC-->>Buyer: 200 OK (PaymentResponse)
    end

    rect rgb(240, 255, 240)
    Note over Seller,DB: ขั้นที่ 2: Seller ตรวจสลิป (PAYMENT_SUBMITTED → PAID หรือกลับ AWAITING_PAYMENT)
    Seller->>PC: POST /api/v1/payments/{id}/confirm
    PC->>PS: confirmPayment(id, sellerUserId)
    PS->>PR: findByIdForUpdate(id)
    PS->>PS: isSeller? (else 403)
    PS->>SR: resolve(PAYMENT_SUBMITTED).canMoveTo(PAID)
    SR-->>PS: true
    PS->>PR: save(confirmedAt, status = PAID)
    PC-->>Seller: 200 OK
    Note right of Seller: ถ้าสลิปไม่ถูกต้อง: POST .../reject {reason}<br/>→ ล้างสลิป, ต่อ dueDate อย่างน้อย 1 วัน, กลับ AWAITING_PAYMENT
    end

    rect rgb(255, 248, 235)
    Note over Seller,DB: ขั้นที่ 3: Seller จัดส่ง (PAID → COMPLETED)
    Seller->>PC: POST /api/v1/payments/{id}/ship {carrier, trackingNumber, trackingUrl, note}
    PC->>PS: ship(id, sellerUserId, ...)
    PS->>PR: findByIdForUpdate(id)
    PS->>SR: resolve(PAID).canMoveTo(COMPLETED)
    SR-->>PS: true
    PS->>PR: save(shipping info, shippedAt, completedAt, status = COMPLETED)
    PR->>DB: UPDATE payments
    PS->>SPR: addToSalecount(sellerProfileId, +1)
    SPR->>DB: UPDATE seller_profiles SET salecount = salecount + 1
    PC-->>Seller: 200 OK (PaymentResponse)
    end
```

**Pattern:** State (`PaymentStateResolver` ตรวจทุกการเปลี่ยนสถานะ), Service Layer + `@Transactional`, Pessimistic Locking กัน race condition, DTO/Mapper
**Error ทั่วไป:** `403` ไม่ใช่เจ้าของ payment, `409` ย้ายสถานะไม่ได้/เลยกำหนด, `404` ไม่พบ

---

## 4.4 Admin ยกเลิก (Void) bid

```mermaid
sequenceDiagram
    autonumber
    actor Admin
    participant Sec as Spring Security
    participant AC as AdminBidActionController
    participant AS as AdminBidActionService<br/>(AdminBidActionServiceImpl)
    participant AR as BidActionRepository
    participant BR as BiddingRepository
    participant SR as BiddingStateResolver
    participant DB as PostgreSQL

    Admin->>Sec: POST /api/v1/admin/bids/{bidId}/void {reason}
    Sec->>Sec: ตรวจ role ADMIN หรือ SUPER_ADMIN (else 403)
    Sec->>AC: voidBid(bidId, authentication, VoidBidRequest)
    AC->>AS: voidBid(adminEmail, bidId, reason)
    activate AS
    Note over AS: @Transactional
    AS->>AR: findBiddingIdById(bidId)
    AR-->>AS: biddingId (else 404)
    AS->>BR: findByIdForUpdate(biddingId)
    BR->>DB: SELECT ... FOR UPDATE
    BR-->>AS: Bidding
    AS->>AR: findById(bidId)
    AR-->>AS: BidAction

    alt bid ถูก void แล้ว
        AS-->>AC: throw 409
    else ประมูลไม่ได้เป็น ACTIVE
        AS->>SR: resolve(status).acceptsBids() = false
        AS-->>AC: throw 409
    else ทำรายการได้
        AS->>AS: bid.status = VOIDED, voidedAt, voidedBy = admin, voidReason
        AS->>AR: saveAndFlush(bid)
        AR->>DB: UPDATE bidactions
        AS->>AR: findTopByBidding_IdAndStatusOrderByAmountDesc(biddingId, VALID)
        AR-->>AS: highest remaining valid bid (หรือไม่มี)
        AS->>BR: save(bidding.lastBid = ราคาใหม่ หรือ null)
        BR->>DB: UPDATE biddings
        AS-->>AC: BidAction
    end
    deactivate AS
    AC-->>Admin: 200 OK (AdminBidActionResponse)
```

**Pattern:** State (ยอม void ได้เฉพาะตอน `acceptsBids()`), Service Layer, Mapper/DTO — Admin API แยก Controller/Service ตาม ISP
