package com.fladx.otpservice.controller;

import com.fladx.otpservice.dto.UserDto;
import com.fladx.otpservice.dto.otp.OtpConfigDto;
import com.fladx.otpservice.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPanelController {

    private final OtpConfigService configService;
    private final UserService userService;
    private final AdminService adminService;

    // Настройки OTP
    @GetMapping("/config")
    public OtpConfigDto getCurrentConfig() {
        return configService.getCurrentConfig();
    }

    @PostMapping("/config")
    public OtpConfigDto createConfig(@RequestBody OtpConfigDto configRequest) {
        return configService.createNewConfig(configRequest);
    }

    @PutMapping("/config")
    public OtpConfigDto updateConfig(@RequestBody OtpConfigDto configRequest) {
        return configService.updateExistingConfig(configRequest);
    }

    // Управление пользователями
    @GetMapping("/users")
    public List<UserDto> getAllRegisteredUsers() {
        return userService.getAllRegisteredUsers();
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUserAccount(
            @Valid @NotNull @Positive @PathVariable("userId") Long userId
    ) {
        adminService.deleteUserWithRelatedData(userId);
    }
}
