package ru.anisimov.springsecurity.FirstSecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.anisimov.springsecurity.FirstSecurity.dto.UserDTO;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.service.UserService;
import ru.anisimov.springsecurity.FirstSecurity.util.UserDTOMapper;

import java.util.Collections;
import java.util.Optional;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    public String registrationForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUser(@RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam int age,
                               Model model) {

        if (userService.findByEmail(email).isPresent()) {
            model.addAttribute("emailError", "Пользователь с таким email уже существует!");
            model.addAttribute("user", new UserDTO());
            return "registration";
        }
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setPassword(password);
        userDTO.setFirstName(firstName);
        userDTO.setLastName(lastName);
        userDTO.setAge(age);

        User user = UserDTOMapper.toEntity(userDTO);
        Optional<Role> userRole = userService.getRoleByName("ROLE_USER");
        userRole.ifPresent(role -> user.setRoles(Collections.singleton(role)));
        userService.saveUser(user);

        return "redirect:/login";
    }
}
