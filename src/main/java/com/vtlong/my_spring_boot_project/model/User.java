package com.vtlong.my_spring_boot_project.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Index;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@Builder
@ToString(of = {"id", "username", "email", "firstName", "lastName"})
@EqualsAndHashCode(of = {"id", "username", "email"})
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_username", columnList = "username", unique = true),
    @Index(name = "idx_users_email", columnList = "email", unique = true)
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @ManyToMany
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
        indexes = {
            @Index(name = "idx_user_roles_user_id", columnList = "user_id"),
            @Index(name = "idx_user_roles_role_id", columnList = "role_id")
        }
    )
    private Set<Role> roles;

    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "First name can only contain letters and spaces")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Last name can only contain letters and spaces")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]{10,15}$", message = "Phone number must be valid")
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
