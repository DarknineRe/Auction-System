package com.example.project.controller.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.dto.request.CancelPaymentRequest;
import com.example.project.dto.request.SellerprofileRequest;
import com.example.project.dto.request.ShipPaymentRequest;
import com.example.project.dto.request.SubmitPaymentSlipRequest;
import com.example.project.dto.request.RejectPaymentRequest;
import com.example.project.dto.request.UpdateBiddingRequest;
import com.example.project.dto.request.UpdateUserStatusRequest;
import com.example.project.dto.request.VoidBidRequest;
import com.example.project.model.Bidding;
import com.example.project.model.BidAction;
import com.example.project.model.Payment;
import com.example.project.model.Sellerprofile;
import com.example.project.model.User;
import com.example.project.service.AdminBidActionService;
import com.example.project.service.AdminBiddingService;
import com.example.project.service.AdminModerationService;
import com.example.project.service.AdminPaymentService;
import com.example.project.service.AdminUserService;
import com.example.project.service.ArtworkService;
import com.example.project.service.BidActionService;
import com.example.project.service.BiddingService;
import com.example.project.service.PaymentService;
import com.example.project.service.SellerprofileService;
import com.example.project.service.UserService;
import com.example.project.service.AuctionListingService;
import com.example.project.service.CommentService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Controller
@Validated
public class PageController {

