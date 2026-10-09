# REST API Specification — Auction System

- Base URL (local): `http://localhost:8080`
- Base URL (production): `https://auction-system-68vk.onrender.com`
- Swagger UI: `/swagger-ui.html` · OpenAPI JSON: `/v3/api-docs`
- API prefix: `/api/v1`
- Content-Type: `application/json`

## 0. Authentication & Roles

ระบบใช้ **HTTP Basic Auth** (email + password) สำหรับ API และ Form Login + session สำหรับหน้าเว็บ Thymeleaf
(ไม่มี JWT) — CSRF ถูกปิดเฉพาะ `/api/v1/**`

```bash
curl -u user@example.com:password123 http://localhost:8080/api/v1/users/me
```

| Role | สิทธิ์ |
|---|---|
| `USER` | ผู้ใช้ทั่วไป (ซื้อ/ขาย/ประมูล/คอมเมนต์) |
| `ADMIN` | จัดการระบบ + ยกเลิก/ดู payment ทั้งหมด |
| `SUPER_ADMIN` | เหมือน ADMIN ยกเว้น `/api/v1/admin/payments/**` (เฉพาะ `ADMIN`) |

| ระดับการเข้าถึง | Endpoint |
|---|---|
| **Public** (ไม่ต้อง login) | `POST /users`, `GET /artworks/**`, `GET /biddings`, `GET /biddings/{id}`, `GET /biddings/{id}/bids`, `GET /biddings/{id}/bids/highest`, `GET /biddings/{id}/comments`, `GET /seller-profiles/users/{userId}` |
| **Authenticated** | Endpoint อื่นทั้งหมดที่ไม่ใช่ `/admin/**` (รวม `GET /biddings/mine`, `/biddings/won`, `/users/me/bids`) |
| **ADMIN / SUPER_ADMIN** | `/api/v1/admin/**` |

> หมายเหตุ: เจ้าของข้อมูลถูกระบุจากผู้ใช้ที่ login (`Authentication`) ไม่ใช่จาก body —
> ดูข้อสังเกตเรื่อง `ownerId` / `userId` ในหัวข้อ 3

---

## 1. Endpoint ทั้งหมด

คำย่อ: **P** = Public · **A** = ต้อง login · **O** = เจ้าของ resource · **AD** = ADMIN/SUPER_ADMIN · **ADM** = ADMIN เท่านั้น

### Users — `/api/v1/users`
| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| POST | `/users` | 201 | 400, 409 | P | สมัครสมาชิก (409 = อีเมลซ้ำ) |
| GET | `/users/me` | 200 | 401 | A | ดูโปรไฟล์ตัวเอง |
| PUT | `/users/me` | 200 | 400, 401 | A | แก้ไขโปรไฟล์ (name, phone, address) |
| PUT | `/users/me/password` | 204 | 400, 401 | A | เปลี่ยนรหัสผ่าน |

### Seller Profiles — `/api/v1/seller-profiles`
| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| POST | `/seller-profiles` | 201 | 400, 409 | A | สร้างโปรไฟล์ผู้ขาย (409 = มีอยู่แล้ว) |
| GET | `/seller-profiles/me` | 200 | 404 | A | ดูโปรไฟล์ผู้ขายของตัวเอง (รวม bank account) |
| PUT | `/seller-profiles/me` | 200 | 400, 404 | A | แก้ไขเลขบัญชี |
| GET | `/seller-profiles/{sellerProfileId}` | 200 | 404 | A | ดูโปรไฟล์สาธารณะ (ไม่มี bank account) |
| GET | `/seller-profiles/users/{userId}` | 200 | 404 | P | ดูโปรไฟล์สาธารณะจาก userId |

### Artworks — `/api/v1/artworks`
| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| POST | `/artworks` | 201 | 400, 401 | A (ผู้ขาย) | เพิ่มผลงาน |
| GET | `/artworks?page=&size=&sort=` | 200 | | P | รายการผลงาน (**Pagination + Sorting**) |
| GET | `/artworks/{artworkId}` | 200 | 404 | P | รายละเอียดผลงาน |
| GET | `/artworks/seller/{sellerUserId}` | 200 | | P | ผลงานทั้งหมดของผู้ขาย (ไม่แบ่งหน้า) |
| PUT | `/artworks/{artworkId}` | 200 | 400, 403, 404 | O | แก้ไขผลงาน |
| DELETE | `/artworks/{artworkId}` | 204 | 403, 404 | O | ลบผลงาน |

