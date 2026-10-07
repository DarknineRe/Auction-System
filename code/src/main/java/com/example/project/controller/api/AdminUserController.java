package com.example.project.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.request.UpdateUserStatusRequest;
import com.example.project.dto.response.AdminUserResponse;
import com.example.project.mapper.AdminUserMapper;
import com.example.project.model.User;
import com.example.project.service.AdminUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final AdminUserMapper adminUserMapper;

    public AdminUserController(AdminUserService adminUserService, AdminUserMapper adminUserMapper) {
        this.adminUserService = adminUserService;
        this.adminUserMapper = adminUserMapper;
    }

    @GetMapping
    public ResponseEntity<PagedModel<AdminUserResponse>> getUsers(
            @RequestParam(required = false) User.Role role,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<AdminUserResponse> page = adminUserService.getUsers(role, pageable)
                .map(adminUserMapper::toResponse);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserMapper.toResponse(adminUserService.getUserById(userId)));
    }


    @PatchMapping("/{userId}/status")
    public ResponseEntity<AdminUserResponse> setStatus(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        User user = adminUserService.setEnabled(authentication.getName(), userId, request.enabled());

        return ResponseEntity.ok(adminUserMapper.toResponse(user));
    }
}