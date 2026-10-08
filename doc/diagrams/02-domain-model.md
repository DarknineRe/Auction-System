# 2. Domain Model / Conceptual Class Diagram

แสดงแนวคิดของธุรกิจ (ไม่ใส่ Service/Repository/DTO) โดยเน้นเฉพาะ attribute ที่สำคัญต่อโดเมน
ชื่อ class ตรงกับ Entity ใน `code/src/main/java/com/example/project/model/`

```mermaid
classDiagram
    direction LR

    class User {
        name
        email
        phone
        address
        role : USER / ADMIN / SUPER_ADMIN
        enabled
    }
    class SellerProfile {
        bankAccount
        rating
        saleCount
    }
    class Artwork {
        title
        imageUrl
    }
    class Bidding {
        startingPrice
        lastBid
        startDate
        endDate
        status : ACTIVE / CLOSED / CANCELLED
        sellerRating
    }
    class Bid {
        amount
        timestamp
        status : VALID / VOIDED
        voidReason
    }
    class Comment {
        message
        thumbsUp
        thumbsDown
    }
    class Reaction {
        type : LIKE / DISLIKE
    }
    class Payment {
        amount
        status
        dueDate
        slipUrl
        shippingAddress
        carrier
        trackingNumber
    }

    User "1" -- "0..1" SellerProfile : may become
    SellerProfile "1" -- "0..*" Artwork : owns
    Bidding "0..*" -- "1..*" Artwork : auctions
    User "1" -- "0..*" Bidding : opens (owner)
    User "0..1" -- "0..*" Bidding : wins
    User "1" -- "0..*" Bid : places
    Bidding "1" *-- "0..*" Bid : receives
    Bidding "1" *-- "0..*" Comment : has
    User "1" -- "0..*" Comment : writes
    Comment "1" *-- "0..*" Reaction : gets
    User "1" -- "0..*" Reaction : gives
    Bidding "1" -- "0..1" Payment : settled by
    User "1" -- "0..*" Payment : pays (buyer)
    SellerProfile "1" -- "0..*" Payment : receives
```

## อธิบายความสัมพันธ์สำคัญ

| ความสัมพันธ์ | ชนิด | ความหมาย / กฎธุรกิจ |
|---|---|---|
| User — SellerProfile | 1 : 0..1 (One-to-One) | User เป็นผู้ขายได้เมื่อสร้าง Seller Profile เท่านั้น |
| SellerProfile — Artwork | 1 : N | ผลงานเป็นของ Seller Profile หนึ่งเดียว |
| Bidding — Artwork | M : N | ประมูลหนึ่งครั้งมีหลายชิ้นได้ และผลงานเดิมประมูลซ้ำได้ (เช่น หลังยกเลิก) แต่ห้ามอยู่ในประมูล ACTIVE สองรายการพร้อมกัน |
| User — Bidding | 1 : N (owner) และ 0..1 : N (winner) | เจ้าของประมูลห้ามประมูลของตัวเอง; ผู้ชนะถูกกำหนดตอนปิดประมูล |
| Bidding — Bid | 1 : N (composition) | Bid ที่ถูก void ยังเก็บไว้เป็นประวัติ (`VOIDED`) ไม่ถูกลบ |
| Bidding — Comment — Reaction | 1 : N : N (composition) | ลบ Comment แล้ว Reaction ถูกลบตาม; 1 user มี 1 reaction ต่อ 1 comment |
| Bidding — Payment | 1 : 0..1 | มี Payment ได้เฉพาะประมูลที่ปิดแล้วและมีผู้ชนะ |
| Payment — User / SellerProfile | N : 1 | Buyer = ผู้ชนะ; ผู้ขายอ้างผ่าน Seller Profile เพื่อให้ Buyer เห็นเลขบัญชีล่าสุดเสมอ |

หมายเหตุ: ข้อมูลการจัดส่ง (carrier, tracking) เก็บอยู่ใน `Payment` เพราะการจัดส่งเกิดขึ้นเพียงครั้งเดียวต่อการชำระเงินหนึ่งรายการ
