package ru.anisimov.springsecurity.FirstSecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.anisimov.springsecurity.FirstSecurity.dto.UserDTO;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.service.UserService;
import ru.anisimov.springsecurity.FirstSecurity.util.UserDTOMapper;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class RestUserController {

    private final UserService userService;
    private final UserDTOMapper userDTOMapper;

    @Autowired
    public RestUserController(UserService userService, UserDTOMapper userDTOMapper) {
        this.userService = userService;
        this.userDTOMapper = userDTOMapper;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> userDTOs = userService.allUsers();
        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.getUserById(id);
        return userDTO != null ? ResponseEntity.ok(userDTO) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody UserDTO userDTO) {
        String password = userDTO.getPassword();
        if (password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        User user = userDTOMapper.toEntity(userDTO);
        userService.saveUser(user);

        return ResponseEntity.ok("User created successfully");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id,
                                             @RequestBody UserDTO userDTO) {
        String password = userDTO.getPassword();
        userService.updateUser(id, userDTO, password);
        return ResponseEntity.ok("Пользователь обновлен");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok("Пользователь удален");
        }
        return ResponseEntity.notFound().build();
    }
}
