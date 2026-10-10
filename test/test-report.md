# รายงานผลทดสอบ Backend — 10 ตุลาคม 2026

## 1. ข้อมูลการทดสอบ

| รายการ | รายละเอียด |
|---|---|
| โปรเจกต์ | Auction-System |
| คำสั่งทดสอบอัตโนมัติ | `.\mvnw.cmd --batch-mode --no-transfer-progress verify` |
| ฐานข้อมูล | PostgreSQL 16 container แยกสำหรับทดสอบ (`auction_test`, `localhost:5433`) |
| Test framework | JUnit 5, Mockito, Spring Boot Test, MockMvc |
| ผล automated test | ผ่าน 20, ล้มเหลว 0, error 0, ข้าม 0 |
| ผล build | `BUILD SUCCESS`, exit code 0 |

การทดสอบสร้างบัญชีทดสอบและใช้ฐานข้อมูล disposable ห้ามชี้ test ไปยัง Neon production

## 2. กรณีทดสอบและผลลัพธ์

### ระบบ, ผู้ใช้ และ authentication

| รหัส | Endpoint | ข้อมูล/การทดสอบ | ผลที่คาดหวัง | ผลที่ได้ | สถานะ |
|---|---|---|---|---|---|
| BE-001 | Spring Boot startup | เริ่ม application context และรัน Flyway migration | ระบบเริ่มได้ | `ProjectApplicationTests.contextLoads` ผ่านเมื่อเชื่อม PostgreSQL | ผ่าน |
| BE-002 | `POST /api/v1/users` | สมัครด้วยข้อมูลที่ถูกต้อง | HTTP 201 และอ่านบัญชีกลับได้ | HTTP 201; `GET /me` หลัง authentication คืนผู้ใช้ที่สมัคร | ผ่าน |
| BE-003 | `POST /api/v1/users` | ชื่อว่าง, email ไม่ถูกต้อง, password สั้น | HTTP 400 พร้อม validation error | HTTP 400; response มี `Validation failed` | ผ่าน |
| BE-004 | `POST /api/v1/users` | สมัคร email เดิมซ้ำ | คำขอที่สองได้ HTTP 409 | สมัครซ้ำได้ HTTP 409 | ผ่าน |
| BE-005 | `GET /api/v1/users/me` | ไม่ได้ authentication | HTTP 401 | HTTP 401 | ผ่าน |
| BE-006 | `GET /api/v1/users/me` | Basic authentication ที่ถูกต้อง | HTTP 200 และข้อมูลผู้ใช้ปัจจุบัน | HTTP 200; email ตรงกับ test account | ผ่าน |
| BE-007 | `PUT /api/v1/users/me` | แก้ชื่อ, เบอร์โทร และที่อยู่ | HTTP 200 พร้อม profile ใหม่ | HTTP 200; response มีชื่อที่แก้แล้ว | ผ่าน |
| BE-008 | `PUT /api/v1/users/me/password` | เปลี่ยน password ปัจจุบัน | HTTP 204; password ใหม่ใช้ได้และ password เดิมใช้ไม่ได้ | HTTP 204; login ด้วยรหัสใหม่ผ่าน รหัสเดิมได้ HTTP 401 | ผ่าน |
| BE-046 | `PUT /api/v1/users/me` | ส่งคำขอที่ authentication แล้วแต่ไม่มี CSRF token | HTTP 403 และ profile ไม่เปลี่ยน | HTTP 403; อ่านซ้ำพบชื่อเดิม | ผ่าน |
| BE-047 | `POST /register` | สมัครผ่านหน้าเว็บ | กลับหน้าแรกและ session นั้น login แล้ว | กลับ `/`; session เดิมเปิด `/profile` ที่ต้อง login ได้ | ผ่าน |
| BE-038 | `GET /api/v1/admin/users` | ผู้ใช้ทั่วไปขอรายชื่อผู้ใช้ admin | HTTP 403 | HTTP 403 | ผ่าน |

### Seller และ artwork

