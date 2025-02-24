package ru.anisimov.springsecurity.FirstSecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);  // ✅ Поиск роли по имени

    boolean existsByName(String name);  // ✅ Проверка существования роли
}