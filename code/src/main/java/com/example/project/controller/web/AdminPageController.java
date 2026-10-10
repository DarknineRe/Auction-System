package com.example.project.controller.web;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.dto.request.CancelPaymentRequest;
import com.example.project.dto.request.UpdateUserStatusRequest;
import com.example.project.dto.request.VoidBidRequest;
import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.model.Payment;
import com.example.project.model.User;
import com.example.project.service.AdminBidActionService;
import com.example.project.service.AdminBiddingService;
import com.example.project.service.AdminPaymentService;
import com.example.project.service.AdminUserService;
import com.example.project.service.BiddingService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Back-office pages. Every handler requires ADMIN or SUPER_ADMIN unless a method narrows it further. */
@Controller
@Validated
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminPageController {

    private final AdminUserService adminUserService;
    private final AdminBiddingService adminBiddingService;
    private final AdminPaymentService adminPaymentService;
    private final AdminBidActionService adminBidActionService;
    private final BiddingService biddingService;
    private final WebSupport web;

    public AdminPageController(AdminUserService adminUserService, AdminBiddingService adminBiddingService,
            AdminPaymentService adminPaymentService, AdminBidActionService adminBidActionService,
            BiddingService biddingService, WebSupport web) {
        this.adminUserService = adminUserService;
        this.adminBiddingService = adminBiddingService;
        this.adminPaymentService = adminPaymentService;
        this.adminBidActionService = adminBidActionService;
        this.biddingService = biddingService;
        this.web = web;
    }

    @GetMapping("/admin")
    public String adminDashboard(Model model, Authentication authentication) {
        model.addAttribute("canManagePayments",
                web.hasAnyRole(authentication, "ROLE_ADMIN", "ROLE_SUPER_ADMIN"));
        return web.workspace(model, "admin-dashboard", "Administration");
    }

    @GetMapping("/admin/users")
    public String adminUsers(
            @RequestParam(defaultValue = "0") int page, Authentication authentication, Model model) {
        Page<User> users = adminUserService.getUsers(null, web.newestFirst(page));
        model.addAttribute("users", users.getContent());
        model.addAttribute("tablePage", users);
        model.addAttribute("isSuperAdmin", web.hasAnyRole(authentication, "ROLE_SUPER_ADMIN"));
        return web.workspace(model, "admin-users", "Manage users");
    }

    @PostMapping("/admin/users/{userId}/status")
    public String setUserStatus(
            @PathVariable Long userId, Authentication authentication, @RequestParam boolean enabled) {
        web.validate(new UpdateUserStatusRequest(enabled));
        adminUserService.setEnabled(authentication.getName(), userId, enabled);
        return "redirect:/admin/users?success";
    }

    @PostMapping("/admin/users/{userId}/promote")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String promoteUserToAdmin(@PathVariable Long userId, Authentication authentication) {
        adminUserService.promoteToAdmin(authentication.getName(), userId);
        return "redirect:/admin/users?success";
    }

    @PostMapping("/admin/users/{userId}/demote")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String demoteAdminToUser(@PathVariable Long userId, Authentication authentication) {
        adminUserService.demoteAdminToUser(authentication.getName(), userId);
        return "redirect:/admin/users?success";
    }

    @GetMapping("/admin/auctions")
    public String adminAuctions(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Bidding> biddings = biddingService.getBiddings(null, web.newestFirst(page));
        model.addAttribute("biddings", biddings.getContent());
        model.addAttribute("tablePage", biddings);
        return web.workspace(model, "admin-auctions", "Manage auctions");
    }

    @PostMapping("/admin/auctions/{biddingId}/close")
    public String closeAuction(@PathVariable Long biddingId) {
        adminBiddingService.closeBidding(biddingId);
        return "redirect:/admin/auctions?success";
    }

    @PostMapping("/admin/auctions/{biddingId}/cancel")
    public String adminCancelAuction(@PathVariable Long biddingId) {
        adminBiddingService.cancelBidding(biddingId);
        return "redirect:/admin/auctions?success";
    }

    @GetMapping("/admin/payments")
    public String adminPayments(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Payment> payments = adminPaymentService.getPayments(null, web.newestFirst(page));
        model.addAttribute("payments", payments.getContent());
        model.addAttribute("tablePage", payments);
        return web.workspace(model, "admin-payments", "Manage payments");
    }

    @PostMapping("/admin/payments/{paymentId}/cancel")
    public String adminCancelPayment(
            @PathVariable Long paymentId, @RequestParam @NotBlank @Size(max = 500) String reason) {
        web.validate(new CancelPaymentRequest(reason));
        adminPaymentService.cancelPayment(paymentId, reason.trim());
        return "redirect:/admin/payments?success";
    }

    @GetMapping("/admin/bids")
    public String adminBids(
            @RequestParam(required = false) Long biddingId,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        model.addAttribute("biddingId", biddingId);
        if (biddingId != null) {
            Page<BidAction> bids = adminBidActionService.getAllBids(biddingId, web.newestFirst(page));
            model.addAttribute("bids", bids.getContent());
            model.addAttribute("tablePage", bids);
        }
        return web.workspace(model, "admin-bids", "Review bids");
    }

    @PostMapping("/admin/bids/{bidId}/void")
    public String voidBid(
            @PathVariable Long bidId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 500) String reason,
            @RequestParam Long biddingId) {
        web.validate(new VoidBidRequest(reason));
        adminBidActionService.voidBid(authentication.getName(), bidId, reason.trim());
        return "redirect:/admin/bids?biddingId=" + biddingId + "&success";
    }
}