| รหัส | Endpoint | ข้อมูล/การทดสอบ | ผลที่คาดหวัง | ผลที่ได้ | สถานะ |
|---|---|---|---|---|---|
| BE-009 | `POST /api/v1/seller-profiles` | ผู้ใช้ที่ login สร้าง seller profile | HTTP 201 | HTTP 201 | ผ่าน |
| BE-010 | `POST /api/v1/artworks` | Seller สร้าง artwork | HTTP 201 และมี ID | HTTP 201; ได้ artwork ID | ผ่าน |
| BE-011 | `POST /api/v1/artworks` | ไม่ได้ login / ชื่อว่าง | HTTP 401 / HTTP 400 | HTTP 401 / HTTP 400 | ผ่าน |
| BE-012 | `GET /api/v1/artworks?page=0&size=10&sort=id,desc` | อ่านรายการแบบ pagination และ sorting | HTTP 200 พร้อมรายการแบ่งหน้า | HTTP 200; content มี artwork ที่สร้าง | ผ่าน |
| BE-013 | `GET /api/v1/artworks/{id}` | อ่าน artwork ที่สร้างไว้ | HTTP 200 และ ID ตรงกัน | HTTP 200; ID ตรงกัน | ผ่าน |
| BE-014 | `PUT /api/v1/artworks/{id}` | เจ้าของแก้ artwork | HTTP 200 และรายละเอียดเปลี่ยน | HTTP 200; title ใหม่ถูกส่งกลับ | ผ่าน |
| BE-015 | `PUT /api/v1/artworks/{id}` | ผู้ใช้อื่นพยายามแก้ artwork | HTTP 403 | HTTP 403 | ผ่าน |
| BE-016 | `DELETE /api/v1/artworks/{id}` | เจ้าของลบ artwork ที่ไม่มีประวัติ bidding | HTTP 204; อ่านอีกครั้งได้ 404 | HTTP 204; อ่านอีกครั้งได้ HTTP 404 | ผ่าน |

### Seller profile

| รหัส | Endpoint | ข้อมูล/การทดสอบ | ผลที่คาดหวัง | ผลที่ได้ | สถานะ |
|---|---|---|---|---|---|
| BE-039 | `GET /api/v1/seller-profiles/me` | Seller อ่าน profile ส่วนตัว | HTTP 200 และเจ้าของเห็นเลขบัญชี | HTTP 200; ได้เลขบัญชีตามที่ตั้งไว้ | ผ่าน |
| BE-040 | `PUT /api/v1/seller-profiles/me` | Seller แก้เลขบัญชี | HTTP 200 และค่าใหม่ถูกส่งกลับ | HTTP 200; ได้ค่าใหม่ | ผ่าน |
| BE-041 | `GET /api/v1/seller-profiles/users/{userId}` | อ่าน public profile | HTTP 200 โดยไม่เปิดเผยเลขบัญชี | HTTP 200; มี user ID แต่ไม่มีเลขบัญชี | ผ่าน |
| BE-042 | `GET /api/v1/seller-profiles/me` | ไม่ได้ authentication | HTTP 401 | HTTP 401 | ผ่าน |

### Bidding, bid และ comment

