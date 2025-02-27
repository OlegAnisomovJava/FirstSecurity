package ru.anisimov.springsecurity.FirstSecurity.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @PersistenceContext
    private EntityManager entityManager;

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

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("🔍 Пытаемся загрузить пользователя с email: [" + email + "]");

        if (email == null || email.isEmpty()) {
            throw new UsernameNotFoundException("❌ Email не может быть пустым!");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("❌ Пользователь не найден: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles()) // Преобразуем роли в GrantedAuthority
        );
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

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

        user.setRoles(Set.of(userRole)); // ✅ Используем уже найденную роль
        user.setPassword(passwordEncoder.encode(user.getPassword())); // ✅ Хешируем пароль

        userRepository.save(user);
        return true;
    }

    @Transactional
    public void updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        System.out.println("🔄 Обновление пользователя: " + user.getEmail());
        System.out.println("🔑 Текущий пароль в БД: " + existingUser.getPassword());
        System.out.println("🛠 Новый пароль (до шифрования): " + user.getPassword());

        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            if (!user.getPassword().startsWith("$2a$")) { // Проверяем, не пришел ли уже хешированный пароль
                String encodedPassword = passwordEncoder.encode(user.getPassword());
                System.out.println("🔒 Новый пароль (зашифрованный): " + encodedPassword);
                existingUser.setPassword(encodedPassword);
            } else {
                System.out.println("⚠ Пароль уже хеширован, не шифруем повторно!");
                existingUser.setPassword(user.getPassword()); // Оставляем его как есть
            }
        } else {
            System.out.println("⚠ Поле пароля пустое, оставляем старый пароль!");
        }


        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setAge(user.getAge());
        existingUser.setEmail(user.getEmail());

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            existingUser.setRoles(user.getRoles());
        }

        // 🔍 Лог перед сохранением
        System.out.println("🛠 Финальное состояние объекта перед сохранением:");
        System.out.println("   🔑 Пароль (зашифрованный): " + existingUser.getPassword());
        System.out.println("   🏷 Email: " + existingUser.getEmail());
        System.out.println("   🏷 Роли: " + existingUser.getRoles());

        // ❗ Оставляем только save(), убираем merge()
        userRepository.save(existingUser);

        System.out.println("✅ Пользователь обновлен в базе.");
    }



    // ✅ Удаление пользователя
    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) { // 🔍 Проверяем, существует ли пользователь
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            // 1️⃣ Удаляем связи пользователя с ролями
            user.getRoles().clear();
            userRepository.save(user);

            // 2️⃣ Теперь можно удалить пользователя
            userRepository.deleteById(id);
            // 🗑 Удаляем пользователя
            return true; // ✅ Успешное удаление
        }
        return false; // ❌ Пользователь не найден
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
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }


}
