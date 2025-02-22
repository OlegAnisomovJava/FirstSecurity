package ru.anisimov.springsecurity.FirstSecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.service.UserService;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Controller
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/admin")
    public String userList(Model model) {
        model.addAttribute("allUsers", userService.allUsers());
        return "admin";
    }

    @PostMapping("/admin/delete")
    public String deleteUser(@RequestParam("userId") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin"; // После удаления перенаправляем на список пользователей
    }

    @GetMapping("/admin/gt/{userId}")
    public String  gtUser(@PathVariable("userId") Long userId, Model model) {
        model.addAttribute("allUsers", userService.usergtList(userId));
        return "admin";
    }
    @PostMapping("/admin/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String role, // Теперь передаем роль
                          Model model) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        userService.saveUser(user, role); // ✅ Теперь передаем и юзера, и роль

        return "redirect:/admin";
    }
    @PostMapping("/admin/update")
    public String updateUser(@RequestParam Long userId,
                             @RequestParam String username,
                             @RequestParam(required = false) String password,
                             @RequestParam String role) {
        User user = userService.findUserById(userId);
        if (user != null) {
            user.setUsername(username);
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            Set<Role> roles = new HashSet<>();
            roles.add(new Role(role.equals("ROLE_ADMIN") ? 2L : 1L, role));
            user.setRoles(roles);

            userService.updateUser(user);
        }
        return "redirect:/admin";
    }
}
