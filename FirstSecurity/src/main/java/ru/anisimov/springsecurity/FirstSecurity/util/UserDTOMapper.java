package ru.anisimov.springsecurity.FirstSecurity.util;

import org.springframework.stereotype.Component;
import ru.anisimov.springsecurity.FirstSecurity.dto.UserDTO;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.dto.RoleDTO;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserDTOMapper {


    public static UserDTO toDTO(User user) {
        List<RoleDTO> roleDTOs = user.getRoles().stream()
                .map(role -> new RoleDTO(role.getId(), role.getName()))
                .collect(Collectors.toList());
        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getAge(), roleDTOs);
    }

    public static User toEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(userDTO.getPassword());
        }
        if (userDTO.getRoles() != null) {
            List<Role> roles = userDTO.getRoles().stream()
                    .map(roleDTO -> new Role(roleDTO.getId(), roleDTO.getName()))
                    .collect(Collectors.toList());
            user.setRoles(new HashSet<>(roles));
        }

        return user;
    }


    public static void updateEntityFromDTO(UserDTO userDTO, User user) {
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());

        List<Role> roles = userDTO.getRoles().stream()
                .map(roleDTO -> new Role(roleDTO.getId(), roleDTO.getName()))
                .collect(Collectors.toList());
        user.setRoles(new HashSet<>(roles));
    }
}
