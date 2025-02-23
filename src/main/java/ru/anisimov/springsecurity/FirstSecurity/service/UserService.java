package ru.anisimov.springsecurity.FirstSecurity.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
    @PersistenceContext
    private EntityManager em;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        return user;
    }
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        Optional<User> userFromDb = userRepository.findById(userId);
        return userFromDb.orElse(new User());
    }
    @Transactional
    public boolean saveUser(User user, String roleName) {
        User userFromDB = userRepository.findByUsername(user.getUsername());

        if (userFromDB != null) {
            return false;
        }

        Role role = roleRepository.findByName(roleName); // Возможно, тут ошибка!
        user.setRoles(Collections.singleton(role));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return true;
    }
    @Transactional
    public boolean updateUser(User user) {
        if (userRepository.existsById(user.getId())) {
            User existingUser = userRepository.findById(user.getId()).get();

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                user.setPassword(existingUser.getPassword()); // Оставляем старый пароль
            } else {
                user.setPassword(passwordEncoder.encode(user.getPassword())); // Хешируем новый пароль
            }

            userRepository.save(user);
            return true;
        }
        return false;
    }
    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    @Transactional(readOnly = true)
    public List<User> usergtList(Long id) {
        return userRepository.findAll()
                .stream()
                .filter(user -> user.getId() > id)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<User> allUsers() {
        return userRepository.findAll();
    }
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}