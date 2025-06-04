package com.fladx.otpservice.controller;

import com.fladx.otpservice.dto.otp.GenerateCodeRequestDto;
import com.fladx.otpservice.dto.otp.ValidateCodeRequestDto;
import com.fladx.otpservice.service.otp.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OtpController {
    private final OtpService otpService;
    @PostMapping
    public String sendOtpCode(@RequestBody GenerateCodeRequestDto request) {
        return otpService.generateOtpCode(request);
    }

    @PostMapping("/validate")
    public void checkOtpCode(@RequestBody ValidateCodeRequestDto request) {
        // Если код неверный - сервис сам выбросит ошибку
        otpService.validateCode(request);
    }
}
