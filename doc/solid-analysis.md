# SOLID Analysis

## สรุปภาพรวม

| หลักการ | ตัวอย่างหลักในระบบ |
|---|---|
| **S**RP | `GlobalExceptionHandler`, `ArtworkMapper`, `AdminInitializer`, `ArtworkController`, `ArtworkServiceImpl` |
| **O**CP | `BiddingStateResolver` และ `PaymentStateResolver` + คลาส State (เพิ่มสถานะใหม่โดยไม่แก้ Service) |
| **L**SP | `ActiveBiddingState`, `ClosedBiddingState`, `CancelledBiddingState` แทนที่ `BiddingState` ได้ และชุด `PaymentState` ทั้ง 6 คลาส |
| **I**SP | Interface ของ Service แยกตามบทบาท (`BidActionService`, `AdminBiddingService`, `AdminModerationService`, `PaymentService`, `AdminPaymentService`) |
| **D**IP | Controller และ Service พึ่งพา Interface ผ่าน constructor injection |

---

# รายละเอียดแต่ละหลักการ

path ทุกไฟล์อยู่ใต้ `code/src/main/java/com/example/project/` (เลขบรรทัดตรวจกับโค้ดใน `develop` ล่าสุด)

## S - Single Responsibility (หนึ่งคลาสทำหน้าที่เดียว)

- `exception/GlobalExceptionHandler.java` บรรทัด 22-90: แปลง exception เป็น response รูปแบบ `ErrorResponse` อย่างเดียว
- `mapper/ArtworkMapper.java` บรรทัด 12-23: แปลง `Artwork` เป็น `ArtworkResponse` อย่างเดียว
- `config/AdminInitializer.java` บรรทัด 30-39: สร้างบัญชี admin ตอนแอปเริ่มทำงานอย่างเดียว
- `controller/api/ArtworkController.java` บรรทัด 48-59: รับ request แล้วส่งต่อให้ Service ไม่มี logic ธุรกิจ
- `service/implementation/ArtworkServiceImpl.java` บรรทัด 25 และ 128-129: กฎธุรกิจของผลงาน เช่น จำกัดฟิลด์ที่ใช้ sort ได้ ไม่ยุ่งกับ HTTP

เหตุผล: แยก Controller / Service / Repository แล้ว แก้ส่วนไหนก็ไม่กระทบส่วนอื่น

## O - Open/Closed (เพิ่มได้ ไม่ต้องแก้ของเดิม)

- `service/state/BiddingStateResolver.java` บรรทัด 16-20: รับ `List<BiddingState>` ที่ Spring รวบรวมมาให้ แล้วเก็บเป็น Map ตามสถานะ
- `service/implementation/BiddingServiceImpl.java` บรรทัด 237 (`acceptsBids()` ตอน `placeBid`) และบรรทัด 165 (`canMoveTo()` ตอนยกเลิก) กับ `AdminBiddingServiceImpl.java` บรรทัด 51: เรียกผ่าน resolver ไม่มี if/switch เช็กชื่อสถานะ
- `service/state/PaymentStateResolver.java` บรรทัด 16-20: รูปแบบเดียวกันสำหรับสถานะการชำระเงิน ใช้ที่ `PaymentServiceImpl.java` บรรทัด 207 และ `AdminPaymentServiceImpl.java` บรรทัด 68

เหตุผล: ถ้าจะเพิ่มสถานะใหม่ แค่สร้างคลาสที่ implement `BiddingState` (หรือ `PaymentState`) โดยไม่ต้องแก้ Service (ต้องเพิ่มค่าใน enum `Bidding.Status` / `Payment.Status` ด้วย)

## L - Liskov Substitution (ใช้คลาสลูกแทนคลาสแม่ได้)

- `service/state/BiddingState.java` บรรทัด 5-11: กำหนดสัญญา `getStatus()`, `acceptsBids()`, `canMoveTo()`
- `ActiveBiddingState.java`, `ClosedBiddingState.java`, `CancelledBiddingState.java` บรรทัด 15-23 ของแต่ละไฟล์: implement ครบและคืนค่า boolean ตามสัญญา
- `service/implementation/AdminBidActionServiceImpl.java` บรรทัด 77: เรียกผ่าน interface โดยไม่ต้องเช็กว่าเป็นสถานะไหน
- `service/state/PaymentState.java` บรรทัด 5-9 และ `AwaitingPaymentState.java`, `PaidPaymentState.java` บรรทัด 15-18 ของแต่ละไฟล์: ชุดสถานะการชำระเงินทำตามสัญญา `canMoveTo()` เดียวกัน

เหตุผล: ทุกคลาสไม่โยน exception เพิ่มและตอบตามสัญญาเดียวกัน สลับใช้แทนกันได้

## I - Interface Segregation (interface เล็ก แยกตามบทบาท)

- `service/BiddingService.java` บรรทัด 14-31: งานประมูลของผู้ใช้ทั่วไป
- `service/BidActionService.java` บรรทัด 10-17: อ่านประวัติ bid อย่างเดียว
- `service/AdminBiddingService.java` บรรทัด 5-9 และ `AdminModerationService.java` บรรทัด 3-7: งานของแอดมินแยกออกมา
- `service/PaymentService.java` บรรทัด 9-28 และ `AdminPaymentService.java` บรรทัด 8-14: งานชำระเงินของผู้ใช้กับของแอดมินแยกกัน
- `controller/api/AdminBiddingController.java` บรรทัด 19-25: ใช้แค่ `AdminBiddingService`

เหตุผล: ผู้ใช้ทั่วไปไม่ต้องพึ่ง method ของแอดมิน และแต่ละ Controller รู้จักเฉพาะ method ที่ใช้

## D - Dependency Inversion (พึ่ง interface ไม่พึ่งคลาสจริง)

- `controller/api/ArtworkController.java` บรรทัด 34-46 และ `BiddingController.java` บรรทัด 45-58: ถือ Service เป็น interface ผ่าน constructor
- `service/implementation/ArtworkServiceImpl.java` บรรทัด 27-36: ถือ Repository ซึ่งเป็น interface ของ Spring Data JPA
- `config/AdminInitializer.java` บรรทัด 18-28: ถือ `AdminUserService` เป็น interface
- `controller/api/AdminPaymentController.java` บรรทัด 30-36: ถือ `AdminPaymentService` เป็น interface

เหตุผล: ตอนเขียน test ใช้ Mockito mock interface ได้เลย และเปลี่ยน implementation ทีหลังได้โดยไม่ต้องแก้ Controller