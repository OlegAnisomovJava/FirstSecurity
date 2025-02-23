package ru.anisimov.springsecurity.FirstSecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.service.UserService;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration"; // Показываем форму регистрации
    }

    @PostMapping("/registration")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               Model model) {
        if (userService.findByUsername(username) != null) {
            model.addAttribute("error", "Пользователь с таким именем уже существует!");
            return "registration";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        userService.saveUser(user, "ROLE_USER");

        return "redirect:/login"; // ✅ После успешной регистрации редиректим на стандартное окно входа
    }
}