| รหัส | Endpoint | ข้อมูล/การทดสอบ | ผลที่คาดหวัง | ผลที่ได้ | สถานะ |
|---|---|---|---|---|---|
| BE-017 | `POST /api/v1/biddings` | Seller สร้าง bidding สำหรับ artwork ของตน | HTTP 201 | HTTP 201; อ่าน bidding ที่สร้างได้ | ผ่าน |
| BE-018 | `GET /api/v1/biddings?page=0&size=5&sort=id,asc` | อ่านรายการแบบแบ่งหน้า | HTTP 200 พร้อม content | HTTP 200; ได้ content array | ผ่าน |
| BE-019 | `GET /api/v1/biddings/{id}` | อ่าน bidding ที่สร้างไว้ | HTTP 200 และสถานะ `ACTIVE` | HTTP 200; สถานะเป็น `ACTIVE` | ผ่าน |
| BE-020 | `POST /api/v1/biddings/{id}/bids` | ไม่ได้ authentication | HTTP 401 | HTTP 401 | ผ่าน |
| BE-021 | `POST /api/v1/biddings/{id}/bids` | เจ้าของ auction bid ใน auction ของตน | HTTP 409 | HTTP 409 | ผ่าน |
| BE-022 | `POST /api/v1/biddings/{id}/bids` | bid แรกเท่ากับราคาเริ่มต้น 100.00 | HTTP 201 | HTTP 201; จำนวน 100.00 | ผ่าน |
| BE-023 | `POST /api/v1/biddings/{id}/bids` | bid ถัดไปตาม minimum increment 102.50 | HTTP 201 | HTTP 201; จำนวน 102.50 | ผ่าน |
| BE-024 | `POST /api/v1/biddings/{id}/bids` | bid ต่ำกว่า increment ที่กำหนด | HTTP 409 | HTTP 409 | ผ่าน |
| BE-025 | `GET /api/v1/biddings/{id}/bids/highest` | อ่าน bid สูงสุด | HTTP 200; จำนวน 102.50 | HTTP 200; จำนวน 102.50 | ผ่าน |
| BE-026 | `PUT /api/v1/biddings/{id}` | เจ้าของแก้ bidding หลังมี bid แล้ว | HTTP 409 | HTTP 409 | ผ่าน |
| BE-027 | `POST /api/v1/biddings/{id}/comments` | บันทึกข้อความลักษณะคล้าย SQL injection (`' OR '1'='1 --`) | HTTP 201; เก็บเป็นข้อความธรรมดา | HTTP 201; response คืนข้อความเดิมตรงกัน | ผ่าน |
| BE-028 | `POST /api/v1/biddings/{id}/comments/{commentId}/like` | ผู้ใช้อื่นกด like comment | HTTP 200; thumbs-up เป็น 1 | HTTP 200; thumbs-up เป็น 1 | ผ่าน |
| BE-029 | `PUT /api/v1/biddings/{id}/comments/{commentId}` | เจ้าของ comment แก้ข้อความ | HTTP 200 และอ่านข้อความใหม่ได้ | HTTP 200; อ่านข้อความที่แก้แล้วได้ | ผ่าน |
| BE-030 | `DELETE /api/v1/biddings/{id}/comments/{commentId}` | เจ้าของลบ comment | HTTP 204; รายการหลังลบว่าง | HTTP 204; อ่านรายการอีกครั้งแล้วว่าง | ผ่าน |
| BE-043 | `POST /api/v1/biddings/{id}/comments/{commentId}/dislike` | ผู้ใช้อื่นกด dislike | HTTP 200; thumbs-down เป็น 1 | HTTP 200; thumbs-down เป็น 1 | ผ่าน |
| BE-044 | `GET /api/v1/users/me/bids`, `/api/v1/biddings/mine`, `/api/v1/biddings/won` | อ่านรายการ bid/auction ของตน | HTTP 200 และรูปแบบรายการถูกต้อง | ทั้งหมด HTTP 200; ตรวจข้อมูล bidder และ owner | ผ่าน |

### Admin API (ใช้บัญชีทดสอบ)

| รหัส | Endpoint/การทำงาน | ข้อมูล/การทดสอบ | ผลที่คาดหวัง | ผลที่ได้ | สถานะ |
|---|---|---|---|---|---|
| BE-048 | `GET /api/v1/admin/users`, `POST /admin/users/{id}/promote` และเปลี่ยนสถานะบัญชี | `SUPER_ADMIN` ดูรายชื่อ, เลื่อนผู้ใช้ทั่วไปเป็น `ADMIN` แล้วปิดบัญชี; `ADMIN` ทั่วไปเลื่อนผู้ใช้ไม่ได้ | ปุ่มเลื่อน role แสดงเฉพาะ `SUPER_ADMIN`; เลื่อนแล้ว role เป็น `ADMIN`; `ADMIN` ได้ HTTP 403; บัญชีที่ปิด login ไม่ได้ | `SUPER_ADMIN` เลื่อนและปิดบัญชีได้; `ADMIN` ไม่เห็นปุ่มและ POST ถูกปฏิเสธ; บัญชีที่ปิดได้ HTTP 401 เมื่อลอง login | ผ่าน |
| BE-049 | Admin bidding และ payment | `SUPER_ADMIN` ยกเลิก auction, ปิด auction ที่มีผู้ชนะ, ดู/ยกเลิก payment และเปิดหน้าจัดการ payment; `ADMIN` ดู payment ได้ | HTTP 200; สถานะและเหตุผลการยกเลิกถูกบันทึก; ทั้งสอง role ใช้ payment management ได้ | ยกเลิก/ปิด auction สำเร็จ; เกิด payment `AWAITING_PAYMENT`; ทั้งสอง role ดู payment ได้; `SUPER_ADMIN` ยกเลิก payment และเปิดหน้าเว็บได้ | ผ่าน |

### กฎ bidding ระดับ service

| รหัส | สถานการณ์ | ผลที่คาดหวัง | ผลที่ได้ | สถานะ |
|---|---|---|---|---|
| BE-031 | รับ bid ตาม increment ของ seller | รับ bid และอัปเดต last bid | ผ่านที่จำนวน 102.50 | ผ่าน |
| BE-032 | bid ต่ำกว่า increment | HTTP 409 และไม่บันทึก bid | HTTP 409; ไม่เรียก repository save | ผ่าน |
| BE-033 | bid แรกเท่ากับราคาเริ่มต้น | รับ bid และอัปเดต last bid | ผ่านที่จำนวน 50.00 | ผ่าน |
| BE-034 | เจ้าของ auction พยายาม bid | HTTP 409 และไม่บันทึก bid | HTTP 409; ไม่เรียก repository save | ผ่าน |
| BE-035 | สถานะ auction ไม่รับ bid | HTTP 409 และไม่บันทึก bid | HTTP 409; ไม่เรียก repository save | ผ่าน |

