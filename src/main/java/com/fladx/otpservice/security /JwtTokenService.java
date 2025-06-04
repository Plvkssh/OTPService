package com.fladx.otpservice.security;

import com.fladx.otpservice.model.user.User;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Генерация и проверка JWT токенов для аутентификации пользователей.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenService {

    @Value("${security.jwt.secret}")
    private String base64SecretKey;  // Секретный ключ в Base64

    @Value("${security.jwt.expirationMs}")
    private long tokenLifetimeMs;    // Время жизни токена в миллисекундах

    /**
     * Создает JWT токен для пользователя.
     * @param user Данные пользователя (используется username)
     * @return Строка с JWT токеном
     */
    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenLifetimeMs);

        return Jwts.builder()
                .subject(user.getUsername())  // Логин пользователя
                .issuedAt(now)                // Время создания
                .expiration(expiryDate)       // Срок действия
                .signWith(getSigningKey())    // Подпись
                .compact();
    }

    /**
     * Извлекает имя пользователя из токена.
     * @throws JwtException если токен невалидный
     */
    public String extractUsername(String token) {
        return parseToken(token).getSubject();
    }

    /**
     * Проверяет валидность токена.
     * @return true если токен подписан правильно и не истек
     */
    public boolean isTokenValid(String token) {
        try {
            Date expiryDate = parseToken(token).getExpiration();
            return !expiryDate.before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }

    // --- Вспомогательные методы ---

    /**
     * Преобразует Base64 ключ в объект SecretKey для подписи
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(base64SecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Разбирает токен и возвращает его claims (утверждения)
     * @throws JwtException если подпись неверная или токен поврежден
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
