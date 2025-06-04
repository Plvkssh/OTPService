package com.fladx.otpservice.service.notification;

import com.fladx.otpservice.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Отправляет OTP-коды по электронной почте.
 * Работает асинхронно (@Async), чтобы не блокировать основной поток.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotifier implements NotificationSender {

    private final JavaMailSender emailClient;

    @Value("${spring.mail.from}")
    private String senderEmail; // Email отправителя (например no-reply@otpservice.com)

    /**
     * Отправляет OTP-код пользователю на email.
     * @param otpCode Одноразовый код для отправки (например "123456")
     * @param user Получатель (должен иметь email)
     */
    @Override
    @Async
    public void send(String otpCode, User user) {
        String recipientEmail = user.getEmail();
        
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.warn("Пропускаем отправку: пользователь {} не имеет email", user.getUsername());
            return;
        }

        try {
            SimpleMailMessage email = composeEmail(recipientEmail, otpCode);
            emailClient.send(email);
            log.debug("OTP-код отправлен на {}", recipientEmail);
        } catch (Exception e) {
            log.error("Не удалось отправить email на {}", recipientEmail, e);
        }
    }

    /**
     * Создает email сообщение с OTP-кодом.
     */
    private SimpleMailMessage composeEmail(String to, String otpCode) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(senderEmail);
        email.setTo(to);
        email.setSubject("Ваш код подтверждения");
        email.setText(String.format(
            "Ваш одноразовый код: %s\nНикому не сообщайте этот код!", 
            otpCode
        ));
        return email;
    }
}
