package com.vtlong.my_spring_boot_project.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.vtlong.my_spring_boot_project.model.Role;
import com.vtlong.my_spring_boot_project.model.RoleType;
import com.vtlong.my_spring_boot_project.model.User;
import com.vtlong.my_spring_boot_project.model.Gender;
import com.vtlong.my_spring_boot_project.repository.RoleRepository;
import com.vtlong.my_spring_boot_project.repository.UserRepository;

import java.time.LocalDate;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByName(roleType)) {
                Role role = Role.builder()
                        .name(roleType)
                        .build();

                roleRepository.save(role);
            }
        }
    }

    private void initializeAdminUser() {
        if (userRepository.existsByEmail("admin@example.com")) {
            return;
        }

        Set<Role> allRoles = roleRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toSet());

        if (allRoles.isEmpty()) {
            return;
        }

        User adminUser = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("12345678"))
                .firstName("Long")
                .lastName("Vt")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .phone("+84 123 456 789")
                .address("Hanoi, Vietnam")
                .roles(allRoles)
                .build();

        userRepository.save(adminUser);
    }
}