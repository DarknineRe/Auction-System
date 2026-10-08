# 8. State Diagram

Entity ที่มีสถานะในระบบ: **Bidding**, **Payment**, **BidAction** — กฎการย้ายสถานะของสองตัวแรกถูกเขียนไว้ในคลาส `service/state/*` (State Pattern) ส่วน BidAction ควบคุมใน `AdminBidActionServiceImpl`

## 8.1 Bidding (`Bidding.Status`)

```mermaid
stateDiagram-v2
    [*] --> ACTIVE : Seller เปิดประมูล (createBidding)

    ACTIVE --> CLOSED : endDate ผ่านแล้ว (BiddingExpiryScheduler), หรือ Admin ปิดประมูล
    ACTIVE --> CANCELLED : Seller ยกเลิก (เฉพาะยังไม่มี bid), หรือ Admin ยกเลิก

    CLOSED --> [*]
    CANCELLED --> [*]

    note right of ACTIVE
        acceptsBids = true
        รับ bid, แก้ไขได้ถ้ายังไม่มี bid
    end note
    note right of CLOSED
        acceptsBids = false
        ตั้งผู้ชนะ + lastBid และสร้าง Payment
        ย้ายต่อไม่ได้
    end note
    note right of CANCELLED
        acceptsBids = false
        ย้ายต่อไม่ได้
    end note
```

| จาก → ไป | เงื่อนไข / ผู้สั่ง | โค้ด |
|---|---|---|
| (เริ่ม) → ACTIVE | ค่าเริ่มต้นของ `Bidding.status` | `Bidding.java` |
| ACTIVE → CLOSED | scheduler ทุก 60 วินาที / `POST /api/v1/admin/biddings/{id}/close` | `BiddingClosingServiceImpl.close` |
| ACTIVE → CANCELLED | เจ้าของ (ไม่มี bid VALID) / Admin | `BiddingServiceImpl.cancelBidding`, `AdminBiddingServiceImpl.cancelBidding` |
| CLOSED, CANCELLED → อื่น | ไม่อนุญาต (`canMoveTo` คืน `false`) | `ClosedBiddingState`, `CancelledBiddingState` |

## 8.2 Payment (`Payment.Status`)

```mermaid
stateDiagram-v2
    [*] --> AWAITING_PAYMENT : สร้างอัตโนมัติเมื่อปิดประมูลที่มีผู้ชนะ

    AWAITING_PAYMENT --> PAYMENT_SUBMITTED : Buyer ส่งสลิป (submitSlip)
    AWAITING_PAYMENT --> EXPIRED : เลย dueDate (PaymentExpiryScheduler)
    AWAITING_PAYMENT --> CANCELLED : Admin ยกเลิก

    PAYMENT_SUBMITTED --> PAID : Seller ยืนยัน (confirmPayment)
    PAYMENT_SUBMITTED --> AWAITING_PAYMENT : Seller ปฏิเสธสลิป (rejectPayment)
    PAYMENT_SUBMITTED --> CANCELLED : Admin ยกเลิก

    PAID --> COMPLETED : Seller จัดส่ง (ship)
    PAID --> CANCELLED : Admin ยกเลิก

    COMPLETED --> CANCELLED : Admin ยกเลิกกรณีข้อพิพาท
    EXPIRED --> CANCELLED : Admin ยกเลิก

    CANCELLED --> [*]
```

| สถานะ | ย้ายไปได้ (`canMoveTo`) | คลาส |
|---|---|---|
| AWAITING_PAYMENT | PAYMENT_SUBMITTED, EXPIRED, CANCELLED | `AwaitingPaymentState` |
| PAYMENT_SUBMITTED | PAID, AWAITING_PAYMENT, CANCELLED | `PaymentSubmittedState` |
| PAID | COMPLETED, CANCELLED | `PaidPaymentState` |
| COMPLETED | CANCELLED | `CompletedPaymentState` |
| EXPIRED | CANCELLED | `ExpiredPaymentState` |
| CANCELLED | — (สถานะสุดท้าย) | `CancelledPaymentState` |

ผลข้างเคียง: `COMPLETED` → `salecount` ของ Seller +1; ถ้ายกเลิกหลัง `COMPLETED` → `salecount` −1 และคะแนนที่ผู้ชนะให้ถูกถอน (`AdminPaymentServiceImpl.cancelPayment`)

## 8.3 BidAction (`BidAction.Status`)

```mermaid
stateDiagram-v2
    [*] --> VALID : ผู้ใช้ลงราคา (placeBid)
    VALID --> VOIDED : Admin void (ขณะประมูลยัง ACTIVE)
    VOIDED --> [*]

    note right of VOIDED
        เก็บ voidedAt, voidedBy, voidReason
        ไม่ถูกนับในราคาสูงสุด / ผู้ชนะ
    end note
```