### ตรวจหน้าเว็บและ deployment

| รหัส | URL/หน้า | การทดสอบ | ผลที่คาดหวัง | ผลที่ได้ | สถานะ |
|---|---|---|---|---|---|
| BE-036 | `https://auction-system-68vk.onrender.com/` | เปิดหน้าแรกใน browser | หน้า auction แสดงผล | หน้า “Live auctions” และรายการแสดงหลัง Render cold start | ผ่าน |
| BE-037 | `https://auction-system-68vk.onrender.com/swagger-ui/index.html` | เปิด Swagger UI | Swagger UI แสดงผล | หน้า Swagger UI โหลดสำเร็จ | ผ่าน |
| BE-045 | Local Thymeleaf browser flow | สมัคร, login, แก้ profile แล้ว logout | แต่ละหน้า/การทำงานสำเร็จและแสดงชื่อใหม่ | Playwright browser flow ผ่าน; logout กลับ `/login?logout` | ผ่าน |

Browser flow ในเครื่องทดสอบด้วย Playwright แบบโต้ตอบ ยังไม่ใช่ Playwright test suite ที่บันทึกใน repository และรันซ้ำอัตโนมัติได้

## 3. สรุปผลทดสอบ

| กลุ่ม test | จำนวน | ผ่าน | ไม่ผ่าน | ข้าม |
|---|---:|---:|---:|---:|
| Integration: user/authentication/authorization | 4 | 4 | 0 | 0 |
| Integration: artwork | 2 | 2 | 0 | 0 |
| Integration: seller profile | 1 | 1 | 0 | 0 |
| Integration: bidding/comment | 2 | 2 | 0 | 0 |
| Integration: admin API | 3 | 3 | 0 | 0 |
| Integration: Thymeleaf page | 2 | 2 | 0 | 0 |
| Spring Boot application context | 1 | 1 | 0 | 0 |
| Unit test: bidding service | 5 | 5 | 0 | 0 |
| **รวม automated test (`mvn verify`)** | **20** | **20** | **0** | **0** |
| ตรวจหน้า home และ Swagger ใน browser | 2 | 2 | 0 | 0 |
| Local Playwright flow (สมัคร, login, แก้ profile, logout) | 1 | 1 | 0 | 0 |

ดูขั้นตอนรันซ้ำ เกณฑ์ PASS/FAIL และกรณีทดสอบที่ยังเหลือใน [คู่มือทดสอบ](./TESTING-GUIDE.md)

## 4. การรันทดสอบ

รัน full verification วันที่ 10 ตุลาคม 2026 ด้วย PostgreSQL 16 container ใหม่ที่แยกสำหรับทดสอบ ใช้ฐาน `auction_test` ที่ port 5433 ไม่ได้ใช้ volume ของ PostgreSQL อื่น

```powershell
Set-Location .\code
$env:DB_URL="jdbc:postgresql://localhost:5433/auction_test"
$env:DB_USERNAME="auction_test"
$env:DB_PASSWORD="auction_test_local_only"
$env:REMEMBER_ME_KEY="ci-only-remember-me-key"
.\mvnw.cmd --batch-mode --no-transfer-progress verify
```

ผลล่าสุดหลังแก้ CSRF, auto-login, สิทธิ์ admin และหน้าเลื่อน role: `Tests run: 20, Failures: 0, Errors: 0, Skipped: 0`; `BUILD SUCCESS`, exit code 0

ผล Surefire แบบ XML/text อยู่ที่ `code/target/surefire-reports/`

ทดสอบ browser ในเครื่องด้วย Playwright 1.64.0 และ Chromium โดยใช้ PostgreSQL disposable เดียวกัน หลังเปิด CSRF แล้ว flow สมัคร/login/แก้ profile/logout ผ่าน หลังแก้ auto-login การสมัครใหม่กลับหน้า `/` และแสดงเมนูที่ต้อง login เช่น Profile และ Log out โดยไม่ต้อง login ซ้ำ ทั้งหมดทดสอบแบบโต้ตอบ ยังไม่มี Playwright spec ที่ commit ไว้ นอกจากนี้ console แจ้ง DNS failure สำหรับ URL รูป `example.test` ที่ใช้เป็นข้อมูลจำลองของ API แต่ไม่ขัดขวางการแสดงหน้าหรือ flow ของผู้ใช้

