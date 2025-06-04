package com.fladx.otpservice.config;

import com.fladx.otpservice.model.user.UserRole;
import com.fladx.otpservice.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Slf4j
@Configuration
@EnableWebSecurity
public class SecuritySetup {

    // Настраиваем цепочку фильтров безопасности
    @Bean
    public SecurityFilterChain setupSecurity(HttpSecurity http, JwtAuthFilter jwtFilter) throws Exception {
        // Отключаем CSRF - не нужно для API
        http.csrf(c -> c.disable());
        
        // Настройка CORS - разрешаем все источники
        http.cors(cors -> cors.configurationSource(corsConfig()));
        
        // Настройка доступа к эндпоинтам
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN") // Только админы
                .requestMatchers("/auth/**", "/", "/error", "/swagger-ui/**", "/v3/api-docs/**").permitAll() // Доступ всем
                .anyRequest().authenticated() // Остальное - только авторизованным
        );
        
        // Обработка ошибок авторизации
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, ex) -> {
                    log.warn("Кто-то пытается достучаться без авторизации: {}", request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Требуется авторизация");
                })
                .accessDeniedHandler((request, response, ex) -> {
                    log.warn("Попытка доступа без прав: {}", request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Доступ запрещен");
                })
        );
        
        // Делаем сессии без состояния (stateless)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        
        // Добавляем наш JWT-фильтр
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    // Настройка CORS - разрешаем всем
    @Bean
    public CorsConfigurationSource corsConfig() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*")); // Можно указать конкретные домены вместо "*"
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setMaxAge(3600L); // Кешируем настройки на 1 час
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Используем BCrypt для хеширования паролей
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Бин для аутентификации
    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
