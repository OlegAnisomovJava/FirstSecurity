package ru.anisimov.springsecurity.FirstSecurity.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.repository.RoleRepository;
import ru.anisimov.springsecurity.FirstSecurity.repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Автоматическое создание ролей при старте приложения
    @PostConstruct
    public void initRoles() {
        if (!roleRepository.existsByName("ROLE_USER")) {
            roleRepository.save(new Role(null, "ROLE_USER"));
        }
        if (!roleRepository.existsByName("ROLE_ADMIN")) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
        }
    }

    // ✅ Загружаем пользователя по email
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
    }

    // ✅ Поиск пользователя по ID
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Transactional
    public boolean saveUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return false;
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Роль ROLE_USER не найдена!"));

        user.setRoles(Collections.singleton(userRole)); // ✅ Назначаем роль пользователю
        user.setPassword(passwordEncoder.encode(user.getPassword())); // ✅ Хешируем пароль

        userRepository.save(user);
        return true;
    }



    // ✅ Обновление пользователя
    @Transactional
    public boolean updateUser(User user) {
        Optional<User> existingUserOpt = userRepository.findById(user.getId());

        if (existingUserOpt.isEmpty()) {
            return false;
        }

        User existingUser = existingUserOpt.get();

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setEmail(user.getEmail());

        if (user.getPassword() != null) { // Теперь сюда придёт уже ОРИГИНАЛЬНЫЙ пароль, не хешированный
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }


        userRepository.save(existingUser);
        return true;
    }

    // ✅ Удаление пользователя
    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // ✅ Получение всех пользователей с ID больше указанного
    @Transactional(readOnly = true)
    public List<User> usergtList(Long id) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getId() > id)
                .collect(Collectors.toList());
    }

    // ✅ Получение списка всех пользователей
    @Transactional(readOnly = true)
    public List<User> allUsers() {
        return userRepository.findAll();
    }

    // ✅ Поиск пользователя по email
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }


}
