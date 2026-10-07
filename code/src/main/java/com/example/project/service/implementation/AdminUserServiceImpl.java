package com.example.project.service.implementation;

import java.util.Locale;
import java.util.Set;

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
            if (user.getRole() == User.Role.ADMIN) {
                return false; // Already an admin
            }
            // Upgrade existing user to admin
            user.setRole(User.Role.ADMIN);
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setName(name.trim());
            userRepository.save(user);
            return true;}
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

        target.setEnabled(enabled);
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
