package ru.anisimov.springsecurity.FirstSecurity.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anisimov.springsecurity.FirstSecurity.dto.RoleDTO;
import ru.anisimov.springsecurity.FirstSecurity.dto.UserDTO;
import ru.anisimov.springsecurity.FirstSecurity.model.Role;
import ru.anisimov.springsecurity.FirstSecurity.model.User;
import ru.anisimov.springsecurity.FirstSecurity.repository.UserRepository;
import ru.anisimov.springsecurity.FirstSecurity.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.anisimov.springsecurity.FirstSecurity.util.UserDTOMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDTOMapper userDTOMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder, UserDTOMapper userDTOMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDTOMapper = userDTOMapper;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAllWithRoles().stream()
                .map(UserDTOMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(UserDTOMapper::toDTO).orElse(null);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO, String password) {
        User user = userDTOMapper.toEntity(userDTO);
        Set<Role> roles = new HashSet<>();
        if (userDTO.getRoles() != null) {
            for (RoleDTO roleDTO : userDTO.getRoles()) {
                if (roleRepository.existsByName(roleDTO.getName())) {
                    Optional<Role> optionalRole = roleRepository.findByName(roleDTO.getName());
                    optionalRole.ifPresent(roles::add);
                } else {
                    throw new RuntimeException("Роль не существует: " + roleDTO.getName());
                }
            }
        }
        user.setRoles(roles);
        userRepository.save(user);
        return userDTOMapper.toDTO(user);
    }

    @Transactional
    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO, String password) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.getRoles().size();
        userDTOMapper.updateEntityFromDTO(userDTO, user);
        if (password != null && !password.isEmpty()) {
            user.setPassword(password);
        }
        if (userDTO.getRoles() != null) {
            user.getRoles().clear();
            for (RoleDTO roleDTO : userDTO.getRoles()) {
                Role role = roleRepository.findByName(roleDTO.getName())
                        .orElseThrow(() -> new RuntimeException("Роль не существует: " + roleDTO.getName()));
                user.getRoles().add(role);
            }
        }

        saveUser(user);
        return userDTOMapper.toDTO(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return true;
        }
        return false;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public List<UserDTO> allUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserDTOMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + email));
        System.out.println("Stored hashed password: " + user.getPassword());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                mapRolesToAuthorities(user.getRoles())
        );
    }
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new RuntimeException("Ошибка: у пользователя нет назначенных ролей!");
        }

        return roles.stream()
                .map(role -> {
                    if (role.getName() == null || role.getName().isEmpty()) {
                        throw new RuntimeException("Ошибка: у роли нет названия! " + role);
                    }
                    return new SimpleGrantedAuthority(role.getName());
                })
                .collect(Collectors.toList());
    }
    public List<SimpleGrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public boolean saveUser(User user) {
        if (user.getId() != null && userRepository.existsById(user.getId())) {
            User existingUser = userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                if (!user.getPassword().startsWith("$2a$")) {
                    existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
                }
            }

            existingUser.setEmail(user.getEmail());
            Set<Role> managedRoles = new HashSet<>();
            for (Role role : user.getRoles()) {
                Role managedRole = roleRepository.findByName(role.getName())
                        .orElseThrow(() -> new RuntimeException("Роль не найдена: " + role.getName()));
                managedRoles.add(managedRole);
            }
            existingUser.setRoles(managedRoles);

            userRepository.save(existingUser);
            return true;
        }

        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (!user.getPassword().startsWith("$2a$")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Роль ROLE_USER не найдена!"));
        user.setRoles(new HashSet<>(Collections.singleton(userRole)));

        userRepository.save(user);
        return true;
    }


    @Override
    public Optional<Role> getRoleByName(String roleName) {
        return roleRepository.findByName(roleName);
    }
}
