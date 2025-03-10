package ru.anisimov.springsecurity.FirstSecurity.service;

import ru.anisimov.springsecurity.FirstSecurity.dto.UserDTO;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<UserDTO> getAllUsers();
    UserDTO getUserById(Long id);
    UserDTO createUser(UserDTO userDTO, String password);
    UserDTO updateUser(Long id, UserDTO userDTO, String password);
    boolean deleteUser(Long id);
    Optional<User> findByEmail(String email);
    boolean saveUser(User user);
    List<UserDTO> allUsers();
    Optional<Role> getRoleByName(String roleName);
}