### Biddings — `/api/v1/biddings`
| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| POST | `/biddings` | 201 | 400, 403, 404, 409 | A | เปิดประมูล (รวมหลายผลงานใน 1 การประมูลได้) |
| GET | `/biddings?status=&page=&size=&sort=` | 200 | 400 | P | รายการประมูล (filter ด้วย `status`) |
| GET | `/biddings/mine?status=&page=&size=&sort=` | 200 | 400 | A | การประมูลที่ฉันเป็นเจ้าของ |
| GET | `/biddings/won?page=&size=&sort=` | 200 | 400 | A | การประมูลที่ฉันชนะ |
| GET | `/biddings/{biddingId}` | 200 | 404 | P | รายละเอียดการประมูล |
| PUT | `/biddings/{biddingId}` | 200 | 400, 403, 404, 409 | O | แก้ราคาเริ่มต้น/วันเริ่ม/วันจบ |
| POST | `/biddings/{biddingId}/cancel` | 200 | 403, 404, 409 | O | ยกเลิกการประมูล |
| POST | `/biddings/{biddingId}/bids` | 201 | 400, 404, 409 | A | ลงราคาประมูล |
| POST | `/biddings/{biddingId}/seller-rating` | 200 | 400, 403, 404, 409 | A (ผู้ชนะ) | ให้คะแนนผู้ขาย 1–5 (ให้ได้ครั้งเดียว) |

### Bids — `/api/v1`
| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| GET | `/biddings/{biddingId}/bids` | 200 | 404 | P | ประวัติการประมูล (list) |
| GET | `/biddings/{biddingId}/bids/highest` | 200 | 404 | P | ราคาสูงสุดปัจจุบัน |
| GET | `/users/me/bids` | 200 | 401 | A | ประวัติการประมูลของฉัน |

### Comments — `/api/v1/biddings/{biddingId}/comments`
| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| POST | `/biddings/{biddingId}/comments` | 201 | 400, 404 | A | เพิ่มคอมเมนต์ |
| GET | `/biddings/{biddingId}/comments` | 200 | 404 | P | ดูคอมเมนต์ทั้งหมด |
| PUT | `/biddings/{biddingId}/comments/{commentId}` | 200 | 400, 403, 404 | O | แก้ไขคอมเมนต์ |
| DELETE | `/biddings/{biddingId}/comments/{commentId}` | 204 | 403, 404 | O | ลบคอมเมนต์ |
| POST | `/biddings/{biddingId}/comments/{commentId}/like` | 200 | 403, 404 | A | กดถูกใจ (403 = คอมเมนต์ตัวเอง) |
| POST | `/biddings/{biddingId}/comments/{commentId}/dislike` | 200 | 403, 404 | A | กดไม่ถูกใจ (403 = คอมเมนต์ตัวเอง) |

### Payments — `/api/v1/payments`
Payment ถูกสร้างอัตโนมัติเมื่อการประมูลปิดและมีผู้ชนะ

| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| GET | `/payments/purchases?status=&page=&size=&sort=` | 200 | 400 | A (ผู้ซื้อ) | รายการที่ฉันต้องจ่าย |
| GET | `/payments/sales?status=&page=&size=&sort=` | 200 | 400 | A (ผู้ขาย) | รายการที่ฉันขายได้ |
| GET | `/payments/{paymentId}` | 200 | 403, 404 | ผู้ซื้อ/ผู้ขาย | ดูรายละเอียด |
| POST | `/payments/{paymentId}/slip` | 200 | 400, 403, 404, 409 | ผู้ซื้อ | ส่งหลักฐานการโอน (slipUrl) |
| POST | `/payments/{paymentId}/confirm` | 200 | 403, 404, 409 | ผู้ขาย | ยืนยันว่าได้รับเงิน |
| POST | `/payments/{paymentId}/reject` | 200 | 400, 403, 404, 409 | ผู้ขาย | ปฏิเสธสลิป (กลับไป `AWAITING_PAYMENT`) |
| POST | `/payments/{paymentId}/ship` | 200 | 400, 403, 404, 409 | ผู้ขาย | บันทึกการจัดส่ง → `COMPLETED` |

