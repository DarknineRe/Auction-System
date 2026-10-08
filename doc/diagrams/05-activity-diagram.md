# 5. Activity Diagram

Mermaid ไม่มี Activity Diagram โดยตรง จึงใช้ flowchart: วงรี = จุดเริ่ม/จบ, สี่เหลี่ยม = กิจกรรม, ข้าวหลามตัด = เงื่อนไข (decision), กรอบ = swimlane

## 5.1 กระบวนการลงราคาประมูล (Place Bid)

```mermaid
flowchart TD
    Start(["เริ่ม: ผู้ใช้กดลงราคา"]) --> Auth{"ล็อกอินแล้ว?"}
    Auth -- ไม่ --> E401[/"401 Unauthorized"/]
    Auth -- ใช่ --> Valid{"amount ผ่าน Bean Validation?"}
    Valid -- ไม่ --> E400[/"400 Bad Request"/]
    Valid -- ใช่ --> Lock["ล็อกแถว Bidding<br/>SELECT FOR UPDATE"]
    Lock --> Found{"พบประมูล?"}
    Found -- ไม่ --> E404[/"404 Not Found"/]
    Found -- ใช่ --> Own{"ผู้ลงราคาเป็น<br/>เจ้าของประมูล?"}
    Own -- ใช่ --> E409[/"409 Conflict"/]
    Own -- ไม่ --> Acc{"สถานะรับ bid?<br/>acceptsBids"}
    Acc -- ไม่ --> E409
    Acc -- ใช่ --> Time{"อยู่ระหว่าง<br/>startDate - endDate?"}
    Time -- ไม่ --> E409
    Time -- ใช่ --> Top{"ผู้ลงราคาเป็น<br/>ผู้สูงสุดอยู่แล้ว?"}
    Top -- ใช่ --> E409
    Top -- ไม่ --> Min{"amount >= ขั้นต่ำ?<br/>bid แรก: startingPrice<br/>ถัดไป: สูงสุด + 1.00"}
    Min -- ไม่ --> E409
    Min -- ใช่ --> Save["บันทึก BidAction VALID"]
    Save --> Upd["อัปเดต Bidding.lastBid"]
    Upd --> Resp[/"201 Created + BidActionResponse"/]
    Resp --> End(["จบ"])
    E401 --> End
    E400 --> End
    E404 --> End
    E409 --> End
```

## 5.2 วงจรชีวิตของการประมูลจนส่งมอบสินค้า (Swimlane)

```mermaid
flowchart TD
    subgraph SELLER["Seller"]
        S1(["เริ่ม"]) --> S2["สร้าง Seller Profile"]
        S2 --> S3["สร้าง Artwork"]
        S3 --> S4["เปิดประมูล<br/>สถานะ ACTIVE"]
        S12["ตรวจสลิป"]
        S13{"สลิปถูกต้อง?"}
        S14["ยืนยัน -> PAID"]
        S15["ปฏิเสธพร้อมเหตุผล<br/>-> AWAITING_PAYMENT"]
        S16["จัดส่งและกรอกเลขพัสดุ<br/>-> COMPLETED<br/>salecount +1"]
        S12 --> S13
        S13 -- ใช่ --> S14 --> S16
        S13 -- ไม่ --> S15
    end

    subgraph BUYER["Bidder / Buyer"]
        B1["ลงราคาประมูล<br/>หลายครั้งได้"]
        B2{"เป็นผู้ชนะ?"}
        B3["ส่งสลิปก่อน dueDate<br/>-> PAYMENT_SUBMITTED"]
        B4["ให้คะแนน Seller"]
        B1 --> B2
        B2 -- ใช่ --> B3
    end

    subgraph SYSTEM["System Scheduler"]
        Y1{"endDate ผ่านแล้ว?"}
        Y2["ปิดประมูล -> CLOSED<br/>ตั้งผู้ชนะ + lastBid"]
        Y3{"มีผู้ชนะ<br/>และ Seller Profile?"}
        Y4["สร้าง Payment<br/>AWAITING_PAYMENT<br/>dueDate = now + 3 วัน"]
        Y5{"เลย dueDate<br/>และยังไม่ส่งสลิป?"}
        Y6["Payment -> EXPIRED"]
        Y1 -- ใช่ --> Y2 --> Y3
        Y3 -- ใช่ --> Y4 --> Y5
        Y5 -- ใช่ --> Y6
    end

    subgraph ADMIN["Admin (ทางเลือก)"]
        A1["void bid / ปิด-ยกเลิกประมูล<br/>/ ยกเลิก Payment"]
    end

    S4 --> B1
    B1 --> Y1
    Y3 -- ไม่ --> End1(["จบ: ไม่มีการชำระเงิน"])
    Y4 --> B3
    B3 --> S12
    S15 --> B3
    S16 --> B4
    B4 --> End2(["จบ: ส่งมอบสำเร็จ"])
    Y6 --> End3(["จบ: หมดอายุ"])
    A1 -. แทรกแซงได้ .-> Y2
```

หมายเหตุ: ค่า `dueDate` เริ่มต้น 3 วัน มาจาก `app.payment.due-days:3` ใน `PaymentServiceImpl`; Seller ยกเลิกประมูลเองได้เฉพาะตอนยังไม่มี bid (ไม่ได้แสดงในรูป — ดูหัวข้อ UC08 และ State Diagram)
