package ru.anisimov.springsecurity.FirstSecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.anisimov.springsecurity.FirstSecurity.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // ✅ Находит пользователя по email
    boolean existsByEmail(String email); // ✅ Проверяет, существует ли email
}