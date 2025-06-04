package com.fladx.otpservice.controller;

import com.fladx.otpservice.dto.UserDto;
import com.fladx.otpservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthenticationController {

    private final AuthService authManager;

    /**
     * Регистрирует нового пользователя в системе
     * @param userData Данные пользователя (логин, пароль и контакты)
     * @return Токен для доступа
     */
    @PostMapping("/signup")
    public String registerUser(@RequestBody @Valid UserDto userData) {
        return authManager.registerNewUser(userData);
    }

    /**
     * Авторизует пользователя и выдает токен
     * @param credentials Логин и пароль
     * @return JWT-токен для доступа к API
     */
    @PostMapping("/signin")
    public String authenticateUser(@RequestBody @Valid UserDto credentials) {
        return authManager.validateCredentials(credentials);
    }
}
