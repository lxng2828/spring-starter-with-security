package com.vtlong.my_spring_boot_project.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vtlong.my_spring_boot_project.service.UserService;
import com.vtlong.my_spring_boot_project.dto.response.UserResponseDto;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponseDto getCurrentUser() {
        return userService.getCurrentUser();
    }

    @GetMapping("/authorities")
    public Map<String, Object> getCurrentUserAuthorities() {
        Map<String, Object> response = new HashMap<>();
        response.put("authorities", userService.getCurrentUserAuthorities());
        return response;
    }
}
