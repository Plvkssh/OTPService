package com.fladx.otpservice.security;

import com.fladx.otpservice.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/**
 * Фильтр для JWT-аутентификации.
 * Проверяет каждый запрос на наличие валидного токена в заголовке Authorization.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Пытаемся получить токен из заголовка
        String authHeader = request.getHeader("Authorization");
        
        if (!isValidAuthHeader(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Извлекаем чистый токен (без "Bearer ")
        String jwtToken = authHeader.substring(7);
        
        // 3. Проверяем токен и аутентифицируем пользователя
        if (tokenProvider.isTokenValid(jwtToken)) {
            authenticateUser(jwtToken, request);
        }

        // 4. Пропускаем запрос дальше по цепочке фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Проверяет формат заголовка Authorization
     */
    private boolean isValidAuthHeader(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ");
    }

    /**
     * Аутентифицирует пользователя на основе JWT-токена
     */
    private void authenticateUser(String jwtToken, HttpServletRequest request) {
        // Получаем username из токена
        String username = tokenProvider.extractUsername(jwtToken);
        
        // Загружаем данные пользователя
        UserDetails userDetails = userService.loadUserByUsername(username);
        
        // Создаем объект аутентификации
        var authentication = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
        );
        
        // Добавляем детали запроса
        authentication.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request)
        );
        
        // Устанавливаем аутентификацию в контекст Security
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
