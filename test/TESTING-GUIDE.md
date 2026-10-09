# คู่มือทดสอบ Auction-System

เอกสารนี้อธิบายวิธีรันทดสอบ วิธีตัดสินผล PASS/FAIL และรายการที่ยังต้องทดสอบเพิ่มเติม

## สถานะล่าสุดที่ตรวจยืนยันแล้ว

- Automated test: **20 ผ่าน, 0 ล้มเหลว, 0 error, 0 ข้าม**
- Build: **`BUILD SUCCESS`**, exit code `0`
- Database: PostgreSQL 16 แบบ disposable สำหรับทดสอบ ไม่ใช่ฐานข้อมูล production
- ทดสอบแล้ว: สมัครและยืนยันตัวตนบางกรณี, CSRF, เลื่อนผู้ใช้เป็น `ADMIN` โดย `SUPER_ADMIN`, ปฏิเสธการเลื่อนโดย `ADMIN`, จัดการสถานะผู้ใช้, ยกเลิก/ปิด auction, ดู payment โดย `ADMIN` และ `SUPER_ADMIN`, จัดการ artwork และ seller profile, bid/comment, กฎการประมูล, หน้า Thymeleaf บางส่วน และ login อัตโนมัติหลังสมัครผ่านหน้าเว็บ
- ยังไม่ครอบคลุมทั้งหมด: วงจร payment ฝั่ง buyer/seller, admin endpoint และ transition บางรายการ, service/UI ทุกกรณี, เปอร์เซ็นต์ test coverage และ browser E2E suite ที่รันซ้ำได้

## 1. รัน automated test ทั้งชุด

ใช้ฐานข้อมูลทดสอบที่ลบและสร้างใหม่ได้เท่านั้น ห้ามรัน test กับฐานข้อมูล Neon production เพราะ test จะสร้างข้อมูลใหม่

### เปิด PostgreSQL ด้วย Docker

เปิด Docker Desktop แล้วรันคำสั่งต่อไปนี้ใน PowerShell:

```powershell
docker run --rm -d --name auction-test-db `
  -e POSTGRES_DB=auction_test `
  -e POSTGRES_USER=auction_test `
  -e POSTGRES_PASSWORD=auction_test_local_only `
  -p 5433:5432 postgres:16

docker exec auction-test-db pg_isready -U auction_test -d auction_test
```

รอจน `pg_isready` แจ้งว่า server รับการเชื่อมต่อแล้ว

### รันทดสอบ

```powershell
Set-Location 'D:\Coding project\Project\Auction-System\code'
$env:DB_URL='jdbc:postgresql://localhost:5433/auction_test'
$env:DB_USERNAME='auction_test'
$env:DB_PASSWORD='auction_test_local_only'
$env:REMEMBER_ME_KEY='ci-only-remember-me-key'
.\mvnw.cmd --batch-mode --no-transfer-progress verify
$testExitCode = $LASTEXITCODE
"Test command exit code: $testExitCode"
```

หลังทดสอบเสร็จให้ปิดฐานข้อมูลชั่วคราว:

```powershell
docker stop auction-test-db
```

### วิธีตัดสิน PASS หรือ FAIL

ถือว่า **PASS** เมื่อครบทุกข้อ:

1. คำสั่ง Maven มี exit code เป็น `0`
2. ผลลัพธ์ลงท้ายด้วย `BUILD SUCCESS`
3. จำนวน failure, error และ skipped เป็น `0`
4. จำนวน test ตรงกับไฟล์รายงาน Surefire

ถือว่า **FAIL** หาก exit code ไม่ใช่ `0`, Maven แสดง `BUILD FAILURE` หรือมี test ล้มเหลว/เกิด error แม้บาง test จะผ่านก็ตาม ให้เปิด stack trace ของ test แรกที่ล้มเหลวใน `code/target/surefire-reports/` แก้สาเหตุ แล้วรัน `verify` ซ้ำ

หากต้องการรัน test class เดียว ให้ใช้ environment variables เดิมและเปลี่ยนคำสั่งสุดท้าย เช่น:

```powershell
.\mvnw.cmd --batch-mode -Dtest=PageControllerIntegrationTest test
```