    private static final int PAGE_SIZE = 15;
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "id");
    private static final Sort AUCTIONS_ENDING_FIRST = Sort.by(Sort.Direction.ASC, "endDate");

    private final UserService userService;
    private final ArtworkService artworkService;
    private final BiddingService biddingService;
    private final BidActionService bidActionService;
    private final PaymentService paymentService;
    private final SellerprofileService sellerprofileService;
    private final AdminUserService adminUserService;
    private final AdminBiddingService adminBiddingService;
    private final AdminPaymentService adminPaymentService;
    private final AdminModerationService adminModerationService;
    private final AdminBidActionService adminBidActionService;
    private final AuctionListingService auctionListingService;
    private final CommentService commentService;
    private final Validator validator;

    public PageController(
            UserService userService,
            ArtworkService artworkService,
            BiddingService biddingService,
            BidActionService bidActionService,
            PaymentService paymentService,
            SellerprofileService sellerprofileService,
            AdminUserService adminUserService,
            AdminBiddingService adminBiddingService,
            AdminPaymentService adminPaymentService,
            AdminModerationService adminModerationService,
            AdminBidActionService adminBidActionService,
            AuctionListingService auctionListingService,
            CommentService commentService,
            Validator validator) {
        this.userService = userService;
        this.artworkService = artworkService;
        this.biddingService = biddingService;
        this.bidActionService = bidActionService;
        this.paymentService = paymentService;
        this.sellerprofileService = sellerprofileService;
        this.adminUserService = adminUserService;
        this.adminBiddingService = adminBiddingService;
        this.adminPaymentService = adminPaymentService;
        this.adminModerationService = adminModerationService;
        this.adminBidActionService = adminBidActionService;
        this.auctionListingService = auctionListingService;
        this.commentService = commentService;
        this.validator = validator;
    }

    @GetMapping({ "/", "/home" })
    public String home(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Bidding> biddings = biddingService.getBiddings(
                Bidding.Status.ACTIVE, PageRequest.of(Math.max(0, page), 12, AUCTIONS_ENDING_FIRST));
        model.addAttribute("biddings", biddings);
        model.addAttribute("now", new Date());
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam @NotBlank @Size(max = 255) String name,
            @RequestParam @NotBlank @Size(max = 320) String email,
            @RequestParam @NotBlank @Size(min = 8, max = 72) String password,
            @RequestParam @NotBlank String confirmPassword,
            @RequestParam(required = false) @Size(max = 100) String phone,
            @RequestParam(required = false) @Size(max = 1000) String address) {
        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }
        userService.registerUser(name, email, password, phone, address);
        return "redirect:/login?registered";
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("user", currentUser(authentication));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 255) String name,
            @RequestParam(required = false) @Size(max = 100) String phone,
            @RequestParam(required = false) @Size(max = 1000) String address) {
        userService.updateCurrentUser(authentication.getName(), name, phone, address);
        return "redirect:/profile?profileUpdated";
    }

    @PostMapping("/profile/password")
    public String changePassword(
            Authentication authentication,
            @RequestParam @NotBlank String currentPassword,
            @RequestParam @NotBlank @Size(min = 8, max = 72) String newPassword,
            @RequestParam @NotBlank String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new passwords do not match");
        }
        userService.changePassword(authentication.getName(), currentPassword, newPassword);
        return "redirect:/profile?passwordUpdated";
    }

    @GetMapping("/biddings/{biddingId}")
    public String biddingDetail(
            @PathVariable Long biddingId,
            Authentication authentication,
            Model model) {
        Bidding bidding = biddingService.getBiddingById(biddingId);
        model.addAttribute("bidding", bidding);
        model.addAttribute("now", new Date());
        model.addAttribute("artworks", bidding.getArtworks());
        model.addAttribute("bids", bidActionService.getBidsByBidding(biddingId));
        model.addAttribute("comments", commentService.getCommentsByBiddingId(biddingId));
        model.addAttribute("currentUser", isAuthenticated(authentication)
                ? userService.getCurrentUser(authentication.getName())
                : null);
        return "bidding-detail";
    }

    @PostMapping("/biddings/{biddingId}/bids")
    public String placeBid(
            @PathVariable Long biddingId,
            Authentication authentication,
            @RequestParam @Positive BigDecimal amount) {
        biddingService.placeBid(biddingId, authentication.getName(), amount);
        return "redirect:/biddings/" + biddingId + "?bidPlaced";
    }

    @PostMapping("/biddings/{biddingId}/comments")
    public String createComment(
            @PathVariable Long biddingId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 2000) String message) {
        User user = currentUser(authentication);
        commentService.createComment(biddingId, user.getId(), message.trim());
        return "redirect:/biddings/" + biddingId + "?commentPosted";
    }

    @PostMapping("/biddings/{biddingId}/comments/{commentId}/like")
    public String likeComment(
            @PathVariable Long biddingId,
            @PathVariable Long commentId,
            Authentication authentication) {
        User user = currentUser(authentication);
        commentService.likeComment(biddingId, commentId, user.getId());
        return "redirect:/biddings/" + biddingId + "#comments";
    }

    @PostMapping("/biddings/{biddingId}/comments/{commentId}/dislike")
    public String dislikeComment(
            @PathVariable Long biddingId,
            @PathVariable Long commentId,
            Authentication authentication) {
        User user = currentUser(authentication);
        commentService.dislikeComment(biddingId, commentId, user.getId());
        return "redirect:/biddings/" + biddingId + "#comments";
    }

    @PostMapping("/biddings/{biddingId}/comments/{commentId}")
    public String updateComment(
            @PathVariable Long biddingId,
            @PathVariable Long commentId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 2000) String message) {
        User user = currentUser(authentication);
        commentService.updateComment(biddingId, commentId, user.getId(), message.trim());
        return "redirect:/biddings/" + biddingId + "#comments";
    }

    @PostMapping("/biddings/{biddingId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long biddingId,
            @PathVariable Long commentId,
            Authentication authentication) {
        User user = currentUser(authentication);
        commentService.deleteComment(biddingId, commentId, user.getId());
        return "redirect:/biddings/" + biddingId + "#comments";
    }

    @PostMapping("/biddings/{biddingId}/seller-rating")
    public String rateSeller(
            @PathVariable Long biddingId,
            Authentication authentication,
            @RequestParam @Positive Integer score) {
        User user = currentUser(authentication);
        sellerprofileService.rateSeller(biddingId, user.getId(), score);
        return "redirect:/biddings/" + biddingId + "?ratingSubmitted";
    }
    @GetMapping("/my-bids")
    public String myBids(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int wonPage,
            Model model) {
        User user = currentUser(authentication);
        Page<BidAction> bids = bidActionService.getBidsByUser(
                user.getId(), PageRequest.of(Math.max(0, page), PAGE_SIZE, NEWEST_FIRST));
        Page<Bidding> wonBiddings = biddingService.getBiddingsWonBy(
                user.getId(), PageRequest.of(Math.max(0, wonPage), PAGE_SIZE, NEWEST_FIRST));
        model.addAttribute("bids", bids.getContent());
        model.addAttribute("bidsPage", bids);
        model.addAttribute("wonBiddings", wonBiddings.getContent());
        model.addAttribute("wonBiddingsPage", wonBiddings);
        return workspace(model, "my-bids", "My bids");
    }

    @GetMapping("/my-auctions")
    public String myAuctions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        User user = currentUser(authentication);
        Page<Bidding> biddings = biddingService.getBiddingsByOwner(
                user.getId(), null, PageRequest.of(Math.max(0, page), PAGE_SIZE, NEWEST_FIRST));
        model.addAttribute("biddings", biddings.getContent());
        model.addAttribute("tablePage", biddings);
        return workspace(model, "my-auctions", "My auctions");
    }

    @GetMapping("/auctions/new")
    public String createAuctionForm(Model model) {
        return workspace(model, "new-auction", "Create an auction");
    }

    @PostMapping("/auctions")
    public String createAuction(
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 255) String title,
            @RequestParam(required = false) @Size(max = 2048) String imageUrl,
            @RequestParam @Positive BigDecimal startingPrice,
            @RequestParam @Positive BigDecimal minimumBidIncrement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        User owner = currentUser(authentication);
        Bidding bidding = auctionListingService.createListing(
                owner.getId(), title.trim(), imageUrl, startingPrice, minimumBidIncrement,
                toDate(startDate), toDate(endDate));
        return "redirect:/biddings/" + bidding.getId() + "?success";
    }

    @GetMapping("/auctions/{biddingId}/edit")
    public String editAuctionForm(
            @PathVariable Long biddingId,
            Authentication authentication,
            Model model) {
        User owner = currentUser(authentication);
        Bidding bidding = biddingService.getBiddingById(biddingId);
        if (!bidding.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the auction owner can edit it");
        }
        model.addAttribute("bidding", bidding);
        return workspace(model, "edit-auction", "Edit auction");
    }

    @PostMapping("/auctions/{biddingId}/edit")
    public String editAuction(
            @PathVariable Long biddingId,
            Authentication authentication,
            @RequestParam @Positive BigDecimal startingPrice,
            @RequestParam @Positive BigDecimal minimumBidIncrement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        User owner = currentUser(authentication);
        validate(new UpdateBiddingRequest(startingPrice, minimumBidIncrement, toDate(startDate), toDate(endDate)));
        biddingService.updateBidding(
                biddingId, owner.getId(), startingPrice, minimumBidIncrement, toDate(startDate), toDate(endDate));
        return "redirect:/my-auctions?success";
    }

    @PostMapping("/auctions/{biddingId}/cancel")
    public String cancelAuction(@PathVariable Long biddingId, Authentication authentication) {
        User owner = currentUser(authentication);
        biddingService.cancelBidding(biddingId, owner.getId());
        return "redirect:/my-auctions?success";
    }

    @GetMapping("/purchases")
    public String purchases(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        User user = currentUser(authentication);
        Page<Payment> payments = paymentService.getPurchases(
                user.getId(), null, PageRequest.of(Math.max(0, page), PAGE_SIZE, NEWEST_FIRST));
        model.addAttribute("payments", payments.getContent());
        model.addAttribute("tablePage", payments);
        return workspace(model, "purchases", "My purchases");
    }

    @GetMapping("/sales")
    public String sales(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        User user = currentUser(authentication);
        Page<Payment> payments = paymentService.getSales(
                user.getId(), null, PageRequest.of(Math.max(0, page), PAGE_SIZE, NEWEST_FIRST));
        model.addAttribute("payments", payments.getContent());
        model.addAttribute("tablePage", payments);
        return workspace(model, "sales", "My sales");
    }

    @GetMapping("/payments/{paymentId}")
    public String payment(
            @PathVariable Long paymentId,
            Authentication authentication,
            Model model) {
        User user = currentUser(authentication);
        model.addAttribute("payment", paymentService.getPayment(paymentId, user.getId()));
        model.addAttribute("currentUserId", user.getId());
        return workspace(model, "payment-detail", "Payment details");
    }

    @PostMapping("/payments/{paymentId}/slip")
    public String submitPaymentSlip(
            @PathVariable Long paymentId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 2048) String slipUrl) {
        validate(new SubmitPaymentSlipRequest(slipUrl));
        User buyer = currentUser(authentication);
        paymentService.submitSlip(paymentId, buyer.getId(), slipUrl.trim());
        return "redirect:/payments/" + paymentId + "?success";
    }

    @PostMapping("/payments/{paymentId}/confirm")
    public String confirmPayment(@PathVariable Long paymentId, Authentication authentication) {
        User seller = currentUser(authentication);
        paymentService.confirmPayment(paymentId, seller.getId());
        return "redirect:/payments/" + paymentId + "?success";
    }

    @PostMapping("/payments/{paymentId}/reject")
    public String rejectPayment(
            @PathVariable Long paymentId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 500) String reason) {
        validate(new RejectPaymentRequest(reason));
        User seller = currentUser(authentication);
        paymentService.rejectPayment(paymentId, seller.getId(), reason.trim());
        return "redirect:/payments/" + paymentId + "?success";
    }

    @PostMapping("/payments/{paymentId}/ship")
    public String shipPayment(
            @PathVariable Long paymentId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 100) String carrier,
            @RequestParam @NotBlank @Size(max = 100) String trackingNumber,
            @RequestParam @NotBlank @Size(max = 2048) String trackingUrl,
            @RequestParam(required = false) @Size(max = 1000) String shippingNote) {
        User seller = currentUser(authentication);
        validate(new ShipPaymentRequest(carrier, trackingNumber, trackingUrl, shippingNote));
        paymentService.ship(paymentId, seller.getId(), carrier.trim(), trackingNumber.trim(),
                trackingUrl.trim(), shippingNote);
        return "redirect:/payments/" + paymentId + "?success";
    }

    @GetMapping("/seller/settings")
    public String sellerSettings(Authentication authentication, Model model) {
        Sellerprofile profile = getSellerProfileOrNull(authentication.getName());
        model.addAttribute("sellerProfile", profile);
        return workspace(model, "seller-settings", "Seller profile");
    }

    @PostMapping("/seller/settings")
    public String saveSellerSettings(
            Authentication authentication,
            @RequestParam @NotBlank String bankaccount) {
        validate(new SellerprofileRequest(bankaccount));
        if (getSellerProfileOrNull(authentication.getName()) == null) {
            sellerprofileService.createSellerProfile(authentication.getName(), bankaccount.trim());
        } else {
            sellerprofileService.updateCurrentSellerProfile(authentication.getName(), bankaccount.trim());
        }
        return "redirect:/seller/settings?success";
    }

    @GetMapping("/sellers/{userId}")
    public String seller(@PathVariable Long userId, Model model) {
        model.addAttribute("sellerProfile", sellerprofileService.getSellerProfileByUserId(userId));
        model.addAttribute("artworks", artworkService.getArtworksBySeller(userId));
        return workspace(model, "seller-profile", "Seller profile");
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String adminDashboard(Model model, Authentication authentication) {
        model.addAttribute("canManagePayments", authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        return workspace(model, "admin-dashboard", "Administration");
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String adminUsers(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<User> users = adminUserService.getUsers(
                null, PageRequest.of(Math.max(0, page), PAGE_SIZE, NEWEST_FIRST));
        model.addAttribute("users", users.getContent());
        model.addAttribute("tablePage", users);
        return workspace(model, "admin-users", "Manage users");
    }

    @PostMapping("/admin/users/{userId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String setUserStatus(
            @PathVariable Long userId,
            Authentication authentication,
            @RequestParam boolean enabled) {
        validate(new UpdateUserStatusRequest(enabled));
        adminUserService.setEnabled(authentication.getName(), userId, enabled);
        return "redirect:/admin/users?success";
    }

    @GetMapping("/admin/auctions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String adminAuctions(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Bidding> biddings = biddingService.getBiddings(
                null, PageRequest.of(Math.max(0, page), PAGE_SIZE, NEWEST_FIRST));
        model.addAttribute("biddings", biddings.getContent());
        model.addAttribute("tablePage", biddings);
        return workspace(model, "admin-auctions", "Manage auctions");
    }

    @PostMapping("/admin/auctions/{biddingId}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String closeAuction(@PathVariable Long biddingId) {
        adminBiddingService.closeBidding(biddingId);
        return "redirect:/admin/auctions?success";
    }

    @PostMapping("/admin/auctions/{biddingId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String adminCancelAuction(@PathVariable Long biddingId) {
        adminBiddingService.cancelBidding(biddingId);
        return "redirect:/admin/auctions?success";
    }

    @GetMapping("/admin/payments")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPayments(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Payment> payments = adminPaymentService.getPayments(
                null, PageRequest.of(Math.max(0, page), PAGE_SIZE, NEWEST_FIRST));
        model.addAttribute("payments", payments.getContent());
        model.addAttribute("tablePage", payments);
        return workspace(model, "admin-payments", "Manage payments");
    }

    @PostMapping("/admin/payments/{paymentId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminCancelPayment(
            @PathVariable Long paymentId,
            @RequestParam @NotBlank @Size(max = 500) String reason) {
        validate(new CancelPaymentRequest(reason));
        adminPaymentService.cancelPayment(paymentId, reason.trim());
        return "redirect:/admin/payments?success";
    }

    @GetMapping("/admin/moderation")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String adminModeration(Model model) {
        return workspace(model, "admin-moderation", "Content moderation");
    }

    @PostMapping("/admin/moderation")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String moderate(
            @RequestParam @NotBlank String type,
            @RequestParam @Positive Long contentId) {
        if ("artworks".equals(type)) {
            adminModerationService.deleteArtwork(contentId);
        } else if ("comments".equals(type)) {
            adminModerationService.deleteComment(contentId);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported content type");
        }
        return "redirect:/admin/moderation?success";
    }

    @GetMapping("/admin/bids")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String adminBids(
            @RequestParam(required = false) Long biddingId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("biddingId", biddingId);
        if (biddingId != null) {
            Page<BidAction> bids = adminBidActionService.getAllBids(
                    biddingId, PageRequest.of(Math.max(0, page), PAGE_SIZE, NEWEST_FIRST));
            model.addAttribute("bids", bids.getContent());
            model.addAttribute("tablePage", bids);
        }
        return workspace(model, "admin-bids", "Review bids");
    }

    @PostMapping("/admin/bids/{bidId}/void")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public String voidBid(
            @PathVariable Long bidId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 500) String reason,
            @RequestParam Long biddingId) {
        validate(new VoidBidRequest(reason));
        adminBidActionService.voidBid(authentication.getName(), bidId, reason.trim());
        return "redirect:/admin/bids?biddingId=" + biddingId + "&success";
    }

    private User currentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication.getName());
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private Sellerprofile getSellerProfileOrNull(String email) {
        try {
            return sellerprofileService.getCurrentSellerProfile(email);
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw exception;
        }
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private <T> void validate(T form) {
        var violations = validator.validate(form);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("Invalid request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private String workspace(Model model, String page, String title) {
        model.addAttribute("page", page);
        model.addAttribute("pageTitle", title);
        return "workspace";
    }
}
