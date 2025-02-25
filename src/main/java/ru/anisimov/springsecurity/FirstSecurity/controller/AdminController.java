package ru.anisimov.springsecurity.FirstSecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.repository.RoleRepository;
import ru.anisimov.springsecurity.FirstSecurity.service.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;


    @GetMapping
    public String adminPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        Optional<User> currentUserOptional = userService.findByEmail(userDetails.getUsername());

        if (currentUserOptional.isEmpty()) {
            return "redirect:/login"; // Если пользователь не найден, редиректим на логин
        }

        model.addAttribute("currentUser", currentUserOptional.get());
        model.addAttribute("allUsers", userService.allUsers());
        return "admin";
    }

    // ✅ Форма добавления нового пользователя
    @GetMapping("/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "addUser";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute User user, @RequestParam List<String> roles, Model model) {
        Set<Role> roleSet = roles.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Роль не найдена: " + roleName)))
                .collect(Collectors.toSet());

        if (roleSet.isEmpty()) {
            model.addAttribute("error", "Ошибка: роли не найдены!");
            return "admin";
        }

        user.setRoles(roleSet);
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Хешируем пароль
        userService.saveUser(user);

        return "redirect:/admin";
    }

    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        User user = userService.findUserById(id);
        if (user == null) {
            return "redirect:/admin";
        }
        model.addAttribute("user", user);
        return "editUser";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute User user,
                             @RequestParam String role,
                             @RequestParam(required = false) String password) {

        User existingUser = userService.findUserById(user.getId());
        if (existingUser == null) {
            return "redirect:/admin";
        }

        Set<Role> roles = new HashSet<>();
        roles.add(new Role("ROLE_ADMIN".equals(role) ? 2L : 1L, role));
        user.setRoles(roles);

        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            user.setPassword(existingUser.getPassword());
        }

        userService.updateUser(user);
        return "redirect:/admin";
    }

    // ✅ Подтверждение удаления
    @GetMapping("/delete/{id}")
    public String confirmDeleteUser(@PathVariable("id") Long id, Model model) {
        User user = userService.findUserById(id);
        if (user == null) {
            return "redirect:/admin";
        }
        model.addAttribute("user", user);
        return "deleteUser";
    }

    @PostMapping("/deleteConfirmed")
    public String deleteConfirmed(@RequestParam("userId") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
}
