package com.vtlong.my_spring_boot_project.controller.admin;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vtlong.my_spring_boot_project.dto.ApiResponse;
import com.vtlong.my_spring_boot_project.dto.request.CreateUserRequestDto;
import com.vtlong.my_spring_boot_project.dto.request.UpdateUserRequestDto;
import com.vtlong.my_spring_boot_project.dto.response.UserResponseDto;
import com.vtlong.my_spring_boot_project.service.AdminUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);
    
    @Autowired
    private AdminUserService adminUserService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponseDto>>> getAllUsers() {
        logger.info("GET /admin/users - Fetching all users");
        List<UserResponseDto> users = adminUserService.findAll();
        logger.info("GET /admin/users - Retrieved {} users", users.size());
        return ResponseEntity.ok(ApiResponse.success(users, "Lấy danh sách người dùng thành công"));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(@PathVariable String id) {
        logger.info("GET /admin/users/{} - Fetching user by id", id);
        
        UserResponseDto user = adminUserService.findById(id);
        logger.info("GET /admin/users/{} - User found", id);
        return ResponseEntity.ok(ApiResponse.success(user, "Lấy thông tin người dùng thành công"));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody CreateUserRequestDto createUserRequestDto) {
        logger.info("POST /admin/users - Creating new user with username: {}", createUserRequestDto.getUsername());
        
        UserResponseDto createdUser = adminUserService.create(createUserRequestDto);
        logger.info("POST /admin/users - User created successfully with id: {}", createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdUser, "Tạo người dùng thành công"));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable String id, 
            @Valid @RequestBody UpdateUserRequestDto updateUserRequestDto) {
        logger.info("PUT /admin/users/{} - Updating user", id);
        
        UserResponseDto updatedUser = adminUserService.update(id, updateUserRequestDto);
        logger.info("PUT /admin/users/{} - User updated successfully", id);
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Cập nhật người dùng thành công"));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String id) {
        logger.info("DELETE /admin/users/{} - Deleting user", id);
        
        adminUserService.delete(id);
        logger.info("DELETE /admin/users/{} - User deleted successfully", id);
        return ResponseEntity.ok(ApiResponse.success("Xóa người dùng thành công"));
    }
}
