package com.vtlong.my_spring_boot_project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

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
        logger.info("Starting default data initialization...");

        initializeRoles();
        initializeAdminUser();

        logger.info("Completed default data initialization!");
    }

    private void initializeRoles() {
        logger.info("Initializing default roles...");

        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByName(roleType)) {
                Role role = Role.builder()
                        .name(roleType)
                        .build();

                Role savedRole = roleRepository.save(role);
                logger.info("Created role: {} with ID: {}", roleType.getCode(), savedRole.getId());
            } else {
                logger.debug("Role {} already exists, skipping", roleType.getCode());
            }
        }

        logger.info("Completed roles initialization!");
    }

    private void initializeAdminUser() {
        logger.info("Initializing admin user...");

        if (userRepository.existsByEmail("admin@example.com")) {
            logger.info("Admin user already exists, skipping");
            return;
        }

        Set<Role> allRoles = roleRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toSet());

        if (allRoles.isEmpty()) {
            logger.warn("No roles found, cannot create admin user");
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

        User savedUser = userRepository.save(adminUser);
        logger.info("Created admin user: {} with ID: {} and {} roles",
                savedUser.getUsername(), savedUser.getId(), savedUser.getRoles().size());

        logger.info("Admin user credentials - Email: admin@example.com, Password: 12345678");
    }
}