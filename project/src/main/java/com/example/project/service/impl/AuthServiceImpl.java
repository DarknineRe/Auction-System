package com.example.project.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.domain.entity.User;
import com.example.project.domain.enums.Role;
import com.example.project.dto.response.UserResponse;
import com.example.project.exception.AuthenticationFailedException;
import com.example.project.exception.BusinessRuleException;
import com.example.project.mapper.UserMapper;
import com.example.project.repository.UserRepository;
import com.example.project.service.AuthService;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse register(String name, String email, String rawPassword, String phone, String address) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessRuleException("Email is already registered: " + email);
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setPhone(phone);
        user.setAddress(address);
        user.setRole(Role.USER);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationFailedException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new AuthenticationFailedException("Invalid email or password");
        }

        return userMapper.toResponse(user);
    }
}