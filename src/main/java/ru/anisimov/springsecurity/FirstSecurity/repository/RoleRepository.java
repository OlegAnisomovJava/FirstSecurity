package ru.anisimov.springsecurity.FirstSecurity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