### Admin — `/api/v1/admin`
| Method | Endpoint | Success | Error | สิทธิ์ | คำอธิบาย |
|---|---|---|---|---|---|
| GET | `/admin/users?role=&page=&size=&sort=` | 200 | 400 | AD | รายชื่อผู้ใช้ (filter ด้วย `role`) |
| GET | `/admin/users/{userId}` | 200 | 404 | AD | ดูผู้ใช้ |
| PATCH | `/admin/users/{userId}/status` | 200 | 400, 404 | AD | เปิด/ระงับบัญชี (`enabled`) |
| POST | `/admin/biddings/{biddingId}/cancel` | 200 | 404, 409 | AD | ยกเลิกการประมูล |
| POST | `/admin/biddings/{biddingId}/close` | 200 | 404, 409 | AD | ปิดการประมูลทันที |
| GET | `/admin/biddings/{biddingId}/bids` | 200 | 404 | AD | ดูบิดทั้งหมด (รวมที่ถูก void) |
| POST | `/admin/bids/{bidId}/void` | 200 | 400, 404, 409 | AD | ทำให้บิดเป็นโมฆะ (ต้องระบุเหตุผล) |
| DELETE | `/admin/artworks/{artworkId}` | 204 | 404 | AD | ลบผลงาน (moderation) |
| DELETE | `/admin/comments/{commentId}` | 204 | 404 | AD | ลบคอมเมนต์ (moderation) |
| GET | `/admin/payments?status=&page=&size=&sort=` | 200 | 400 | **ADM** | payment ทั้งหมด |
| GET | `/admin/payments/{paymentId}` | 200 | 404 | **ADM** | ดู payment |
| POST | `/admin/payments/{paymentId}/cancel` | 200 | 400, 404, 409 | **ADM** | ยกเลิก payment (ต้องระบุเหตุผล) |

### Pagination & Sorting
Endpoint ที่คืนค่าแบบแบ่งหน้ารับ `?page=0&size=10&sort=field,asc` (ค่าเริ่มต้น: `size=10`, `sort=id`)

| Resource | Field ที่ sort ได้ |
|---|---|
| Biddings | `id`, `startingPrice`, `lastBid`, `startDate`, `endDate`, `status`, `owner.id`, `owner.name`, `owner.email` |
| Payments | `id`, `amount`, `status`, `createdAt`, `dueDate`, `completedAt` |

ถ้า sort ด้วย field อื่นจะได้ `400 Cannot sort by: <field>`

---

## 2. Enum

| Enum | ค่า |
|---|---|
| `User.Role` | `USER`, `ADMIN`, `SUPER_ADMIN` |
| `Bidding.Status` | `ACTIVE`, `CLOSED`, `CANCELLED` |
| `BidAction.Status` | `VALID`, `VOIDED` |
| `Payment.Status` | `AWAITING_PAYMENT`, `PAYMENT_SUBMITTED`, `PAID`, `COMPLETED`, `EXPIRED`, `CANCELLED` |

### Payment lifecycle
```
AWAITING_PAYMENT --(buyer: /slip)--> PAYMENT_SUBMITTED --(seller: /confirm)--> PAID --(seller: /ship)--> COMPLETED
        ^                                   |
        +--------(seller: /reject)----------+
AWAITING_PAYMENT --(เลยกำหนด dueDate)--> EXPIRED
ADMIN: /admin/payments/{id}/cancel --> CANCELLED
```
การเปลี่ยนสถานะที่ไม่ถูกต้องคืน `409 Cannot change payment from X to Y`

---

## 3. Request / Response ตัวอย่าง

### Paged response (`PagedModel`)
```json
{
  "content": [ { "...": "..." } ],
  "page": { "size": 10, "number": 0, "totalElements": 42, "totalPages": 5 }
}
```

### POST /api/v1/users — สมัครสมาชิก
**Request** — `name`, `email` (ต้องเป็นอีเมล), `password` (8–72 ตัวอักษร) จำเป็น
```json
{
  "name": "Somchai Jaidee",
  "email": "somchai@example.com",
  "password": "password123",
  "phone": "0812345678",
  "address": "123 Bangkok"
}
```
**Response 201** (`UserResponse`)
```json
{
  "id": 3,
  "name": "Somchai Jaidee",
  "email": "somchai@example.com",
  "phone": "0812345678",
  "address": "123 Bangkok",
  "role": "USER"
}
```

### PUT /api/v1/users/me/password
```json
{ "currentPassword": "password123", "newPassword": "newpassword456" }
```
Response `204 No Content`

