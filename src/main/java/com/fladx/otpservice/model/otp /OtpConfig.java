package com.fladx.otpservice.model.otp;

import jakarta.persistence.*;
import lombok.*;

/**
 * Настройки генерации OTP-кодов:
 * - Длина кода (например 6 цифр)
 * - Время жизни кода в секундах
 * 
 * Хранится в виде единственной записи в таблице (id=1)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "otp_config")
public class OtpConfig {

    /**
     * Всегда равен 1 - так мы храним единственный набор настроек
     */
    @Id
    private Integer id = 1;

    /**
     * Сколько цифр должно быть в коде
     * Пример: 6 → "123456"
     */
    @Column(name = "code_length", nullable = false)
    private Integer codeLength;

    /**
     * Сколько секунд код будет действителен
     * Пример: 300 → 5 минут
     */
    @Column(name = "ttl_seconds", nullable = false)
    private Long ttlSeconds;

    /**
     * Конструктор для удобного создания настроек
     */
    @Builder
    public OtpConfig(Integer codeLength, Long ttlSeconds) {
        this.codeLength = codeLength;
        this.ttlSeconds = ttlSeconds;
    }
}
