package com.vtlong.my_spring_boot_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vtlong.my_spring_boot_project.service.UserService;
import com.vtlong.my_spring_boot_project.dto.response.UserResponseDto;
import com.vtlong.my_spring_boot_project.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
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
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(HttpServletRequest request) {
        UserResponseDto user = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin user hiện tại thành công", request));
    }

    @GetMapping("/authorities")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserAuthorities(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("authorities", userService.getCurrentUserAuthorities());
        return ResponseEntity.ok(ApiResponse.success(response, "Lấy quyền user hiện tại thành công", request));
    }
}
