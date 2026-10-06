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

## 1. SRP — Single Responsibility Principle

### `exception/GlobalExceptionHandler.java` (20-77)
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(...) { ... }        // บรรทัด 25

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(...) { ... }            // บรรทัด 32

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(...) { ... }        // บรรทัด 40

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(...) { ... }          // บรรทัด 45

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(...) { ... }         // บรรทัด 50

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(...) { ... }        // บรรทัด 56

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(...) { ... }            // บรรทัด 61
}
```

### `mapper/ArtworkMapper.java` (12-23)
```java
public ArtworkResponse toResponse(Artwork artwork) {
    Sellerprofile seller = artwork.getSellerprofile();
    Long sellerprofileId = seller == null ? null : seller.getSellprofileId();
    Long sellerUserId = (seller == null || seller.getUser() == null) ? null : seller.getUser().getId();

    return new ArtworkResponse(
            artwork.getId(),
            artwork.getTitle(),
            artwork.getImageUrl(),
            sellerprofileId,
            sellerUserId);
}
```

### `config/AdminInitializer.java` (13-38)
```java
@Component
public class AdminInitializer implements ApplicationRunner {

    private final AdminUserService adminUserService;
    private final String adminEmail;
    private final String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD not set - skipping default admin creation");
            return;
        }
        boolean created = adminUserService.createAdminIfAbsent("Administrator", adminEmail, adminPassword);
        log.info(created ? "Default admin account created" : "Default admin account already exists");
    }
}
```

### `controller/api/ArtworkController.java` (42-50)
```java
@PostMapping
public ResponseEntity<ArtworkResponse> createArtwork(@Valid @RequestBody CreateArtworkRequest request) {
    Artwork artwork = artworkService.createArtwork(
            request.sellerUserId(),
            request.title(),
            request.imageUrl());

    return ResponseEntity.status(HttpStatus.CREATED).body(artworkMapper.toResponse(artwork));
}
```

### `service/implementation/ArtworkServiceImpl.java` (29-42)
```java
@Override
@Transactional
public Artwork createArtwork(Long sellerUserID, String title, String imageUrl) {
    Sellerprofile seller = sellerprofileRepository.findByUser_Id(sellerUserID)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Seller profile not found for user: " + sellerUserID));

    Artwork artwork = new Artwork();
    artwork.setTitle(title);
    artwork.setImageUrl(imageUrl);
    artwork.setSellerprofile(seller);

    return artworkRepository.save(artwork);
}
```

**เหตุผล:** การแยกชั้น Controller → Service → Repository ทำให้เปลี่ยนรูปแบบ response (แก้ Mapper), เปลี่ยนกฎธุรกิจ (แก้ Service) หรือเปลี่ยนรูปแบบ error (แก้ Handler) ได้ที่เดียว โดยไม่กระทบชั้นอื่น

---

## 2. OCP — Open/Closed Principle

### `service/state/BiddingStateResolver.java` (11-28)
```java
@Component
public class BiddingStateResolver {

    private final Map<Bidding.Status, BiddingState> states = new EnumMap<>(Bidding.Status.class);

    public BiddingStateResolver(List<BiddingState> stateList) {
        for (BiddingState state : stateList) {
            states.put(state.getStatus(), state);
        }
    }

    public BiddingState resolve(Bidding.Status status) {
        BiddingState state = states.get(status);
        if (state == null) {
            throw new IllegalStateException("No state registered for " + status);
        }
        return state;
    }
}
```

### `service/implementation/BiddingServiceImpl.java` (90)
```java
if (!stateResolver.resolve(bidding.getStatus()).acceptsBids()) {
    throw new ResponseStatusException(HttpStatus.CONFLICT,
            "This bidding is " + bidding.getStatus() + " and is not accepting bids.");
}
```

### `service/implementation/AdminBiddingServiceImpl.java` (41)
```java
if (!stateResolver.resolve(bidding.getStatus()).canMoveTo(target)) {
    throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Cannot change bidding from " + bidding.getStatus() + " to " + target);
}
```

### `exception/GlobalExceptionHandler.java` (25-59)
```java
@ExceptionHandler(ResponseStatusException.class)
public ResponseEntity<ErrorResponse> handleResponseStatus(...) { ... }

@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(...) { ... }

// เพิ่ม Exception ชนิดใหม่ = เพิ่ม method @ExceptionHandler ใหม่ ไม่แก้ของเดิม
```

**เหตุผล:** ถ้าต้องเพิ่มสถานะประมูลใหม่ (เช่น `PAUSED`) ให้สร้างคลาสใหม่ที่ implement `BiddingState` และใส่ `@Component` โดย `BiddingStateResolver`, `BiddingServiceImpl` และ `AdminBiddingServiceImpl` ไม่ต้องแก้ จึงปิดต่อการแก้ไขและเปิดต่อการขยาย

---

## 3. LSP — Liskov Substitution Principle

### `service/state/BiddingState.java` (5-11)
```java
public interface BiddingState {
    Bidding.Status getStatus();
    boolean acceptsBids();
    boolean canMoveTo(Bidding.Status target);
}
```

### `service/state/ActiveBiddingState.java` (8-23)
```java
@Component
public class ActiveBiddingState implements BiddingState {
    @Override
    public Bidding.Status getStatus() { return Bidding.Status.ACTIVE; }

    @Override
    public boolean acceptsBids() { return true; }

