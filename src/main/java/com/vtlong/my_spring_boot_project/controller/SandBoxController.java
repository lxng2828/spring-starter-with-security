package com.vtlong.my_spring_boot_project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vtlong.my_spring_boot_project.dto.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sandbox")
public class SandBoxController {
    @GetMapping("/security-context")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSecurityContextInfo() {
        Map<String, Object> securityInfo = new HashMap<>();
        var securityContext = SecurityContextHolder.getContext();
        securityInfo.put("contextClass", securityContext.getClass().getSimpleName());
        securityInfo.put("contextHashCode", securityContext.hashCode());
        Authentication authentication = securityContext.getAuthentication();
        if (authentication != null) {
            Map<String, Object> authInfo = new HashMap<>();
            authInfo.put("name", authentication.getName());
            authInfo.put("principal", authentication.getPrincipal().toString());
            authInfo.put("principalClass", authentication.getPrincipal().getClass().getSimpleName());
            authInfo.put("authenticated", authentication.isAuthenticated());
            authInfo.put("credentials", authentication.getCredentials() != null ? "***" : null);
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                authInfo.put("authorities", authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()));
                authInfo.put("authorityCount", authorities.size());
            } else {
                authInfo.put("authorities", "No authorities");
                authInfo.put("authorityCount", 0);
            }
            authInfo.put("details", authentication.getDetails());
            authInfo.put("detailsClass",
                    authentication.getDetails() != null ? authentication.getDetails().getClass().getSimpleName()
                            : null);
            securityInfo.put("authentication", authInfo);
        } else {
            securityInfo.put("authentication", "No authentication found");
        }
        Thread currentThread = Thread.currentThread();
        securityInfo.put("threadInfo", Map.of(
                "threadId", currentThread.getId(),
                "threadName", currentThread.getName(),
                "threadPriority", currentThread.getPriority()));
        securityInfo.put("timestamp", System.currentTimeMillis());
        securityInfo.put("timestampReadable", java.time.Instant.now().toString());
        return ResponseEntity
                .ok(ApiResponse.success(securityInfo, "Security context information retrieved successfully"));
    }
}
