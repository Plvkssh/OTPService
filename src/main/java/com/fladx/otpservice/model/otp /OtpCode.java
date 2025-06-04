package com.fladx.otpservice.model.otp;

import com.fladx.otpservice.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * Хранит информацию об одноразовом коде подтверждения (OTP):
 * - Сам код
 * - К какому пользователю привязан
 * - Срок действия
 * - Статус (активен/использован/просрочен)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "otp_codes")
public class OtpCode {

    /**
     * Уникальный ID в базе данных
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Сам OTP-код (например "429517")
     * Уникален в рамках системы
     */
    @Column(unique = true, nullable = false)
    private String code;

    /**
     * Пользователь, которому отправлен код
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * ID операции, для которой создан код
     * (например, подтверждение платежа #12345)
     */
    @Column(name = "operation_id", nullable = false)
    private Long operationId; // Переименовано в camelCase

    /**
     * Текущее состояние кода
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpStatus status;

    /**
     * Когда код перестанет работать
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Когда код был создан (автоматически)
     */
    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Когда код последний раз обновляли (автоматически)
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