    @Override
    public boolean canMoveTo(Bidding.Status target) {
        return target == Bidding.Status.CLOSED || target == Bidding.Status.CANCELLED;
    }
}
```

### `service/state/ClosedBiddingState.java` (8-23) และ `CancelledBiddingState.java` (8-23)
```java
@Component
public class ClosedBiddingState implements BiddingState {
    @Override
    public Bidding.Status getStatus() { return Bidding.Status.CLOSED; }

    @Override
    public boolean acceptsBids() { return false; }

    @Override
    public boolean canMoveTo(Bidding.Status target) { return false; }
}
// CancelledBiddingState มีรูปแบบเดียวกันทุกประการ (getStatus() คืน CANCELLED)
```

### `service/implementation/AdminBidActionServiceImpl.java` (60)
```java
if (!stateResolver.resolve(bidding.getStatus()).acceptsBids()) {
    throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Bids can only be voided while the bidding is ACTIVE, but it is " + bidding.getStatus());
}
```

---

## 4. ISP — Interface Segregation Principle

### `service/BiddingService.java` (9-16)
```java
public interface BiddingService {
    Bidding createBidding(List<Long> artworkIDs, Long ownerID, Double startingPrice, Date startDate, Date endDate);
    Bidding getBiddingById(Long biddingID);
    List<Bidding> getAllBiddings();
    BidAction placeBid(Long biddingID, Long userID, Double amount);
}
```

### `service/BidActionService.java` (7-13)
```java
public interface BidActionService {
    List<BidAction> getBidsByBidding(Long biddingID);
    BidAction getHighestBid(Long biddingID);
    List<BidAction> getBidsByUser(Long userID);
}
```

### `service/AdminBiddingService.java` (5-9)
```java
public interface AdminBiddingService {
    Bidding cancelBidding(Long biddingId);
    Bidding closeBidding(Long biddingId);
}
```

### `service/AdminBidActionService.java` (7-11)
```java
public interface AdminBidActionService {
    List<BidAction> getAllBids(Long biddingId);
    BidAction voidBid(String actorEmail, Long bidId, String reason);
}
```

### `service/AdminModerationService.java` (3-7)
```java
public interface AdminModerationService {
    void deleteArtwork(Long artworkId);
    void deleteComment(Long commentId);
}
```

### `controller/api/AdminBiddingController.java` (16-24)
```java
@RestController
@RequestMapping("/api/v1/admin/biddings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBiddingController {

    private final AdminBiddingService adminBiddingService;
    private final BiddingMapper biddingMapper;

    public AdminBiddingController(AdminBiddingService adminBiddingService, BiddingMapper biddingMapper) {
        this.adminBiddingService = adminBiddingService;
        this.biddingMapper = biddingMapper;
    }
}
```

---

## 5. DIP — Dependency Inversion Principle

### `controller/api/ArtworkController.java` (34-40)
```java
private final ArtworkService artworkService;
private final ArtworkMapper artworkMapper;

public ArtworkController(ArtworkService artworkService, ArtworkMapper artworkMapper) {
    this.artworkService = artworkService;
    this.artworkMapper = artworkMapper;
}
```

### `controller/api/BiddingController.java` (29-34)
```java
private final BiddingService biddingService;
private final BiddingMapper biddingMapper;

public BiddingController(BiddingService biddingService, BiddingMapper biddingMapper) {
    this.biddingService = biddingService;
    this.biddingMapper = biddingMapper;
}
```

### `config/AdminInitializer.java` (17-27)
```java
private final AdminUserService adminUserService;

public AdminInitializer(AdminUserService adminUserService,
        @Value("${app.admin.email:}") String adminEmail,
        @Value("${app.admin.password:}") String adminPassword) {
    this.adminUserService = adminUserService;
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
}
```

### `service/implementation/ArtworkServiceImpl.java` (21-27)
```java
private final ArtworkRepository artworkRepository;
private final SellerprofileRepository sellerprofileRepository;

public ArtworkServiceImpl(ArtworkRepository artworkRepository, SellerprofileRepository sellerprofileRepository) {
    this.artworkRepository = artworkRepository;
    this.sellerprofileRepository = sellerprofileRepository;
}
```

### `service/implementation/BiddingServiceImpl.java` (26-37)
```java
private final BiddingRepository biddingRepository;
private final ArtworkRepository artworkRepository;
private final UserRepository userRepository;
private final BidActionRepository bidActionRepository;
private final BiddingStateResolver stateResolver;

public BiddingServiceImpl(BiddingRepository biddingRepository, ArtworkRepository artworkRepository,
        UserRepository userRepository, BidActionRepository bidActionRepository,
        BiddingStateResolver stateResolver) {
    this.biddingRepository = biddingRepository;
    this.artworkRepository = artworkRepository;
    this.userRepository = userRepository;
    this.bidActionRepository = bidActionRepository;
    this.stateResolver = stateResolver;
}
```

---

## ข้อจำกัดที่ทราบ

- **DIP (บางส่วน):** `BiddingServiceImpl.java:30` และ `AdminBiddingServiceImpl.java:17` พึ่งพา `BiddingStateResolver` ซึ่งเป็นคลาสจริง ไม่ใช่ Interface
- **SRP (บางส่วน):** `placeBid` (`BiddingServiceImpl.java:85-119`) ทำหลายขั้นในเมธอดเดียว
- **OCP (บางส่วน):** การเพิ่มสถานะใหม่ต้องเพิ่มค่าใน `Bidding.Status` (enum) ด้วย
- **ชั้น Service รั่วชนิดของ Entity:** `BiddingService.java:10,16` รับ `Date`/`Double` และคืน Entity ให้ Controller แปลงต่อ
