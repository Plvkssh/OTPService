package com.fladx.otpservice.service.notification;

import com.fladx.otpservice.model.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.smpp.Session;
import org.smpp.pdu.SubmitSM;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Отправляет SMS с OTP-кодами через SMPP-протокол.
 * Работает асинхронно (@Async), чтобы не блокировать основной поток.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsNotifier implements NotificationSender {

    private final Session smppSession;
    
    @Value("${smpp.source_addr}")
    private String senderPhoneNumber; // Номер отправителя (например "OTPService")

    /**
     * Отправляет SMS с OTP-кодом на телефон пользователя.
     * @param otpCode Одноразовый код (например "123456")
     * @param user Получатель (должен иметь номер телефона)
     */
    @Override
    @Async
    public void send(String otpCode, User user) {
        String phoneNumber = user.getPhoneNumber();
        
        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("Пропускаем отправку SMS: пользователь {} не указал номер телефона", user.getUsername());
            return;
        }

        try {
            SubmitSM sms = createSmsMessage(phoneNumber, otpCode);
            smppSession.submit(sms);
            log.debug("SMS с OTP-кодом отправлена на номер {}", phoneNumber);
        } catch (Exception e) {
            log.error("Ошибка отправки SMS на {}: {}", phoneNumber, e.getMessage());
            // Можно добавить retry логику или уведомление админа
        }
    }

    /**
     * Создает SMPP-сообщение для отправки.
     */
    private SubmitSM createSmsMessage(String phoneNumber, String otpCode) {
        SubmitSM sms = new SubmitSM();
        sms.setSourceAddr(senderPhoneNumber);
        sms.setDestAddr(phoneNumber);
        sms.setShortMessage("Ваш код подтверждения: " + otpCode);
        return sms;
    }
}
