# SOLID Analysis

## สรุปภาพรวม

| หลักการ | ตัวอย่างหลักในระบบ |
|---|---|
| **S**RP | `GlobalExceptionHandler`, `ArtworkMapper`, `AdminInitializer`, `ArtworkController`, `ArtworkServiceImpl` |
| **O**CP | `BiddingStateResolver` + คลาส State (เพิ่มสถานะใหม่โดยไม่แก้ Service) |
| **L**SP | `ActiveBiddingState`, `ClosedBiddingState`, `CancelledBiddingState` แทนที่ `BiddingState` ได้ |
| **I**SP | Interface ของ Service แยกตามบทบาท (`BidActionService`, `AdminBiddingService`, `AdminModerationService`) |
| **D**IP | Controller และ Service พึ่งพา Interface ผ่าน constructor injection |

---

# SOLID Analysis

path ทุกไฟล์อยู่ใต้ `code/src/main/java/com/example/project/`

## S - Single Responsibility (หนึ่งคลาสทำหน้าที่เดียว)

- `exception/GlobalExceptionHandler.java` บรรทัด 20-71: แปลง exception เป็น response รูปแบบ `ErrorResponse` อย่างเดียว
- `mapper/ArtworkMapper.java` บรรทัด 12-23: แปลง `Artwork` เป็น `ArtworkResponse` อย่างเดียว
- `config/AdminInitializer.java` บรรทัด 13-38: สร้างบัญชี admin ตอนแอปเริ่มทำงานอย่างเดียว
- `controller/api/ArtworkController.java` บรรทัด 42-50: รับ request แล้วส่งต่อให้ Service ไม่มี logic ธุรกิจ

เหตุผล: แยก Controller / Service / Repository แล้ว แก้ส่วนไหนก็ไม่กระทบส่วนอื่น

## O - Open/Closed (เพิ่มได้ ไม่ต้องแก้ของเดิม)

- `service/state/BiddingStateResolver.java` บรรทัด 16-20: รับ `List<BiddingState>` ที่ Spring รวบรวมมาให้ แล้วเก็บเป็น Map ตามสถานะ
- `service/implementation/BiddingServiceImpl.java` บรรทัด 90 และ `AdminBiddingServiceImpl.java` บรรทัด 41: เรียก `acceptsBids()` / `canMoveTo()` ผ่าน resolver ไม่มี if/switch เช็กชื่อสถานะ

เหตุผล: ถ้าจะเพิ่มสถานะใหม่ แค่สร้างคลาสที่ implement `BiddingState` โดยไม่ต้องแก้ Service (ต้องเพิ่มค่าใน enum `Bidding.Status` ด้วย)

## L - Liskov Substitution (ใช้คลาสลูกแทนคลาสแม่ได้)

- `service/state/BiddingState.java` บรรทัด 5-11: กำหนดสัญญา `getStatus()`, `acceptsBids()`, `canMoveTo()`
- `ActiveBiddingState.java`, `ClosedBiddingState.java`, `CancelledBiddingState.java` บรรทัด 15-23 ของแต่ละไฟล์: implement ครบและคืนค่า boolean ตามสัญญา
- `service/implementation/AdminBidActionServiceImpl.java` บรรทัด 60: เรียกผ่าน interface โดยไม่ต้องเช็กว่าเป็นสถานะไหน

เหตุผล: ทั้งสามคลาสไม่โยน exception เพิ่มและตอบตามสัญญาเดียวกัน สลับใช้แทนกันได้

## I - Interface Segregation (interface เล็ก แยกตามบทบาท)

- `service/BiddingService.java` บรรทัด 10-18: งานประมูลของผู้ใช้ทั่วไป
- `service/BidActionService.java` บรรทัด 7-13: อ่านประวัติ bid อย่างเดียว
- `service/AdminBiddingService.java` บรรทัด 5-9 และ `AdminModerationService.java` บรรทัด 3-7: งานของแอดมินแยกออกมา
- `controller/api/AdminBiddingController.java` บรรทัด 19-24: ใช้แค่ `AdminBiddingService`

เหตุผล: ผู้ใช้ทั่วไปไม่ต้องพึ่ง method ของแอดมิน และแต่ละ Controller รู้จักเฉพาะ method ที่ใช้

## D - Dependency Inversion (พึ่ง interface ไม่พึ่งคลาสจริง)

- `controller/api/ArtworkController.java` บรรทัด 34-40 และ `BiddingController.java` บรรทัด 30-36: ถือ Service เป็น interface ผ่าน constructor
- `service/implementation/ArtworkServiceImpl.java` บรรทัด 21-27: ถือ Repository ซึ่งเป็น interface ของ Spring Data JPA
- `config/AdminInitializer.java` บรรทัด 17-27: ถือ `AdminUserService` เป็น interface

เหตุผล: ตอนเขียน test ใช้ Mockito mock interface ได้เลย และเปลี่ยน implementation ทีหลังได้โดยไม่ต้องแก้ Controller