### POST /api/v1/seller-profiles
```json
{ "bankaccount": "123-4-56789-0" }
```
**Response 201** (`SellerprofileResponse`)
```json
{ "sellerProfileId": 1, "userId": 3, "bankaccount": "123-4-56789-0", "rating": 0, "saleCount": 0 }
```

### POST /api/v1/artworks
```json
{ "title": "Sunset Over Bangkok", "imageUrl": "https://example.com/sunset.jpg" }
```
**Response 201** (`ArtworkResponse`)
```json
{
  "id": 10,
  "title": "Sunset Over Bangkok",
  "imageUrl": "https://example.com/sunset.jpg",
  "sellerprofileId": 1,
  "sellerUserId": 3
}
```

### POST /api/v1/biddings — เปิดประมูล
**Request** — `artworkIds` ต้องไม่ว่างและเป็นผลงานของผู้เปิดประมูลที่ไม่ได้อยู่ในการประมูลอื่น
```json
{
  "artworkIds": [10, 11],
  "ownerId": 3,
  "startingPrice": 1000.00,
  "startDate": "2026-10-10T09:00:00.000+00:00",
  "endDate": "2026-10-17T09:00:00.000+00:00"
}
```
**Response 201** (`BiddingResponse`)
```json
{
  "id": 5,
  "artworkIds": [10, 11],
  "ownerId": 3,
  "startingPrice": 1000.00,
  "lastBid": null,
  "startDate": "2026-10-10T09:00:00.000+00:00",
  "endDate": "2026-10-17T09:00:00.000+00:00",
  "status": "ACTIVE",
  "winnerId": null,
  "sellerRating": null
}
```
> ⚠️ `ownerId` เป็น `@NotNull` ใน DTO จึงต้องส่งมา แต่ server **ใช้ผู้ใช้ที่ login เป็นเจ้าของ** และไม่ได้ใช้ค่านี้

### POST /api/v1/biddings/{biddingId}/bids — ลงราคา
```json
{ "userId": 4, "amount": 1500.00 }
```
**Response 201** (`BidActionResponse`)
```json
{ "id": 21, "biddingId": 5, "userId": 4, "amount": 1500.00, "timestamp": "2026-10-11T14:30:00.000+00:00" }
```
> ⚠️ `userId` เป็น `@NotNull` ใน DTO จึงต้องส่งมา แต่ server ใช้ผู้ใช้ที่ login (`authentication.getName()`) เป็นผู้ลงบิดเสมอ

### POST /api/v1/biddings/{biddingId}/seller-rating
```json
{ "score": 5 }
```
Response 200 → `PublicSellerprofileResponse`
```json
{ "sellerProfileId": 1, "userId": 3, "name": "Somchai Jaidee", "rating": 5.00, "saleCount": 1 }
```

### POST /api/v1/biddings/{biddingId}/comments
```json
{ "message": "สวยมากครับ" }
```
**Response 201** (`CommentResponse`)
```json
{ "id": 8, "biddingId": 5, "userId": 4, "message": "สวยมากครับ", "thumbsup": 0, "thumbsdown": 0 }
```

### POST /api/v1/payments/{paymentId}/slip
`slipUrl` ต้องเป็น http(s) URL
```json
{ "slipUrl": "https://example.com/slip.png" }
```

### POST /api/v1/payments/{paymentId}/ship
`trackingUrl` ต้องเป็น http(s) URL
```json
{
  "carrier": "Kerry Express",
  "trackingNumber": "KEX1234567890",
  "trackingUrl": "https://th.kerryexpress.com/track/KEX1234567890",
  "shippingNote": "ส่งแล้ววันนี้"
}
```
**Response 200** (`PaymentResponse`)
```json
{
  "id": 2,
  "biddingId": 5,
  "buyerId": 4,
  "sellerUserId": 3,
  "sellerProfileId": 1,
  "sellerBankAccount": "123-4-56789-0",
  "amount": 1500.00,
  "status": "COMPLETED",
  "createdAt": "2026-10-17T09:00:00.000+00:00",
  "dueDate": "2026-10-20T09:00:00.000+00:00",
  "slipUrl": "https://example.com/slip.png",
  "paidAt": "2026-10-18T10:00:00.000+00:00",
  "confirmedAt": "2026-10-18T12:00:00.000+00:00",
  "rejectReason": null,
  "shippingAddress": "123 Bangkok",
  "carrier": "Kerry Express",
  "trackingNumber": "KEX1234567890",
  "trackingUrl": "https://th.kerryexpress.com/track/KEX1234567890",
  "shippingNote": "ส่งแล้ววันนี้",
  "shippedAt": "2026-10-19T08:00:00.000+00:00",
  "completedAt": "2026-10-19T08:00:00.000+00:00",
  "cancelledAt": null,
  "cancelReason": null
}
```