test class อื่นที่เลือกแยกรันได้ ได้แก่ `AdminApiIntegrationTest`, `UserApiIntegrationTest`, `ArtworkApiIntegrationTest`, `SellerprofileApiIntegrationTest`, `BiddingApiIntegrationTest` และ `BiddingServiceImplTest`

### เลื่อนบัญชีผู้ใช้เป็น admin

1. ให้บุคคลนั้นสมัครบัญชีตามปกติ และให้ `SUPER_ADMIN` login เข้าระบบ
2. เปิด **Admin → Users** แล้วค้นหาบัญชีที่ต้องการ
3. กด **Promote to Admin** ที่แถวบัญชีซึ่งมี role เป็น `USER`
4. **PASS:** ระบบแจ้งว่าสำเร็จ และ role เปลี่ยนเป็น `ADMIN` ปุ่มนี้แสดงให้ `SUPER_ADMIN` เท่านั้น; `ADMIN` ทั่วไปไม่มีสิทธิ์เลื่อนผู้ใช้
5. ให้ผู้ใช้ที่ถูกเลื่อนออกจากระบบแล้ว login ใหม่ เพื่อให้ session โหลด role ใหม่

## 2. ตรวจหน้าเว็บด้วยตนเอง

ทำกับ local หรือ staging เท่านั้น ใช้ email ทดสอบและรหัสผ่านจำลอง ห้ามใช้ข้อมูล payment จริงหรือบัญชี production

### สมัครแล้ว login อัตโนมัติ

1. เปิด `/register`
2. สมัครบัญชีทดสอบใหม่ โดยกรอกรหัสผ่านและยืนยันรหัสผ่านให้ตรงกัน
3. **PASS:** ระบบกลับไปหน้า `/`, แสดงเมนูสำหรับผู้ใช้ที่ login แล้ว เช่น Profile และ Log out และเปิด `/profile` ได้โดยไม่ต้อง login ซ้ำ
4. **FAIL:** กลับไปหน้า login, ยังเป็นผู้ใช้ที่ไม่ได้ login หรือไม่มีบัญชีถูกบันทึก

### Login และแก้ไข profile

1. Log out แล้ว login ด้วยบัญชีทดสอบ
2. แก้ชื่อใน profile แล้วบันทึก
3. เปลี่ยนรหัสผ่านด้วยรหัสผ่านเดิม จากนั้น log out แล้ว login ด้วยรหัสผ่านใหม่
4. **PASS:** มีข้อความ/redirect แจ้งสำเร็จ, reload แล้วยังเห็น profile ที่แก้, รหัสผ่านใหม่ใช้ได้ และรหัสผ่านเดิมใช้ไม่ได้
5. **FAIL:** ระบบแจ้งสำเร็จแต่ reload แล้วข้อมูลหาย หรือผลการเปลี่ยนรหัสผ่านไม่ตรงตามที่คาด

### Auction, bid และ comment

1. ใช้ seller test account ที่มี seller profile สร้าง artwork และ auction ที่กำหนดวันเวลาในอนาคตอย่างถูกต้อง
2. ใช้ buyer อีกบัญชีเปิด auction แล้ววาง bid แรกเท่ากับราคาเริ่มต้น จากนั้นวาง bid ที่สูงขึ้นตาม increment
3. เพิ่ม comment แล้วลองกด like/dislike
4. Reload หน้ารายละเอียดแล้วตรวจ auction, bid สูงสุด, comment และ reaction ที่บันทึกไว้
5. **PASS:** ข้อมูลยังอยู่หลัง reload และ bid ที่ผิดกฎ/เพิ่มไม่ถึงขั้นต่ำไม่ถูกบันทึกในประวัติ
6. **FAIL:** bid ถูกระบุว่าเป็นของผู้ใช้อื่น, bid ที่ผิดกฎถูกบันทึก หรือข้อมูลหลัง reload ไม่ตรงกับผลตอบกลับ

API flow หลักของ bid/comment มี automated test แล้ว แต่หน้าจอ browser จริงยังไม่มี browser test suite ที่รันอัตโนมัติซ้ำได้

