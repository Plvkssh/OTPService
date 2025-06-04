package com.fladx.otpservice.service;

import com.fladx.otpservice.dto.UserDto;
import com.fladx.otpservice.exception.AdminExistsException;
import com.fladx.otpservice.exception.UserNotFoundException;
import com.fladx.otpservice.exception.UserAlreadyExistsException;
import com.fladx.otpservice.model.user.User;
import com.fladx.otpservice.model.user.UserRole;
import com.fladx.otpservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для работы с пользователями: регистрация, поиск, управление.
 * Реализует UserDetailsService для интеграции со Spring Security.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Загружает пользователя по username для Spring Security
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    @Transactional(readOnly = true)
    public User loadUserByUsername(String username) throws UserNotFoundException {
        return findUserByUsername(username);
    }

    /**
     * Поиск пользователя по логину
     * @throws UserNotFoundException если пользователь не существует
     */
    @Transactional(readOnly = true)
    public User findUserByUsername(String username) throws UserNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Попытка входа несуществующего пользователя: {}", username);
                    return new UserNotFoundException("Пользователь не найден");
                });
    }

    /**
     * Проверяет существование пользователя по логину
     */
    @Transactional(readOnly = true)
    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     * Создает нового пользователя (USER)
     * @throws UserAlreadyExistsException если логин занят
     * @throws AdminExistsException при попытке создать второго ADMIN
     */
    @Transactional
    public User createUser(UserDto userDto) {
        validateUserCreation(userDto);

        User newUser = buildUserFromDto(userDto);
        User savedUser = userRepository.save(newUser);
        
        log.info("Создан новый пользователь: {}", savedUser.getUsername());
        return savedUser;
    }

    /**
     * Возвращает текущего аутентифицированного пользователя
     */
    public User getCurrentAuthenticatedUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    /**
     * Получает список всех обычных пользователей (без ADMIN)
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllRegularUsers() {
        return userRepository.findByRole(UserRole.USER).stream()
                .map(this::convertToDto)
                .toList();
    }

    /**
     * Удаляет пользователя по ID
     */
    @Transactional
    public void deleteUserAccount(Long userId) {
        userRepository.deleteById(userId);
        log.info("Удален пользователь с ID: {}", userId);
    }

    // --- Вспомогательные методы ---
    
    private void validateUserCreation(UserDto userDto) {
        if (isUsernameTaken(userDto.username())) {
            throw new UserAlreadyExistsException("Логин уже занят");
        }
        if (userDto.role() == UserRole.ADMIN) {
            throw new AdminExistsException("Администратор уже существует");
        }
    }

    private User buildUserFromDto(UserDto userDto) {
        return User.builder()
                .username(userDto.username())
                .password(passwordEncoder.encode(userDto.password()))
                .role(userDto.role())
                .email(userDto.email())
                .phoneNumber(userDto.phoneNumber())
                .telegramId(userDto.telegramId())
                .build();
    }

    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .telegramId(user.getTelegramId())
                .build();
    }
}
