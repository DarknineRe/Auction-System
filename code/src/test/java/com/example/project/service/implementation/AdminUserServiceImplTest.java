package com.example.project.service.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.project.model.User;
import com.example.project.repository.UserRepository;

class AdminUserServiceImplTest {

    @Test
    void doesNotTakeOverAnExistingNonSuperAdminAccount() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        User existingUser = new User();
        existingUser.setEmail("admin@example.test");
        existingUser.setName("Existing user");
        existingUser.setPassword("existing-password-hash");
        existingUser.setRole(User.Role.USER);
        existingUser.setEnabled(false);
        when(userRepository.findByEmail("admin@example.test")).thenReturn(Optional.of(existingUser));
        AdminUserServiceImpl service = new AdminUserServiceImpl(userRepository, passwordEncoder);

        boolean created = service.createAdminIfAbsent("Configured admin", "admin@example.test", "new-password");

        assertFalse(created);
        assertEquals("Existing user", existingUser.getName());
        assertEquals("existing-password-hash", existingUser.getPassword());
        assertEquals(User.Role.USER, existingUser.getRole());
        assertFalse(existingUser.isEnabled());
        verify(userRepository, never()).save(existingUser);
        verify(passwordEncoder, never()).encode("new-password");
    }
}
