package ru.anisimov.springsecurity.FirstSecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.service.UserServiceInterface;

import java.util.List;

@RestController
@RequestMapping("/api/users")// или "/api/users", если ты хочешь
@CrossOrigin(origins = "*") // Разрешаем запросы с других источников
public class RestUserController {

    @Autowired
    private UserServiceInterface userService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.allUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }


    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {
        System.out.println("Создание пользователя: " + user);
        if (userService.saveUser(user)) {
            return ResponseEntity.ok("Пользователь успешно создан");
        }
        return ResponseEntity.badRequest().body("Ошибка: пользователь с таким email уже существует");
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateUser(user);
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
