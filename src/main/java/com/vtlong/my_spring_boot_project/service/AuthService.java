package com.vtlong.my_spring_boot_project.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.vtlong.my_spring_boot_project.config.JwtConfig;
import com.vtlong.my_spring_boot_project.dto.request.LoginRequest;
import com.vtlong.my_spring_boot_project.dto.response.LoginResponse;
import com.vtlong.my_spring_boot_project.exception.AppException;
import com.vtlong.my_spring_boot_project.exception.ErrorCode;
import com.vtlong.my_spring_boot_project.model.Role;
import com.vtlong.my_spring_boot_project.model.User;
import com.vtlong.my_spring_boot_project.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtConfig jwtConfig) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtConfig = jwtConfig;
    }

    public LoginResponse handleLogin(LoginRequest loginRequest) {
        logger.info("Starting login process for email: {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", loginRequest.getEmail());
                    return new AppException(ErrorCode.USER_NOT_FOUND);
                });

        logger.debug("Found user: {}", user.getUsername());

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            logger.warn("Invalid password for user: {}", user.getUsername());
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }

        logger.info("Authentication successful for user: {}", user.getUsername());
        logger.info("Starting token generation for user: {}", user.getUsername());
        String token = generateToken(user);
        logger.info("Token generated successfully for user: {}", user.getUsername());

        return LoginResponse.builder().success(true).token(token).build();
    }

    private String generateToken(User user) {
        logger.debug("Creating JWT header for user: {}", user.getUsername());
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        logger.debug("Collecting roles for user: {}", user.getUsername());
        Set<String> roleNames = new HashSet<>();
        for (Role role : user.getRoles()) {
            roleNames.add(role.getName().getCode());
        }
        logger.debug("Found {} roles for user: {}", roleNames.size(), user.getUsername());

        logger.debug("Creating JWT claims set for user: {}", user.getUsername());
        JWTClaimsSet jwsClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("vtlong.com")
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * jwtConfig.getExpirationHours()))
                .claim("userId", user.getId())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .claim("roles", roleNames)
                .build();

        Payload payload = new Payload(jwsClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            logger.debug("Signing token for user: {}", user.getUsername());
            jwsObject.sign(new MACSigner(jwtConfig.getSignerKey().getBytes()));
            logger.debug("Token signed successfully for user: {}", user.getUsername());
        } catch (JOSEException e) {
            logger.error("Error signing token for user: {}", user.getUsername(), e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return jwsObject.serialize();
    }
}
