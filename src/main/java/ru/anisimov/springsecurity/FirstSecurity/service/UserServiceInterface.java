package ru.anisimov.springsecurity.FirstSecurity.service;

import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;

import java.util.List;
import java.util.Optional;

public interface UserServiceInterface {
    boolean saveUser(User user);
    void updateUser(User user);
    boolean deleteUser(Long id);
    List<User> usergtList(Long id);
    List<User> allUsers();
    Optional<User> findByEmail(String email);
    Role findRoleByName(String roleName);
    User getUserById(Long id);
    List<User> getAllUsers();
}