## 3. API และ business flow ที่ยังต้องทดสอบ

ใช้บัญชี local/staging และ API ตามเอกสาร Swagger โดยยึด request schema และ status ใน Swagger กับ `doc/api-spec.md` ให้บันทึกค่าที่ส่งและ response จริง ทุก API ที่เปลี่ยนข้อมูลต้องส่ง CSRF token และใช้ session cookie เดิม

| ความสำคัญ | ส่วนที่ยังต้องทดสอบ | สิ่งที่ให้ทดลอง | หลักฐาน PASS |
|---|---|---|---|
| สูง | Payment | Buyer ส่งหลักฐานชำระเงิน; seller ยืนยันหรือปฏิเสธ; seller ส่งสินค้าและข้อมูล tracking; buyer/seller ดูรายการและรายละเอียด | แต่ละ transition ที่อนุญาตได้ status ตาม API; role ที่ไม่มีสิทธิ์ถูกปฏิเสธ; reload แล้วยังเห็นสถานะใหม่; transition ที่ผิดหรือทำซ้ำถูกปฏิเสธ |
| กลาง | Admin payment/user ที่เหลือ | ดูรายละเอียด/กรอง payment; เปิดใช้งาน user; ทดลองเรียก admin route โดย role ที่ไม่มีสิทธิ์ | การเปลี่ยนแปลงถูกบันทึกและผู้ไม่มีสิทธิ์ได้ HTTP 403 |
| กลาง | ขอบเขต admin auction | เจ้าของยกเลิก; ทดลอง admin cancel/close ซ้ำหรือไม่ถูกสถานะ; ตรวจผลต่อ bid/payment | เฉพาะผู้มีสิทธิ์ทำได้; อ่านซ้ำเห็นสถานะ; transition ที่ผิด/ทำซ้ำถูกปฏิเสธ |
| กลาง | Bid moderation | Admin ดู bid และ void bid ทดสอบพร้อมเหตุผล | ผู้ไม่มีสิทธิ์ถูกปฏิเสธ; มีการบันทึกเหตุผล/ผู้ดำเนินการ; bid และ bid สูงสุดสอดคล้องกัน |
| กลาง | Seller rating | ผู้ชนะให้คะแนน seller; ลองคะแนนผิด, ผู้ไม่ชนะ, ให้ซ้ำ และ seller ที่ไม่มีอยู่ | คะแนนถูกต้องถูกบันทึก; คำขอที่ผิด/ไม่มีสิทธิ์/ซ้ำถูกปฏิเสธตาม API contract |
| กลาง | Permission และ not found | ลองเปิด profile/artwork/comment/payment ของคนอื่น และ ID ที่ไม่มี | ไม่เปิดเผยข้อมูลส่วนตัว; ได้ 403/404 ตามกรณี และข้อมูลไม่ถูกเปลี่ยน |
| กลาง | ขอบเขตวันเวลาและตัวเลข | Auction เริ่มในอนาคต, ถึงเวลาเริ่ม/จบ, หมดเวลาแล้ว; จำนวนเงินศูนย์/ติดลบ; วันเวลาเรียงผิด | ผลตรงกับ business rule; คำขอที่ไม่ผ่าน validation ไม่ถูกบันทึก |
| กลาง | Error handling และ validation | ขาด field, JSON ผิดรูปแบบ, enum/page/sort ไม่ถูกต้อง | ได้ 4xx ที่อธิบายปัญหาโดยไม่เปิด stack trace, secret หรือรายละเอียด SQL ภายใน |

`AdminApiIntegrationTest` สร้าง `SUPER_ADMIN` และ `ADMIN` สำหรับ test โดยเฉพาะในฐานข้อมูล disposable ไม่ต้องใช้ credential จริงหรือบัญชี production สำหรับ automated test เหล่านั้น การทดลอง payment ด้วยตนเองให้ใช้ auction และข้อมูลทดสอบเท่านั้น ห้ามโอนเงินจริงหรืออัปโหลดสลิปจริง และห้ามเลื่อน role ใน production เพื่อทดสอบ

## 4. ตรวจ CSRF และ API client

