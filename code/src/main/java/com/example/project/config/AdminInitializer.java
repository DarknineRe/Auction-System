package com.example.project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.project.model.User;
import com.example.project.service.AdminUserService;

@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final AdminUserService adminUserService;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(AdminUserService adminUserService,
            @Value("${app.admin.email:}") String adminEmail,
            @Value("${app.admin.password:}") String adminPassword) {
        this.adminUserService = adminUserService;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD not set - skipping default admin creation");
            return;
        }

        boolean created = adminUserService.createAdminIfAbsent("Super Administrator", adminEmail, adminPassword);
        log.info(created ? "Default super admin account created"
                : "Default super admin account not created; see account conflict warning if applicable");
    }
}