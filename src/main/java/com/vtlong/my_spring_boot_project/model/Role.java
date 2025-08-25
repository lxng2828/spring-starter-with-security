package com.vtlong.my_spring_boot_project.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Index;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@ToString(of = { "id", "name" })
@EqualsAndHashCode(of = { "id", "name" })
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles", indexes = { @Index(name = "idx_roles_name", columnList = "name") })
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull(message = "Role type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, length = 20, unique = true)
    private RoleType name;

}