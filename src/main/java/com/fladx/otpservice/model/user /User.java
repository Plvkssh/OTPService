package com.fladx.otpservice.model.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

/**
 * Модель пользователя системы.
 * Хранит:
 * - Учетные данные (логин/пароль)
 * - Контакты (телефон, email, Telegram)
 * - Роль (ADMIN/USER)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    /**
     * Уникальный ID пользователя
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Логин для входа (уникальный)
     * Пример: "ivanov"
     */
    @Column(nullable = false, unique = true)
    private String username;

    /**
     * Зашифрованный пароль
     */
    @Column(nullable = false)
    private String password;

    /**
     * Роль в системе:
     * - ADMIN: полный доступ
     * - USER: обычный пользователь
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * Номер телефона в формате +7/8XXXXXXXXXX
     * Пример: "+79161234567" или "89161234567"
     */
    @Pattern(
        regexp = "^(\\+7|8)\\d{10}$", 
        message = "Номер должен начинаться с +7 или 8 и содержать ровно 11 цифр"
    )
    private String phoneNumber;

    /**
     * ID Telegram-аккаунта
     * Пример: 123456789
     */
    private Long telegramId;

    /**
     * Email-адрес
     * Пример: "user@example.com"
     */
    private String email;

    // Методы для Spring Security

    /**
     * Возвращает список прав пользователя (на основе роли)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Аккаунт не просрочен? (всегда true)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Аккаунт не заблокирован? (всегда true)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Пароль не просрочен? (всегда true)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Аккаунт включен? (всегда true)
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
