package com.vtlong.my_spring_boot_project.dto.request;

import java.time.LocalDate;
import java.util.Set;

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
public class UpdateUserRequestDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String phone;
    private String address;
    private Set<Role> roles;
}