### Request body อื่น ๆ
| Endpoint | Body |
|---|---|
| `PUT /users/me` | `{ "name": "...", "phone": "...", "address": "..." }` |
| `PUT /seller-profiles/me` | `{ "bankaccount": "..." }` |
| `PUT /artworks/{id}` | `{ "title": "...", "imageUrl": "..." }` |
| `PUT /biddings/{id}` | `{ "startingPrice": 1200.00, "startDate": "...", "endDate": "..." }` |
| `PUT /biddings/{id}/comments/{commentId}` | `{ "message": "..." }` |
| `POST /payments/{id}/reject` | `{ "reason": "สลิปไม่ถูกต้อง" }` |
| `PATCH /admin/users/{id}/status` | `{ "enabled": false }` |
| `POST /admin/bids/{bidId}/void` | `{ "reason": "Suspicious bid" }` |
| `POST /admin/payments/{id}/cancel` | `{ "reason": "..." }` |

### Response ฝั่ง Admin
`AdminUserResponse` = `UserResponse` + `enabled`

`AdminBidActionResponse`
```json
{
  "id": 21, "biddingId": 5, "userId": 4, "amount": 1500.00,
  "timestamp": "2026-10-11T14:30:00.000+00:00",
  "status": "VOIDED",
  "voidedAt": "2026-10-12T09:00:00.000+00:00",
  "voidedById": 1,
  "voidReason": "Suspicious bid"
}
```

---

## 4. Error Response Format

จัดการโดย `@RestControllerAdvice` ใน `exception/GlobalExceptionHandler.java`
(ระบบไม่มี `errorCode` — ให้ดูจาก `status` และ `message`)

```json
{
  "timestamp": "2026-10-11T14:30:00.123Z",
  "status": 409,
  "error": "Conflict",
  "message": "Bid must be at least 1550.00",
  "path": "/api/v1/biddings/5/bids",
  "details": []
}
```

Validation ผิดพลาด (400) — `details` เป็นรายการ `field: message`
```json
{
  "timestamp": "2026-10-11T14:31:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/users",
  "details": [
    "email: must be a well-formed email address",
    "password: size must be between 8 and 72"
  ]
}
```

### HTTP Status ที่ใช้

| Status | เกิดเมื่อ |
|---|---|
| 400 | Validation ไม่ผ่าน, body อ่านไม่ได้, parameter ผิดชนิด, sort field ไม่ถูกต้อง |
| 401 | ไม่ได้ login / รหัสผ่านผิด (`Authentication required`) |
| 403 | ไม่มีสิทธิ์ (`You do not have permission to perform this action`) หรือไม่ใช่เจ้าของ |
| 404 | ไม่พบข้อมูล (`... not found`) |
| 409 | ขัดแย้งกับสถานะ/ข้อมูลเดิม (ดูตารางด้านล่าง) |
| 500 | ข้อผิดพลาดที่ไม่คาดคิด (`An unexpected error occurred`) |

---

## 5. กฎทางธุรกิจที่ทำให้เกิด Error (จากโค้ดจริง)

