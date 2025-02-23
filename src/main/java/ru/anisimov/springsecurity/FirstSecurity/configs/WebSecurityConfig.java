package ru.anisimov.springsecurity.FirstSecurity.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Отключаем CSRF (лучше включить, если не API)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/registration").permitAll() // Доступ к регистрации всем
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Доступ только админам
                        .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN") // Доступ пользователям и админам
                        .requestMatchers("/", "/resources/**").permitAll() // Главная доступна всем
                        .anyRequest().authenticated() // Остальные страницы - только аутентифицированным пользователям
                )
                .formLogin(login -> login // Используем стандартное окно Spring Security
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true) // Завершаем сессию
                        .deleteCookies("JSESSIONID") // Удаляем куки сессии
                        .permitAll()
                );

        return http.build();
    }
}
