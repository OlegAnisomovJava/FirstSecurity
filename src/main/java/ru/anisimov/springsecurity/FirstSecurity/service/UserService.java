package ru.anisimov.springsecurity.FirstSecurity.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
public class UserService implements UserServiceInterface, UserDetailsService { // ✅ Реализуем оба интерфейса

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("❌ Пользователь не найден: " + email));

        System.out.println("✅ Найден пользователь: " + user.getEmail());
        System.out.println("🎭 Роли пользователя до маппинга: " + user.getRoles());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles()) // 📌 Ошибка может быть тут!
        );
    }




    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new RuntimeException("❌ Ошибка: у пользователя нет назначенных ролей!");
        }

        return roles.stream()
                .map(role -> {
                    if (role.getName() == null || role.getName().isEmpty()) {
                        throw new RuntimeException("❌ Ошибка: у роли нет названия! " + role);
                    }
                    return new SimpleGrantedAuthority(role.getName());
                })
                .collect(Collectors.toList());
    }




    @Transactional
    @Override
    public boolean saveUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            return false;
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Роль ROLE_USER не найдена!"));

        user.setRoles(Set.of(userRole));
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
        return true;
    }

    @Transactional
    @Override
    public void updateUser(User updatedUser) {
        User user = userRepository.findById(updatedUser.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setAge(updatedUser.getAge());
        user.setEmail(updatedUser.getEmail());

        // ✅ Если передан пароль, обновляем его
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        // ✅ Загружаем роли из БД по ID (чтобы не передавать null)
        Set<Role> updatedRoles = updatedUser.getRoles().stream()
                .map(role -> roleRepository.findById(role.getId())
                        .orElseThrow(() -> new RuntimeException("Роль с ID " + role.getId() + " не найдена")))
                .collect(Collectors.toSet());

        user.setRoles(updatedRoles);

        userRepository.save(user);
    }





    @Transactional
    @Override
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            user.getRoles().clear();
            userRepository.save(user);

            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    @Override
    public List<User> usergtList(Long id) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getId() > id)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public List<User> allUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
    }
    @Transactional
    public User getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.getRoles().size(); // Принудительная инициализация ролей
        }
        return user;
    }


    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Получаем всех пользователей
    }
}
