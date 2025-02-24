package ru.anisimov.springsecurity.FirstSecurity.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.anisimov.springsecurity.FirstSecurity.model.User;

@Controller
public class UserController {

    // ✅ Страница пользователя
    @GetMapping("/user")
    public String userProfile(@AuthenticationPrincipal User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("lastName", user.getLastName());
        model.addAttribute("age", user.getAge());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("roles", user.getRoles());

        return "user";
    }
}
