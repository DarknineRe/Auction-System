[1mdiff --git a/README.md b/README.md[m
[1mindex 1249a09..3f38663 100644[m
[1m--- a/README.md[m
[1m+++ b/README.md[m
[36m@@ -14,12 +14,14 @@[m
 [m
 ##  Tech Stack[m
 [m
[31m-- **Backend:** Spring Boot 3.x (Java 17+)[m
[31m-- **Build Tool:** Maven[m
[31m-- **Database:** PostgreSQL / MySQL[m
[32m+[m[32m- **Backend:** Spring Boot 4.1.1, Java 26, Spring MVC, Spring Security, Bean Validation[m
[32m+[m[32m- **Build Tool:** Maven (Maven Wrapper)[m
[32m+[m[32m- **Database:** Neon PostgreSQL[m
 - **ORM:** Spring Data JPA (Hibernate)[m
[31m-- **API Documentation:** idk[m
[31m-- **Frontend:** idk[m
[32m+[m[32m- **API Documentation:** OpenAPI / Swagger UI (Springdoc)[m
[32m+[m[32m- **Frontend:** Thymeleaf[m
[32m+[m[32m- **Deployment:** Render Web Service[m
[32m+[m[32m- **Containerization:** Docker[m
 [m
 ##  System Architecture[m
 [m
[1mdiff --git a/code/src/main/java/com/example/project/dto/request/UpdateBiddingRequest.java b/code/src/main/java/com/example/project/dto/request/UpdateBiddingRequest.java[m
[1mindex b8e0f08..ccce092 100644[m
[1m--- a/code/src/main/java/com/example/project/dto/request/UpdateBiddingRequest.java[m
[1m+++ b/code/src/main/java/com/example/project/dto/request/UpdateBiddingRequest.java[m
[36m@@ -1,12 +1,13 @@[m
 package com.example.project.dto.request;[m
 [m
[32m+[m[32mimport java.math.BigDecimal;[m
 import java.util.Date;[m
 [m
 import jakarta.validation.constraints.NotNull;[m
 import jakarta.validation.constraints.Positive;[m
 [m
 public record UpdateBiddingRequest([m
[31m-        @NotNull @Positive Double startingPrice,[m
[32m+[m[32m        @NotNull @Positive BigDecimal startingPrice,[m
         @NotNull Date startDate,[m
         @NotNull Date endDate) {[m
 }[m
[1mdiff --git a/code/src/main/java/com/example/project/dto/response/PaymentResponse.java b/code/src/main/java/com/example/project/dto/response/PaymentResponse.java[m
[1mindex 26108ae..e4d25ae 100644[m
[1m--- a/code/src/main/java/com/example/project/dto/response/PaymentResponse.java[m
[1m+++ b/code/src/main/java/com/example/project/dto/response/PaymentResponse.java[m
[36m@@ -1,5 +1,6 @@[m
 package com.example.project.dto.response;[m
 [m
[32m+[m[32mimport java.math.BigDecimal;[m
 import java.util.Date;[m
 [m
 import com.example.project.model.Payment;[m
[36m@@ -11,7 +12,7 @@[m [mpublic record PaymentResponse([m
         Long sellerUserId,[m
         Long sellerProfileId,[m
         String sellerBankAccount,[m
[31m-        Double amount,[m
[32m+[m[32m        BigDecimal amount,[m
         Payment.Status status,[m
         Date createdAt,[m
         Date dueDate,[m
[1mdiff --git a/code/src/main/java/com/example/project/dto/response/PublicSellerprofileResponse.java b/code/src/main/java/com/example/project/dto/response/PublicSellerprofileResponse.java[m
[1mindex 54441fb..2222372 100644[m
[1m--- a/code/src/main/java/com/example/project/dto/response/PublicSellerprofileResponse.java[m
[1m+++ b/code/src/main/java/com/example/project/dto/response/PublicSellerprofileResponse.java[m
[36m@@ -1,10 +1,12 @@[m
 package com.example.project.dto.response;[m
 [m
[32m+[m[32mimport java.math.BigDecimal;[m
[32m+[m
 // Seller details anyone may see; leaves out the bank account.[m
 public record PublicSellerprofileResponse([m
         Long sellerProfileId,[m
         Long userId,[m
         String name,[m
[31m-        double rating,[m
[32m+[m[32m        BigDecimal rating,[m
         int saleCount) {[m
 }[m
[1mdiff --git a/code/src/main/java/com/example/project/model/Bidding.java b/code/src/main/java/com/example/project/model/Bidding.java[m
[1mindex b3ac16a..7fa04da 100644[m
[1m--- a/code/src/main/java/com/example/project/model/Bidding.java[m
[1m+++ b/code/src/main/java/com/example/project/model/Bidding.java[m
[36m@@ -49,6 +49,13 @@[m [mpublic class Bidding {[m
     @JoinColumn(name = "user_id")[m
     private User owner;[m
 [m
[32m+[m[32m    @ManyToOne[m
[32m+[m[32m    @JoinColumn(name = "winner_user_id")[m
[32m+[m[32m    private User winner;[m
[32m+[m
[32m+[m[32m    @Column[m
[32m+[m[32m    private Integer sellerRating;[m
[32m+[m
     @Column(precision = 19, scale = 4)[m
     private BigDecimal startingPrice; // startingPrince and date HERE instead of artwork[m
     private Date startDate;[m
[1mdiff --git a/code/src/main/java/com/example/project/model/Payment.java b/code/src/main/java/com/example/project/model/Payment.java[m
[1mindex f505e43..d3739f7 100644[m
[1m--- a/code/src/main/java/com/example/project/model/Payment.java[m
[1m+++ b/code/src/main/java/com/example/project/model/Payment.java[m
[36m@@ -1,5 +1,6 @@[m
 package com.example.project.model;[m
 [m
[32m+[m[32mimport java.math.BigDecimal;[m
 import java.util.Date;[m
 [m
 import jakarta.persistence.Column;[m
[36m@@ -46,8 +47,8 @@[m [mpublic class Payment {[m
     private Sellerprofile sellerprofile;[m
 [m
     // Final price, copied from the winning bid when the bidding closed.[m
[31m-    @Column(nullable = false)[m
[31m-    private Double amount;[m
[32m+[m[32m    @Column(nullable = false, precision = 19, scale = 4)[m
[32m+[m[32m    private BigDecimal amount;[m
 [m
     @Enumerated(EnumType.STRING)[m
     @Column(nullable = false, length = 30)[m
[36m@@ -134,11 +135,11 @@[m [mpublic class Payment {[m
         this.sellerprofile = sellerprofile;[m
     }[m
 [m
[31m-    public Double getAmount() {[m
[32m+[m[32m    public BigDecimal getAmount() {[m
         return this.amount;[m
     }[m
 [m
[31m-    public void setAmount(Double amount) {[m
[32m+[m[32m    public void setAmount(BigDecimal amount) {[m
         this.amount = amount;[m
     }[m
 [m
[1mdiff --git a/code/src/main/java/com/example/project/repository/SellerprofileRepository.java b/code/src/main/java/com/example/project/repository/SellerprofileRepository.java[m
[1mindex ccc8265..94433fd 100644[m
[1m--- a/code/src/main/java/com/example/project/repository/SellerprofileRepository.java[m
[1m+++ b/code/src/main/java/com/example/project/repository/SellerprofileRepository.java[m
[36m@@ -1,5 +1,6 @@[m
 package com.example.project.repository;[m
 [m
[32m+[m[32mimport java.math.BigDecimal;[m
 import java.util.Optional;[m
 [m
 import org.springframework.data.jpa.repository.JpaRepository;[m
[36m@@ -22,5 +23,5 @@[m [mpublic interface SellerprofileRepository extends JpaRepository<Sellerprofile, Lo[m
 [m
     @Modifying(flushAutomatically = true, clearAutomatically = true)[m
     @Query("update Sellerprofile s set s.rating = :rating where s.sellprofileId = :id")[m
[31m-    int updateRating(@Param("id") Long sellerProfileId, @Param("rating") double rating);[m
[32m+[m[32m    int updateRating(@Param("id") Long sellerProfileId, @Param("rating") BigDecimal rating);[m
 }[m
[1mdiff --git a/code/src/main/java/com/example/project/service/ArtworkService.java b/code/src/main/java/com/example/project/service/ArtworkService.java[m
[1mindex 94b008c..8017102 100644[m
[1m--- a/code/src/main/java/com/example/project/service/ArtworkService.java[m
[1m+++ b/code/src/main/java/com/example/project/service/ArtworkService.java[m
[36m@@ -16,7 +16,7 @@[m [mpublic interface ArtworkService {[m
 [m
     List<Artwork> getArtworksBySeller(Long sellerUserID);[m
 [m
[31m-    Artwork updateArtwork(Long artworkID, String actorEmail, String title, String imageUrl);[m
[32m+[m[32m    Artwork updateArtwork(Long artworkID, Long sellerUserID, String title, String imageUrl);[m
 [m
     void deleteArtwork(Long artworkID, Long userID);[m
 [m
[1mdiff --git a/code/src/main/java/com/example/project/service/BiddingService.java b/code/src/main/java/com/example/project/service/BiddingService.java[m
[1mindex 3519661..259e821 100644[m
[1m--- a/code/src/main/java/com/example/project/service/BiddingService.java[m
[1m+++ b/code/src/main/java/com/example/project/service/BiddingService.java[m
[36m@@ -18,9 +18,9 @@[m [mpublic interface BiddingService {[m
 [m
     Page<Bidding> getBiddings(Bidding.Status status, Pageable pageable);[m
 [m
[31m-    BidAction placeBid(Long biddingID, Long userID, Double amount);[m
[32m+[m[32m    BidAction placeBid(Long biddingID, String actorEmail, BigDecimal amount);[m
 [m
[31m-    Bidding updateBidding(Long biddingID, Long ownerID, Double startingPrice, Date startDate, Date endDate);[m
[32m+[m[32m    Bidding updateBidding(Long biddingID, Long ownerID, BigDecimal startingPrice, Date startDate, Date endDate);[m
 [m
     Bidding cancelBidding(Long biddingID, Long ownerID);[m
 [m
[1mdiff --git a/code/src/main/java/com/example/project/service/implementation/AdminBidActionServiceImpl.java b/code/src/main/java/com/example/project/service/implementation/AdminBidActionServiceImpl.java[m
[1mindex 6f929dc..31337b5 100644[m
[1m--- a/code/src/main/java/com/example/project/service/implementation/AdminBidActionServiceImpl.java[m
[1m+++ b/code/src/main/java/com/example/project/service/implementation/AdminBidActionServiceImpl.java[m
[36m@@ -82,7 +82,7 @@[m [mpublic class AdminBidActionServiceImpl implements AdminBidActionService {[m
                 .findTopByBidding_IdAndStatusOrderByAmountDesc(bidding.getId(), BidAction.Status.VALID)[m
                 .map(BidAction::getAmount)[m
                 .orElse(null);[m
[31m-        bidding.setLastBid(highestValidBid);[m
[32m+[m[32m        bidding.setLastBid(currentPrice);[m
         biddingRepository.save(bidding);[m
 [m
         return saved;[m
[1mdiff --git a/code/src/main/java/com/example/project/service/implementation/AdminPaymentServiceImpl.java b/code/src/main/java/com/example/project/service/implementation/AdminPaymentServiceImpl.java[m
[1mindex d90b564..df44ee6 100644[m
[1m--- a/code/src/main/java/com/example/project/service/implementation/AdminPaymentServiceImpl.java[m
[1m+++ b/code/src/main/java/com/example/project/service/implementation/AdminPaymentServiceImpl.java[m
[36m@@ -1,5 +1,6 @@[m
 package com.example.project.service.implementation;[m
 [m
[32m+[m[32mimport java.math.BigDecimal;[m
 import java.util.Date;[m
 [m
 import org.springframework.data.domain.Page;[m
[36m@@ -81,7 +82,8 @@[m [mpublic class AdminPaymentServiceImpl implements AdminPaymentService {[m
                 bidding.setSellerRating(null);[m
                 biddingRepository.saveAndFlush(bidding);[m
                 Double average = biddingRepository.averageSellerRatingByOwnerId(bidding.getOwner().getId());[m
[31m-                sellerprofileRepository.updateRating(sellerProfileId, average == null ? 0 : average);[m
[32m+[m[32m                sellerprofileRepository.updateRating(sellerProfileId,[m
[32m+[m[32m                    average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average));[m
             }[m
             sellerprofileRepository.addToSalecount(sellerProfileId, -1);[m
         }[m
[1mdiff --git a/code/src/main/java/com/example/project/service/implementation/ArtworkServiceImpl.java b/code/src/main/java/com/example/project/service/implementation/ArtworkServiceImpl.java[m
[1mindex 7b0adf2..141c5b2 100644[m
[1m--- a/code/src/main/java/com/example/project/service/implementation/ArtworkServiceImpl.java[m
[1m+++ b/code/src/main/java/com/example/project/service/implementation/ArtworkServiceImpl.java[m
[36m@@ -73,11 +73,11 @@[m [mpublic class ArtworkServiceImpl implements ArtworkService {[m
 [m
     @Override[m
     @Transactional[m
[31m-    public Artwork updateArtwork(Long artworkID, String actorEmail, String title, String imageUrl) {[m
[32m+[m[32m    public Artwork updateArtwork(Long artworkID, Long sellerUserID, String title, String imageUrl) {[m
         Artwork artwork = getArtworkById(artworkID);[m
         if (artwork.getSellerprofile() == null[m
                 || artwork.getSellerprofile().getUser() == null[m
[31m-                || !artwork.getSellerprofile().getUser().getEmail().equalsIgnoreCase(actorEmail.trim())) {[m
[32m+[m[32m                || !artwork.getSellerprofile().getUser().getId().equals(sellerUserID)) {[m
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the artwork owner can update it");[m
         }[m
         artwork.setTitle(title);[m
[1mdiff --git a/code/src/main/java/com/example/project/service/implementation/BiddingServiceImpl.java b/code/src/main/java/com/example/project/service/implementation/BiddingServiceImpl.java[m
[1mindex 9e64cae..c526a8c 100644[m
[1m--- a/code/src/main/java/com/example/project/service/implementation/BiddingServiceImpl.java[m
[1m+++ b/code/src/main/java/com/example/project/service/implementation/BiddingServiceImpl.java[m
[36m@@ -5,6 +5,7 @@[m [mimport java.util.ArrayList;[m
 import java.util.Date;[m
 import java.util.LinkedHashSet;[m
 import java.util.List;[m
[32m+[m[32mimport java.util.Optional;[m
 import java.util.Set;[m
 [m
 import org.springframework.data.domain.Page;[m
[36m@@ -30,6 +31,7 @@[m [mimport com.example.project.service.state.BiddingStateResolver;[m
 @Service[m
 public class BiddingServiceImpl implements BiddingService {[m
 [m
[32m+[m[32m    private static final BigDecimal MIN_BID_INCREMENT = new BigDecimal("1.00");[m
     private static final Set<String> SORTABLE_FIELDS = Set.of("id", "startingPrice", "lastBid", "startDate", "endDate", "status", "owner.id", "owner.name", "owner.email");[m
 [m
     private final BiddingRepository biddingRepository;[m
[36m@@ -141,7 +143,7 @@[m [mpublic class BiddingServiceImpl implements BiddingService {[m
 [m
     @Override[m
     @Transactional[m
[31m-    public Bidding updateBidding(Long biddingID, Long ownerID, Double startingPrice, Date startDate, Date endDate) {[m
[32m+[m[32m    public Bidding updateBidding(Long biddingID, Long ownerID, BigDecimal startingPrice, Date startDate, Date endDate) {[m
         validateDates(startDate, endDate);[m
         Bidding bidding = findOwnBiddingWithoutBids(biddingID, ownerID, "edited");[m
 [m
[36m@@ -202,26 +204,12 @@[m [mpublic class BiddingServiceImpl implements BiddingService {[m
         }[m
     }[m
     [m
[31m-    @Override[m
[31m-    @Transactional(readOnly = true)[m
[31m-    public Page<Bidding> getAllBiddings(Pageable pageable) {[m
[31m-        validateSort(pageable);[m
[31m-        return biddingRepository.findAll(pageable);[m
[31m-    }[m
[31m-[m
[31m-    private void validateSort(Pageable pageable) {[m
[31m-        for (Sort.Order order : pageable.getSort()) {[m
[31m-            if (!SORTABLE_FIELDS.contains(order.getProperty())) {[m
[31m-                throw new ResponseStatusException([m
[31m-                        HttpStatus.BAD_REQUEST, "Cannot sort by: " + order.getProperty());[m
[31m-            }[m
[31m-        }[m
[31m-    }[m
[31m-[m
     @Override[m
     @Transactional[m
     public BidAction placeBid(Long biddingID, String actorEmail, BigDecimal amount) {[m
[31m-        Bidding bidding = getBiddingById(biddingID);[m
[32m+[m[32m        Bidding bidding = biddingRepository.findByIdForUpdate(biddingID)[m
[32m+[m[32m                .orElseThrow(() -> new ResponseStatusException([m
[32m+[m[32m                        HttpStatus.NOT_FOUND, "Bidding not found: " + biddingID));[m
         User bidder = userRepository.findByEmail(actorEmail.trim().toLowerCase(java.util.Locale.ROOT))[m
                 .orElseThrow(() -> new ResponseStatusException([m
                         HttpStatus.NOT_FOUND, "User not found"));[m
[36m@@ -246,22 +234,18 @@[m [mpublic class BiddingServiceImpl implements BiddingService {[m
         Optional<BidAction> highestBid = bidActionRepository[m
                 .findTopByBidding_IdAndStatusOrderByAmountDesc(biddingID, BidAction.Status.VALID);[m
 [m
[32m+[m[32m        if (highestBid.map(bid -> bid.getUser().getId().equals(userID)).orElse(false)) {[m
[32m+[m[32m            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already the highest bidder.");[m
[32m+[m[32m        }[m
[32m+[m
         // The first bid may match the starting price; later bids must raise the highest bid by MIN_BID_INCREMENT.[m
[31m-        double minimumBid = highestBid[m
[31m-                .map(bid -> bid.getAmount() + MIN_BID_INCREMENT)[m
[32m+[m[32m        BigDecimal minimumBid = highestBid[m
[32m+[m[32m            .map(bid -> bid.getAmount().add(MIN_BID_INCREMENT))[m
                 .orElse(bidding.getStartingPrice());[m
[31m-        if (amount < minimumBid) {[m
[32m+[m[32m        if (amount.compareTo(minimumBid) < 0) {[m
             throw new ResponseStatusException(HttpStatus.CONFLICT, "Bid must be at least " + minimumBid);[m
         }[m
 [m
[31m-        // Prevent self-bidding (bidding against your own previous bid)[m
[31m-        BidAction highestBid = bidActionRepository[m
[31m-                .findTopByBidding_IdAndStatusOrderByAmountDesc(biddingID, BidAction.Status.VALID)[m
[31m-                .orElse(null);[m
[31m-        if (highestBid != null && highestBid.getUser().getId().equals(userID)) {[m
[31m-            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already the highest bidder.");[m
[31m-        }[m
[31m-[m
         BidAction action = new BidAction();[m
         action.setBidding(bidding);[m
         action.setUser(bidder);[m
[1mdiff --git a/code/src/main/java/com/example/project/service/implementation/SellerprofileServiceImpl.java b/code/src/main/java/com/example/project/service/implementation/SellerprofileServiceImpl.java[m
[1mindex 307e8bb..463d3f2 100644[m
[1m--- a/code/src/main/java/com/example/project/service/implementation/SellerprofileServiceImpl.java[m
[1m+++ b/code/src/main/java/com/example/project/service/implementation/SellerprofileServiceImpl.java[m
[36m@@ -1,5 +1,6 @@[m
 package com.example.project.service.implementation;[m
 [m
[32m+[m[32mimport java.math.BigDecimal;[m
 import java.util.Locale;[m
 [m
 import org.springframework.http.HttpStatus;[m
[36m@@ -107,8 +108,9 @@[m [mpublic class SellerprofileServiceImpl implements SellerprofileService {[m
 [m
         Long sellerUserId = bidding.getOwner().getId();[m
         Sellerprofile seller = findSellerProfileByUserId(sellerUserId);[m
[32m+[m[32m        Double average = biddingRepository.averageSellerRatingByOwnerId(sellerUserId);[m
         sellerprofileRepository.updateRating(seller.getSellprofileId(),[m
[31m-                biddingRepository.averageSellerRatingByOwnerId(sellerUserId));[m
[32m+[m[32m            average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average));[m
         // The update query clears the persistence context, so read the profile again.[m
         return findSellerProfileByUserId(sellerUserId);[m
     }[m
