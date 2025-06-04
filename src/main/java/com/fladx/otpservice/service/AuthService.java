package com.fladx.otpservice.service;

import com.fladx.otpservice.dto.UserDto;
import com.fladx.otpservice.exception.AuthenticationException;
import com.fladx.otpservice.exception.UserAlreadyExistsException;
import com.fladx.otpservice.model.user.User;
import com.fladx.otpservice.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Сервис для регистрации и аутентификации пользователей.
 * Работает с JWT-токенами и Spring Security.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtTokenService tokenService;
    private final AuthenticationManager authManager;

    /**
     * Регистрирует нового пользователя и возвращает JWT-токен.
     * @param userData Данные для регистрации (username, password, контакты)
     * @return JWT-токен для доступа к API
     * @throws UserAlreadyExistsException если пользователь уже существует
     */
    public String registerUser(UserDto userData) {
        try {
            User newUser = userService.createUser(userData);
            log.info("Зарегистрирован новый пользователь: {}", newUser.getUsername());
            return tokenService.generateToken(newUser);
        } catch (Exception e) {
            log.error("Ошибка регистрации пользователя {}: {}", userData.username(), e.getMessage());
            throw new UserAlreadyExistsException("Пользователь уже существует");
        }
    }

    /**
     * Аутентифицирует пользователя и возвращает JWT-токен.
     * @param credentials Данные для входа (username и password)
     * @return JWT-токен для доступа к API
     * @throws AuthenticationException если аутентификация не удалась
     */
    public String authenticateUser(UserDto credentials) {
        try {
            // Проверяем логин/пароль
            Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    credentials.username(),
                    credentials.password()
                )
            );
            
            User user = (User) auth.getPrincipal();
            log.info("Успешный вход пользователя: {}", user.getUsername());
            
            return tokenService.generateToken(user);
        } catch (Exception e) {
            log.warn("Ошибка аутентификации для {}: {}", credentials.username(), e.getMessage());
            throw new AuthenticationException("Неверный логин или пароль");
        }
    }
}