## 5. Security review และขอบเขตที่ยังไม่ครอบคลุม

มีการตรวจ source code แบบอ่านอย่างเดียวในส่วน authentication/authorization, endpoint ที่แก้ข้อมูล, request validation, JPA repository/query, template ที่เกี่ยวข้อง และ config ของแอป/deployment

- **SQL injection:** ไม่พบช่องโหว่ SQL injection ในโค้ดที่ตรวจ repository ใช้ derived JPA query หรือ JPQL ที่ bind parameter ไม่พบการประกอบ SQL จาก input โดยตรง และ test ยืนยันว่าข้อความ comment ที่มีรูปแบบคล้าย injection ถูกเก็บเป็นข้อความตามตัวอักษร ผลนี้ไม่ใช่ penetration test หรือหลักฐานว่าทุกเส้นทางปลอดภัยสมบูรณ์
- **แก้ CSRF finding แล้ว:** `SecurityConfig` เปิด Spring Security CSRF protection ทั้ง API และ browser มี regression test ยืนยันว่า state-changing API ที่ไม่มี token ได้ HTTP 403 และไม่เปลี่ยน profile; browser flow สมัคร/login/แก้ profile/logout ผ่านเมื่อเปิด CSRF API client ต้องเก็บ session cookie และส่ง CSRF token เช่นอ่าน hidden `_csrf` จาก `/register` แล้วส่งใน `X-CSRF-TOKEN` สำหรับ write request

ขอบเขต test กว้างขึ้น แต่ยังไม่ครอบคลุมทุก endpoint/service/page Admin integration test สร้างบัญชีเฉพาะทดสอบใน PostgreSQL disposable ไม่ใช้บัญชี production ครอบคลุมการดู/ปิดบัญชีผู้ใช้, `SUPER_ADMIN` เลื่อนเป็น `ADMIN` (และปฏิเสธ `ADMIN` ทั่วไป), ยกเลิก/ปิด auction และดู/ยกเลิก payment โดยทั้งสอง role ส่วนที่ยังต้องทดสอบ:

- Buyer ส่ง payment, seller ยืนยัน/ปฏิเสธ/จัดส่ง, admin ดูรายละเอียด/กรอง payment และเปิดใช้งานผู้ใช้อีกครั้ง
- Admin moderation และการ void bid
- Seller rating และ edge case ของ rating/permission
- การยกเลิก/ปิด bidding, ขอบเขตเวลา และวันที่ไม่ถูกต้อง
- API error, ownership, validation และ unit test สำหรับ service อื่นนอกเหนือจาก bidding
- หน้า Thymeleaf ที่ต้อง login และ browser flow ส่วนใหญ่ เช่น auction, bid, comment, seller settings, purchases/sales และหน้า admin
- Playwright spec ที่บันทึกไว้และรันซ้ำอัตโนมัติ
- JaCoCo line/branch coverage (ยังไม่ได้ติดตั้งหรือวัด)

## 6. บันทึก defect

ไม่พบ failure ใน automated test 20 รายการหรือ browser check 3 รายการ ส่วนที่ยังไม่ได้ทดสอบข้างต้นเป็น coverage gap ไม่ใช่ defect ที่ยืนยันแล้ว CSRF finding จาก source review ได้แก้และเพิ่ม regression test แล้ว รายงานนี้ไม่ใช่ penetration test หรือ security certification

## 7. สรุป

Full automated Maven suite ผ่าน: 20 test, 0 failure MockMvc integration test ครอบคลุมสมัคร/authentication รวม web sign-up auto-login, CSRF, แก้ user/seller profile, artwork CRUD และ access control, bidding/comment, ข้อความ comment รูปแบบคล้าย SQL injection, admin ดู/ปิด/เลื่อนบัญชี, ยกเลิก/ปิด auction, ดู/ยกเลิก payment ด้วยทั้งสอง admin role และ render หน้า Thymeleaf บางส่วนบน PostgreSQL

Browser check ด้วยตนเองยืนยัน flow สมัคร/login/แก้ profile/logout ในเครื่องเมื่อเปิด CSRF รวมถึงหน้า home และ Swagger UI บน Render ส่วน payment flow ฝั่ง buyer/seller, UI อื่น, Playwright spec ที่รันซ้ำได้ และ code coverage ยังต้องทำต่อ ไม่พบ SQL injection จากโค้ดที่ตรวจ และได้แก้ CSRF finding พร้อมทดสอบยืนยันแล้ว