ระบบเปิด CSRF protection สำหรับ write request รวมถึง `/api/v1/**` การเปิด Swagger UI ได้ไม่ได้ยืนยันว่า write operation ใช้งานได้

- Browser form ควรแนบ CSRF token ให้อัตโนมัติ
- API client ต้องเก็บ session cookie, อ่าน token จาก hidden `_csrf` ใน HTML ของ `/register` และส่ง token ใน `X-CSRF-TOKEN` พร้อม cookie เดิมเมื่อเรียก POST/PUT/PATCH/DELETE
- **PASS:** write request ที่ login และมี token ทำงานตาม API contract; request เดียวกันเมื่อไม่มี token ได้ `403` และข้อมูลไม่เปลี่ยน
- **FAIL:** write request ที่ไม่มี token ยังแก้ข้อมูลได้

การปฏิเสธ request ที่ไม่มี token มี regression test แล้ว แต่ยังไม่ได้ยืนยัน flow ผ่าน Swagger ด้วย client ที่ใช้งานจริง

## 5. SQL injection และ security testing

การตรวจ source code ไม่พบ SQL ที่นำ input มาต่อ string โดยตรง; persistence ใช้ Spring Data JPA derived query หรือ JPQL ที่ bind parameter นอกจากนี้ยังทดสอบข้อความ comment ที่มีรูปแบบคล้าย SQL injection ว่าถูกบันทึกเป็นข้อความธรรมดา อย่างไรก็ตาม ยังสรุปไม่ได้ว่าทุก input ปลอดภัยทั้งหมด

หากต้องการทดสอบเพิ่มเติม:

1. ใช้ฐานข้อมูล local ที่ทิ้งได้ หรือ staging ที่ได้รับอนุญาตชัดเจนเท่านั้น
2. เริ่มจาก OWASP ZAP แบบ passive/baseline scan
3. ใช้ active scan เมื่อได้รับอนุญาตและมีข้อมูลทดสอบ เพราะอาจสร้าง/เปลี่ยนข้อมูลและเพิ่ม load
4. ตรวจ alert ทีละรายการและจด scanner/version, URL/role, หลักฐาน, severity และว่าสามารถทำซ้ำได้หรือไม่
5. **PASS:** ไม่พบ injection, authentication bypass หรือข้อมูลเปลี่ยนโดยไม่คาดหมาย; ตรวจสอบและบันทึก alert ระดับสูง/กลางทุกข้อก่อนสรุปว่าปลอดภัย

ห้ามยิง payload SQL injection หรือสั่ง active scan กับ production โดยไม่มีสิทธิ์อนุญาตชัดเจน และห้ามใช้ payload ที่ทำลายข้อมูล

## 6. สิ่งที่ควรทำต่อ

1. รัน `mvn verify` ซ้ำตามขั้นตอนฐานข้อมูล disposable ด้านบน และเก็บ output ไว้
2. ทดสอบวงจร payment buyer/seller ด้วยบัญชีทดสอบ
3. ทดสอบ admin endpoint/transition ที่เหลือ, seller rating, permission และขอบเขตวันเวลาตามตาราง
4. ทดสอบหน้าประมูลและจัดการบัญชีผ่าน browser รวมถึงกรณีกรอกข้อมูลผิด
5. หากรายวิชากำหนด security scan ให้ใช้ OWASP ZAP กับ local/staging แล้วเพิ่มเฉพาะผลที่ตรวจยืนยันแล้วลงรายงาน
6. บันทึกแต่ละกรณีเป็น **ผ่าน**, **ไม่ผ่าน** หรือ **ยังไม่ได้ทดสอบ** พร้อมวันที่, ข้อมูลทดสอบที่ไม่ใช่ secret, ผลที่คาดหวัง, ผลจริง และหลักฐาน ห้ามระบุกรณีที่ยังไม่ได้ทดสอบว่าผ่าน

ผล automated test เป็นหลักฐานเฉพาะกรณีที่ระบุไว้ ไม่ใช่หลักฐานว่าทุก service, endpoint, browser page หรือ security property ผ่านการทดสอบทั้งหมดแล้ว
