package com.vtlong.my_spring_boot_project.dto.request;

import java.time.LocalDate;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.vtlong.my_spring_boot_project.model.Gender;
import com.vtlong.my_spring_boot_project.model.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequestDto {
    private String username;
    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    private String password;
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private Set<Role> roles;
}
