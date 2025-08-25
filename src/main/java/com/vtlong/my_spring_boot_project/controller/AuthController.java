package com.vtlong.my_spring_boot_project.controller;

import java.text.ParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.vtlong.my_spring_boot_project.dto.ApiResponse;
import com.vtlong.my_spring_boot_project.dto.request.LoginRequest;
import com.vtlong.my_spring_boot_project.dto.request.IntrospectRequest;
import com.vtlong.my_spring_boot_project.dto.response.LoginResponse;
import com.vtlong.my_spring_boot_project.dto.response.IntrospectResponse;
import com.vtlong.my_spring_boot_project.service.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.handleLogin(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Đăng nhập thành công"));
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest introspectRequest) throws JOSEException, ParseException {
        IntrospectResponse introspectResponse = authService.handleIntrospect(introspectRequest);
        return ResponseEntity.ok(ApiResponse.success(introspectResponse, "Introspect thành công"));
    }
}
