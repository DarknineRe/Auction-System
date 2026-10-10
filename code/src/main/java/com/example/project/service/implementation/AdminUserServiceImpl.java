package com.example.project.service.implementation;

import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import com.example.project.service.AdminUserService;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private static final Logger log = LoggerFactory.getLogger(AdminUserServiceImpl.class);
    private static final Set<String> SORTABLE_FIELDS = Set.of("id", "name", "email", "role", "enabled");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public boolean createAdminIfAbsent(String name, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        var existingUser = userRepository.findByEmail(normalizedEmail);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getRole() == User.Role.SUPER_ADMIN) {
                return false; // Already a super admin
            }
            log.warn("Default super admin was not created: configured email {} already belongs to a non-super-admin account",
                    normalizedEmail);
            return false;
        }

        User admin = new User();
        admin.setName(name.trim());
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(rawPassword));
        admin.setRole(User.Role.SUPER_ADMIN);
        admin.setEnabled(true);
        userRepository.save(admin);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<User> getUsers(User.Role role, Pageable pageable) {
        validateSort(pageable);
        return role == null
                ? userRepository.findAll(pageable)
                : userRepository.findByRole(role, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return findUserById(userId);
    }

    @Override
    @Transactional
    public User setEnabled(String actorEmail, Long userId, boolean enabled) {
        User target = findUserById(userId);
        rejectSelfChange(actorEmail, target);
        
        // Prevent non-super admins from disabling other admins/super admins
        if (target.getRole() == User.Role.SUPER_ADMIN) {
            // Only super admins can modify super admins
            var actor = userRepository.findByEmail(actorEmail.trim().toLowerCase(Locale.ROOT)).orElse(null);
            if (actor == null || actor.getRole() != User.Role.SUPER_ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only super admin can modify super admin");
            }
        } else if (target.getRole() == User.Role.ADMIN) {
            // Super admins can modify regular admins
            var actor = userRepository.findByEmail(actorEmail.trim().toLowerCase(Locale.ROOT)).orElse(null);
            if (actor == null || actor.getRole() != User.Role.SUPER_ADMIN) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only super admin can modify admin");
            }
        }

        target.setEnabled(enabled);
        return userRepository.save(target);
    }

    @Override
    @Transactional
    public User promoteToAdmin(String actorEmail, Long userId) {
        User actor = userRepository.findByEmail(actorEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Super admin access required"));
        if (actor.getRole() != User.Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Super admin access required");
        }

        User target = findUserById(userId);
        rejectSelfChange(actorEmail, target);
        if (target.getRole() != User.Role.USER) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only regular users can be promoted to admin");
        }

        target.setRole(User.Role.ADMIN);
        return userRepository.save(target);
    }

    @Override
    @Transactional
    public User demoteAdminToUser(String actorEmail, Long userId) {
        User actor = userRepository.findByEmail(actorEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Super admin access required"));
        if (actor.getRole() != User.Role.SUPER_ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Super admin access required");
        }

        User target = findUserById(userId);
        rejectSelfChange(actorEmail, target);
        if (target.getRole() != User.Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only admins can be demoted");
        }

        target.setRole(User.Role.USER);
        return userRepository.save(target);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private void rejectSelfChange(String actorEmail, User target) {
        if (target.getEmail().equalsIgnoreCase(actorEmail.trim())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Admins cannot change their own role or status");
        }
    }

    private void validateSort(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORTABLE_FIELDS.contains(order.getProperty())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cannot sort by: " + order.getProperty());
            }
        }
    }
}
