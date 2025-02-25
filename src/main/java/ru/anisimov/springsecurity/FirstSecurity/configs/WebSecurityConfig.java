package ru.anisimov.springsecurity.FirstSecurity.configs;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CustomSuccessHandler customSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/registration").permitAll() // Логин доступен всем
                        .requestMatchers("/admin").hasRole("ADMIN") // Доступ к /admin только у админа
                        .requestMatchers("/user").hasAnyRole("USER", "ADMIN") // Пользователь и админ могут зайти на /user
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")  // Используем кастомную страницу
                        .successHandler(customSuccessHandler) // Используем кастомный обработчик входа
                        .failureUrl("/login?error") // Ошибка — снова на логин
                        .usernameParameter("username") // Email вместо username
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
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
