package ru.anisimov.springsecurity.FirstSecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.service.UserService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ Главная страница админки
    @GetMapping
    public String userList(Model model) {
        model.addAttribute("allUsers", userService.allUsers());
        return "admin";
    }

    // ✅ Форма добавления нового пользователя
    @GetMapping("/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "addUser"; // Файл addUser.html должен быть в /resources/templates
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute User user,
                          @RequestParam String role,
                          @RequestParam String password) {
        user.setPassword(password); // Пароль хешируется внутри `saveUser()`

        if (!userService.saveUser(user, role)) { // ✅ Теперь передаём роль
            return "redirect:/admin?error=user-exists";
        }
        return "redirect:/admin";
    }


    // ✅ Страница редактирования пользователя
    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        Optional<User> userOptional = Optional.ofNullable(userService.findUserById(id));
        if (userOptional.isEmpty()) {
            return "redirect:/admin";
        }
        model.addAttribute("user", userOptional.get());
        return "editUser";
    }

    // ✅ Обновление пользователя с возможностью смены пароля
    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam String role,
                             @RequestParam(required = false) String password) {

        Optional<User> existingUserOptional = Optional.ofNullable(userService.findUserById(user.getId()));
        if (existingUserOptional.isEmpty()) {
            return "redirect:/admin";
        }
        User existingUser = existingUserOptional.get();

        Set<Role> roles = new HashSet<>();
        roles.add(new Role("ROLE_ADMIN".equals(role) ? 2L : 1L, role));
        user.setRoles(roles);

        // ✅ Обновляем пароль, если пользователь ввёл новый
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            user.setPassword(existingUser.getPassword()); // Сохраняем старый пароль
        }

        userService.updateUser(user);
        return "redirect:/admin";
    }

    @GetMapping("/delete/{id}")
    public String confirmDeleteUser(@PathVariable("id") Long id, Model model) {
        User user = userService.findUserById(id);
        if (user == null) {
            return "redirect:/admin";
        }
        model.addAttribute("user", user);
        return "deleteUser"; // Теперь будет отображаться страница подтверждения
    }

    // ✅ Подтверждение удаления
    @PostMapping("/deleteConfirmed")
    public String deleteConfirmed(@RequestParam("userId") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
