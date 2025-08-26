package com.vtlong.my_spring_boot_project.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.vtlong.my_spring_boot_project.model.Role;
import com.vtlong.my_spring_boot_project.model.RoleType;
import com.vtlong.my_spring_boot_project.repository.RoleRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Starting default data initialization...");

        initializeRoles();

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
}
