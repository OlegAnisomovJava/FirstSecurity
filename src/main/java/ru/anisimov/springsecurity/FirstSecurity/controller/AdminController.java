package ru.anisimov.springsecurity.FirstSecurity.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.repository.RoleRepository;
import ru.anisimov.springsecurity.FirstSecurity.repository.UserRepository;
import ru.anisimov.springsecurity.FirstSecurity.service.RoleService;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleService roleService;

    @GetMapping
    public String adminPage(Model model, @AuthenticationPrincipal UserDetails currentUser) {
        List<User> users = userRepository.findAllWithRoles(); // Загружаем всех пользователей

        // Найти текущего пользователя в БД по email
        User user = userRepository.findByEmail(currentUser.getUsername()).orElse(null);

        model.addAttribute("users", users);
        model.addAttribute("currentUser", user); // Добавляем currentUser в модель
        return "admin";
    }





    @PostMapping("/addUser")
    public String addUser(@ModelAttribute User user, @RequestParam List<String> roleNames) {
        Set<Role> roleSet = new HashSet<>();

        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName) // 🔥 Ищем роль в БД
                    .orElseThrow(() -> new RuntimeException("Роль не найдена: " + roleName));
            roleSet.add(role);
        }

        user.setRoles(roleSet); // 🔥 Устанавливаем роли корректно
        userService.saveUser(user); // Сохраняем пользователя

        return "redirect:/admin";
    }


    @GetMapping("/edit/{id}")
    public String showEditUserForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin";
        }
        model.addAttribute("user", user);
        return "editUser";
    }

    @PostMapping("/update")
    public String updateUser(@RequestParam Long id,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam int age,
                             @RequestParam String email,
                             @RequestParam(required = false) String password,
                             @RequestParam(required = false) List<String> roleNames) {

        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin"; // Если пользователь не найден, редирект обратно
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setAge(age);
        user.setEmail(email);

        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        if (roleNames != null && !roleNames.isEmpty()) {
            Set<Role> roles = roleNames.stream()
                    .map(roleService::findRoleByName) // Исправлено
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        userService.updateUser(user);
        return "redirect:/admin";
    }

    // ✅ Подтверждение удаления
    @GetMapping("/delete/{id}")
    public String confirmDeleteUser(@PathVariable("id") Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/admin";
        }
        model.addAttribute("user", user);
        return "deleteUser";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin";
    }
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }
}
