package com.vtlong.my_spring_boot_project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vtlong.my_spring_boot_project.model.Role;
import com.vtlong.my_spring_boot_project.model.RoleType;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleType name);

    boolean existsByName(RoleType name);
}