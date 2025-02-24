package ru.anisimov.springsecurity.FirstSecurity.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // ❗ Отключаем CSRF (не для PROD)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/registration").permitAll() // Регистрация доступна всем
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Доступ к админке
                        .requestMatchers("/user").hasAnyRole("USER", "ADMIN") // Доступ в профиль всем авторизованным
                        .requestMatchers("/", "/resources/**").permitAll() // Главная доступна всем
                        .anyRequest().authenticated() // Всё остальное - только авторизованным
                )
                .formLogin(login -> login
                        .successHandler(this::redirectBasedOnRole) // ✅ Перенаправление после входа
                        .failureUrl("/login?error")
                        .usernameParameter("email") // ✅ Логинимся по email
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/") // После выхода → на главную
                        .invalidateHttpSession(true) // Завершаем сессию
                        .deleteCookies("JSESSIONID") // Удаляем куки сессии
                        .permitAll()
                );

        return http.build();
    }

    // ✅ Логика редиректа в зависимости от роли пользователя
    private void redirectBasedOnRole(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            response.sendRedirect("/admin");
        } else {
            response.sendRedirect("/user");
        }
    }
}