| Status | Message | Endpoint | สาเหตุ |
|---|---|---|---|
| 409 | `Email is already registered` | POST /users | อีเมลซ้ำ |
| 400 | `Password must not exceed 72 bytes` | POST /users, PUT /users/me/password | รหัสผ่านยาวเกิน |
| 400 | `Current password is incorrect` | PUT /users/me/password | รหัสผ่านเดิมผิด |
| 400 | `New password must be different` | PUT /users/me/password | รหัสใหม่ซ้ำรหัสเดิม |
| 409 | `Seller profile already exists` | POST /seller-profiles | มีโปรไฟล์ผู้ขายแล้ว |
| 400 | `Bank account is required` | PUT /seller-profiles/me | ไม่ระบุเลขบัญชี |
| 400 | `A bidding needs at least one artwork.` | POST /biddings | ไม่มี artworkIds |
| 400 | `Duplicate artwork IDs in request.` | POST /biddings | artworkIds ซ้ำ |
| 400 | `Start date cannot be in the past.` | POST /biddings | วันเริ่มเป็นอดีต |
| 400 | `End date must be after start date.` | POST /biddings | วันจบก่อนวันเริ่ม |
| 403 | `Artwork {id} does not belong to the bidding owner` | POST /biddings | ใช้ผลงานของผู้อื่น |
| 409 | `Artwork {id} is already part of an active bidding` | POST /biddings | ผลงานอยู่ในประมูลอื่นแล้ว |
| 403 | `Only the bidding owner can modify it.` | PUT, cancel bidding | ไม่ใช่เจ้าของ |
| 409 | `This bidding is {status} and can no longer be {action}.` | PUT, cancel bidding | สถานะไม่ใช่ `ACTIVE` |
| 409 | `Cannot bid on your own auction.` | POST /bids | บิดการประมูลตัวเอง |
| 409 | `This bidding is {status} and is not accepting bids.` | POST /bids | ไม่ใช่ `ACTIVE` |
| 409 | `This bidding has not started yet.` | POST /bids | ยังไม่ถึงเวลาเริ่ม |
| 409 | `This bidding has already closed.` | POST /bids | เลยเวลาจบแล้ว |
| 409 | `You are already the highest bidder.` | POST /bids | เป็นผู้บิดสูงสุดอยู่แล้ว |
| 409 | `Bid must be at least {minimumBid}` | POST /bids | บิดต่ำกว่าขั้นต่ำ |
| 403 | `You cannot react to your own comment` | like/dislike | กดโต้ตอบคอมเมนต์ตัวเอง |
| 403 | `Only the comment author can modify it` | PUT/DELETE comment | ไม่ใช่เจ้าของคอมเมนต์ |
| 403 | `Only the buyer or the seller can view this payment` | GET /payments/{id} | ไม่เกี่ยวข้องกับ payment |
| 403 | `Only the buyer can pay for this payment` | POST /slip | ไม่ใช่ผู้ซื้อ |
| 403 | `Only the seller can update this payment` | confirm / reject / ship | ไม่ใช่ผู้ขาย |
| 409 | `The payment deadline has passed` | POST /slip | เลยกำหนดชำระ |
| 400 | `Add a shipping address to your profile before paying` | POST /slip | ผู้ซื้อยังไม่มีที่อยู่จัดส่ง |
| 409 | `Cannot change payment from {X} to {Y}` | payment actions | สถานะไม่ถูกต้อง |
| 409 | `Bid is already voided: {id}` | POST /admin/bids/{id}/void | ถูก void แล้ว |
| 409 | `This rating ... / This bidding has already been rated` | POST /seller-rating | ให้คะแนนซ้ำหรือยังไม่เข้าเงื่อนไข |
| 409 | `The request conflicts with existing data` | ทั่วไป | ละเมิด unique/FK constraint |

---

## 6. Validation (Bean Validation)

| DTO | กฎ |
|---|---|
| `RegisterUserRequest` | `name` NotBlank · `email` NotBlank + Email · `password` NotBlank, 8–72 |
| `ChangePasswordRequest` | `currentPassword` NotBlank · `newPassword` NotBlank, 8–72 |
| `CreateArtworkRequest` / `UpdateArtworkRequest` | `title` NotBlank, ≤255 · `imageUrl` ≤2048 |
| `CreateBiddingRequest` | `artworkIds` NotEmpty (แต่ละตัว Positive) · `ownerId` NotNull, Positive · `startingPrice`, `startDate`, `endDate` NotNull |
| `UpdateBiddingRequest` | `startingPrice` NotNull, Positive · `startDate`, `endDate` NotNull |
| `PlaceBidRequest` | `userId` NotNull, Positive · `amount` NotNull |
| `RateSellerRequest` | `score` NotNull, 1–5 |
| `Create/UpdateCommentRequest` | `message` NotBlank, ≤2000 |
| `SellerprofileRequest` | `bankaccount` NotBlank |
| `SubmitPaymentSlipRequest` | `slipUrl` NotBlank, ≤2048, ต้องเป็น `http(s)://` |
| `ShipPaymentRequest` | `carrier`, `trackingNumber` NotBlank ≤100 · `trackingUrl` NotBlank ≤2048 `http(s)://` · `shippingNote` ≤1000 |
| `Reject/CancelPaymentRequest`, `VoidBidRequest` | `reason` NotBlank, ≤500 |
| `UpdateUserStatusRequest` | `enabled` NotNull |
