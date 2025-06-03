package com.fladx.otpservice.service;

import com.fladx.otpservice.dto.UserDto;
import com.fladx.otpservice.model.user.UserRole;
import com.fladx.otpservice.model.user.User;
import com.fladx.otpservice.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return findByUsername(username);
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.info("User not found. username: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public User create(UserDto request) {
        if (existsByUsername(request.username())) {
            log.info("Username already exists");
            throw new EntityExistsException("User already exists");
        }

        if (request.role().equals(UserRole.ADMIN)) {
            log.info("Admin already exists");
            throw new EntityExistsException("Admin already exists");
        }

        User user = User.builder()
                .password(passwordEncoder.encode(request.password()))
                .username(request.username())
                .role(request.role())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .telegramId(request.telegramId())
                .build();

        log.info("Creating user: {}", user);
        return userRepository.save(user);
    }

    public User getAuthorizedUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findByRole(UserRole.USER);
        return users.stream()
                .map(user -> UserDto.builder()
                        .telegramId(user.getTelegramId())
                        .email(user.getEmail())
                        .phoneNumber(user.getPhoneNumber())
                        .username(user.getUsername())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
